package com.lz.vectos.domain.structural.analysis.core

import kotlin.math.*

class StructuralSystem {
    private val nodes = mutableListOf<Node>()
    private val elements = mutableListOf<Element>()
    private val elementEquivalentForces = mutableMapOf<Int, DoubleArray>()
    private val fixedDofs = mutableSetOf<Pair<Int, DofType>>()
    private val externalForces = mutableMapOf<Pair<Int, DofType>, Double>()

    data class Node(val id: Int, val x: Double, val y: Double, val z: Double)
    data class Element(
        val id: Int,
        val startNode: Int,
        val endNode: Int,
        val e: Double,
        val i: Double,
        val iy: Double,
        val g: Double,
        val a: Double,
        val j: Double,
        val dofConfig: DofConfig
    )

    fun addNode(x: Double, y: Double, z: Double): Int {
        val id = nodes.size
        nodes.add(Node(id, x, y, z))
        return id
    }

    fun addElement(startNode: Int, endNode: Int, e: Double, i: Double, iy: Double, g: Double, a: Double, j: Double, dofConfig: DofConfig) {
        elements.add(Element(elements.size, startNode, endNode, e, i, iy, g, a, j, dofConfig))
    }

    /**
     * Adds an Equivalent Nodal Load to the element ends.
     * Note: This should be the NEGATIVE of the Fixed End Actions (FEA).
     * For a downward UDL (w), Feq_y1 = -wL/2, Meq_z1 = -wL^2/12.
     */
    fun addElementEquivalentForce(elementIdx: Int, dofInElement: Int, value: Double) {
        val forces = elementEquivalentForces.getOrPut(elementIdx) { DoubleArray(12) }
        forces[dofInElement] += value
    }

    fun fixNode(nodeIdx: Int, dof: DofType) {
        fixedDofs.add(nodeIdx to dof)
    }

    fun addNodeForce(nodeIdx: Int, dof: DofType, value: Double) {
        val current = externalForces.getOrDefault(nodeIdx to dof, 0.0)
        externalForces[nodeIdx to dof] = current + value
    }

    fun getNodes() = nodes.toList()
    fun getElements() = elements.toList()
    fun getElementEquivalentForces() = elementEquivalentForces.toMap()
    fun getFixedDofs() = fixedDofs.toSet()
    fun getExternalForces() = externalForces.toMap()

    fun getNodeDofs(nodeIdx: Int): List<DofType> {
        return DofType.values().toList()
    }
}

data class StructuralResult(
    val displacements: Map<Pair<Int, DofType>, Double>,
    val reactions: Map<Int, Map<DofType, Double>>,
    val elementEndForces: Map<Int, List<Double>> = emptyMap()
)

data class DofConfig(
    val dofsPerNode: Int,
    val dofTypes: List<DofType>
) {
    companion object {
        val SIX_DOF = DofConfig(6, listOf(DofType.UX, DofType.UY, DofType.UZ, DofType.RX, DofType.RY, DofType.RZ))
    }
}

enum class DofType {
    UX, UY, UZ,
    RX, RY, RZ
}

class StructuralSolver {
    fun solve(system: StructuralSystem): StructuralResult {
        val nodes = system.getNodes()
        val elements = system.getElements()
        val eqForces = system.getElementEquivalentForces()
        val fixedDofs = system.getFixedDofs()
        val externalForces = system.getExternalForces()

        val numNodes = nodes.size
        val dofsPerNode = 6
        val totalDofs = numNodes * dofsPerNode

        val globalK = Array(totalDofs) { DoubleArray(totalDofs) }
        val globalF = DoubleArray(totalDofs)

        // 1. Assemble Global Stiffness Matrix and Equivalent Loads
        elements.forEach { el ->
            val n1 = nodes[el.startNode]
            val n2 = nodes[el.endNode]
            val dx = n2.x - n1.x
            val dy = n2.y - n1.y
            val dz = n2.z - n1.z
            val L = sqrt(dx * dx + dy * dy + dz * dz)
            if (L <= 1e-9) return@forEach

            val keLocal = calculateElementStiffness(el, L)
            val R = calculateRotationMatrix(n1, n2)
            val keGlobal = transformMatrix(keLocal, R)

            val dofIndices = getElementDofIndices(el)

            // Transform local equivalent forces to global: Fg = T' * Fl
            val feqLocal = eqForces[el.id] ?: DoubleArray(12)
            val feqGlobal = transformVector(feqLocal, R, transpose = true)

            for (i in 0 until 12) {
                for (j in 0 until 12) {
                    globalK[dofIndices[i]][dofIndices[j]] += keGlobal[i][j]
                }
                globalF[dofIndices[i]] += feqGlobal[i]
            }
        }

        // 2. Assemble Load Vector
        externalForces.forEach { (key, value) ->
            val (nodeIdx, dof) = key
            val globalDof = nodeIdx * dofsPerNode + dof.ordinal
            globalF[globalDof] += value
        }

        // 3. Apply Boundary Conditions (Penalty Method for simplicity in this implementation)
        val penalty = 1e10
        fixedDofs.forEach { (nodeIdx, dof) ->
            val globalDof = nodeIdx * dofsPerNode + dof.ordinal
            globalK[globalDof][globalDof] += penalty
            // globalF[globalDof] = 0.0 // Assuming zero displacement fixed supports
        }

        // 4. Solve System (Gaussian Elimination)
        val u = solveLinearSystem(globalK, globalF)

        // 5. Post-process Displacements & Reactions
        val displacements = mutableMapOf<Pair<Int, DofType>, Double>()
        val reactions = mutableMapOf<Int, MutableMap<DofType, Double>>()

        nodes.forEach { node ->
            reactions[node.id] = mutableMapOf()
            DofType.values().forEach { dof ->
                val globalDof = node.id * dofsPerNode + dof.ordinal
                displacements[node.id to dof] = u[globalDof]

                // Reaction = K_unconstrained * u - F_ext (at constrained DOFs)
                // In penalty method: (K_orig + penalty) * u = F_ext + F_eq
                // Reaction R = K_orig * u - F_ext - F_eq = -penalty * u
                val isFixed = fixedDofs.contains(node.id to dof)
                if (isFixed) {
                    reactions[node.id]!![dof] = -u[globalDof] * penalty
                } else {
                    reactions[node.id]!![dof] = 0.0
                }
            }
        }

        // 6. Calculate Element End Forces (Local)
        val elementEndForces = mutableMapOf<Int, List<Double>>()
        elements.forEach { el ->
            val n1 = nodes[el.startNode]
            val n2 = nodes[el.endNode]
            val dx = n2.x - n1.x
            val dy = n2.y - n1.y
            val dz = n2.z - n1.z
            val L = sqrt(dx * dx + dy * dy + dz * dz)
            if (L <= 1e-9) return@forEach

            val keLocal = calculateElementStiffness(el, L)
            val R = calculateRotationMatrix(n1, n2)
            val dofIndices = getElementDofIndices(el)

            val ueGlobal = DoubleArray(12) { u[dofIndices[it]] }
            // Transform global displacements to local: uL = T * ug
            val ueLocal = transformVector(ueGlobal, R, transpose = false)

            val feqLocal = eqForces[el.id] ?: DoubleArray(12)

            // fe_local = ke_local * ue_local - feq_local
            val feLocal = DoubleArray(12)
            for (i in 0 until 12) {
                var sum = 0.0
                for (j in 0 until 12) {
                    sum += keLocal[i][j] * ueLocal[j]
                }
                feLocal[i] = sum - feqLocal[i]
            }
            elementEndForces[el.id] = feLocal.toList()
        }

        return StructuralResult(displacements, reactions, elementEndForces)
    }

    private fun calculateRotationMatrix(n1: StructuralSystem.Node, n2: StructuralSystem.Node): Array<DoubleArray> {
        val dx = n2.x - n1.x
        val dy = n2.y - n1.y
        val dz = n2.z - n1.z
        val L = sqrt(dx * dx + dy * dy + dz * dz)
        if (L < 1e-9) return Array(3) { i -> DoubleArray(3) { j -> if (i == j) 1.0 else 0.0 } }
        val cx = dx / L
        val cy = dy / L
        val cz = dz / L
        val R = Array(3) { DoubleArray(3) }
        if (abs(cx) < 1e-6 && abs(cz) < 1e-6) {
            R[0][1] = cy; R[1][0] = -cy; R[2][2] = 1.0
        } else {
            val D = sqrt(cx * cx + cz * cz)
            R[0][0] = cx; R[0][1] = cy; R[0][2] = cz
            R[1][0] = -cx * cy / D; R[1][1] = D; R[1][2] = -cy * cz / D
            R[2][0] = -cz / D; R[2][1] = 0.0; R[2][2] = cx / D
        }
        return R
    }

    private fun transformMatrix(kLocal: Array<DoubleArray>, R: Array<DoubleArray>): Array<DoubleArray> {
        val T = Array(12) { DoubleArray(12) }
        for (i in 0 until 4) {
            val offset = i * 3
            for (r in 0 until 3) {
                for (c in 0 until 3) {
                    T[offset + r][offset + c] = R[r][c]
                }
            }
        }
        val TT = transpose(T)
        val temp = multiply(TT, kLocal)
        return multiply(temp, T)
    }

    private fun transformVector(v: DoubleArray, R: Array<DoubleArray>, transpose: Boolean): DoubleArray {
        val res = DoubleArray(12)
        val mat = if (transpose) transpose(R) else R
        for (i in 0 until 4) {
            val subV = doubleArrayOf(v[i * 3], v[i * 3 + 1], v[i * 3 + 2])
            val subRes = multiplyVec(mat, subV)
            res[i * 3] = subRes[0]
            res[i * 3 + 1] = subRes[1]
            res[i * 3 + 2] = subRes[2]
        }
        return res
    }

    private fun transpose(A: Array<DoubleArray>): Array<DoubleArray> {
        val n = A.size; val m = A[0].size
        val B = Array(m) { DoubleArray(n) }
        for (i in 0 until n) for (j in 0 until m) B[j][i] = A[i][j]
        return B
    }

    private fun multiply(A: Array<DoubleArray>, B: Array<DoubleArray>): Array<DoubleArray> {
        val n = A.size; val m = B[0].size; val p = A[0].size
        val C = Array(n) { DoubleArray(m) }
        for (i in 0 until n) for (j in 0 until m) {
            var s = 0.0
            for (k in 0 until p) s += A[i][k] * B[k][j]
            C[i][j] = s
        }
        return C
    }

    private fun multiplyVec(A: Array<DoubleArray>, v: DoubleArray): DoubleArray {
        val n = A.size; val m = A[0].size
        val res = DoubleArray(n)
        for (i in 0 until n) {
            var s = 0.0
            for (j in 0 until m) s += A[i][j] * v[j]
            res[i] = s
        }
        return res
    }

    private fun getElementDofIndices(el: StructuralSystem.Element): IntArray {
        val indices = IntArray(12)
        val n1Base = el.startNode * 6
        val n2Base = el.endNode * 6
        for (i in 0 until 6) indices[i] = n1Base + i
        for (i in 0 until 6) indices[i + 6] = n2Base + i
        return indices
    }

    private fun calculateElementStiffness(el: StructuralSystem.Element, L: Double): Array<DoubleArray> {
        val k = Array(12) { DoubleArray(12) }
        val E = el.e; val I = el.i; val Iy = el.iy
        val A = el.a; val G = el.g; val J = el.j

        // Axial (UX)
        val kAxial = E * A / L
        k[0][0] = kAxial; k[0][6] = -kAxial
        k[6][0] = -kAxial; k[6][6] = kAxial

        // Torsion (RX)
        val kTorsion = G * J / L
        k[3][3] = kTorsion; k[3][9] = -kTorsion
        k[9][3] = -kTorsion; k[9][9] = kTorsion

        // Bending Z (Strong Axis - UY, RZ)
        val k1z = 12 * E * I / (L.pow(3))
        val k2z = 6 * E * I / (L.pow(2))
        val k3z = 4 * E * I / L
        val k4z = 2 * E * I / L

        k[1][1] = k1z;  k[1][5] = k2z;  k[1][7] = -k1z; k[1][11] = k2z
        k[5][1] = k2z;  k[5][5] = k3z;  k[5][7] = -k2z; k[5][11] = k4z
        k[7][1] = -k1z; k[7][5] = -k2z; k[7][7] = k1z;  k[7][11] = -k2z
        k[11][1] = k2z; k[11][5] = k4z; k[11][7] = -k2z; k[11][11] = k3z

        // Bending Y (Weak Axis - UZ, RY) - Signs adjusted for th_y = -dw/dx
        val k1y = 12 * E * Iy / (L.pow(3))
        val k2y = 6 * E * Iy / (L.pow(2))
        val k3y = 4 * E * Iy / L
        val k4y = 2 * E * Iy / L

        k[2][2] = k1y;   k[2][4] = -k2y;  k[2][8] = -k1y; k[2][10] = k2y
        k[4][2] = k2y;  k[4][4] = k3y;   k[4][8] = -k2y;  k[4][10] = k4y
        k[8][2] = -k1y;  k[8][4] = -k2y;   k[8][8] = k1y;  k[8][10] = -k2y
        k[10][2] = -k2y; k[10][4] = k4y;  k[10][8] = k2y; k[10][10] = k3y

        return k
    }

    private fun solveLinearSystem(A: Array<DoubleArray>, b: DoubleArray): DoubleArray {
        val n = b.size
        for (i in 0 until n) {
            var max = abs(A[i][i])
            var maxRow = i
            for (k in i + 1 until n) {
                if (abs(A[k][i]) > max) {
                    max = abs(A[k][i])
                    maxRow = k
                }
            }

            if (abs(A[maxRow][i]) < 1e-18) {
                // System is singular or under-constrained
                continue
            }

            val temp = A[maxRow]
            A[maxRow] = A[i]
            A[i] = temp
            val t = b[maxRow]
            b[maxRow] = b[i]
            b[i] = t

            for (k in i + 1 until n) {
                val c = -A[k][i] / A[i][i]
                for (j in i until n) {
                    if (i == j) A[k][j] = 0.0
                    else A[k][j] += c * A[i][j]
                }
                b[k] += c * b[i]
            }
        }

        val x = DoubleArray(n)
        for (i in n - 1 downTo 0) {
            x[i] = b[i] / A[i][i]
            for (k in i - 1 downTo 0) {
                b[k] -= A[k][i] * x[i]
            }
        }
        return x
    }
}
