package com.lz.vectos.domain.structural.analysis

import com.lz.vectos.domain.structural.*
import kotlin.math.abs
import kotlin.math.pow

/**
 * Result of a structural analysis for a single span.
 */
data class SpanAnalysisResult(
    val spanId: java.util.UUID,
    val maxMoment: Double,
    val maxShear: Double,
    val shearDiagram: List<AnalysisPoint>,
    val momentDiagram: List<AnalysisPoint>
)

/**
 * A single point on a diagram.
 */
data class AnalysisPoint(val x: Double, val value: Double)

/**
 * Comprehensive analysis result for a structural member.
 */
data class BeamAnalysisResult(
    val maxMoment: Double,
    val maxShear: Double,
    val spanResults: List<SpanAnalysisResult>
)

/**
 * Matrix-based structural analysis solver for continuous beams.
 * Handles multiple spans and arbitrary loading via the Stiffness Method.
 */
object BeamAnalysisSolver {

    /**
     * Solves for moments and shears across all spans of the member.
     */
    fun solve(
        member: StructuralMember,
        loads: List<Load>,
        ePa: Double = 200e9, // Default to steel if not provided
        iM4: Double = 1e-4
    ): BeamAnalysisResult {
        val spans = member.spans
        if (spans.isEmpty()) return BeamAnalysisResult(0.0, 0.0, emptyList())

        val n = spans.size
        val numNodes = n + 1
        val numDof = numNodes * 2 // 2 DOFs per node: [displacement (v), rotation (theta)]

        val kGlobal = Array(numDof) { DoubleArray(numDof) }
        val fGlobal = DoubleArray(numDof)

        // 1. Assemble Global Stiffness Matrix
        for (idx in 0 until n) {
            val span = spans[idx]
            val l = span.length.meters
            if (l <= 0) continue

            val kLocal = calculateBeamStiffness(ePa, iM4, l)
            val dofIndices = intArrayOf(idx * 2, idx * 2 + 1, (idx + 1) * 2, (idx + 1) * 2 + 1)

            for (i in 0 until 4) {
                for (j in 0 until 4) {
                    kGlobal[dofIndices[i]][dofIndices[j]] += kLocal[i][j]
                }
            }
        }

        // 2. Equivalent Nodal Loads (Fixed End Actions)
        for (idx in 0 until n) {
            val span = spans[idx]
            val l = span.length.meters
            val spanLoads = loads.filter { it.spanId == span.id }
            val dofIndices = intArrayOf(idx * 2, idx * 2 + 1, (idx + 1) * 2, (idx + 1) * 2 + 1)

            spanLoads.forEach { load ->
                val fea = calculateFixedEndActions(load, l)
                for (i in 0 until 4) {
                    fGlobal[dofIndices[i]] -= fea[i]
                }
            }
        }

        // 3. Apply Boundary Conditions
        val constrained = BooleanArray(numDof)
        
        // Start support
        when (member.spans.first().startSupport) {
            SupportCondition.FIXED -> { constrained[0] = true; constrained[1] = true }
            SupportCondition.PINNED, SupportCondition.ROLLER -> { constrained[0] = true }
            else -> {}
        }

        // End supports of all spans
        for (idx in 0 until n) {
            val support = spans[idx].endSupport
            when (support) {
                SupportCondition.FIXED -> { constrained[(idx + 1) * 2] = true; constrained[(idx + 1) * 2 + 1] = true }
                SupportCondition.PINNED, SupportCondition.ROLLER -> { constrained[(idx + 1) * 2] = true }
                else -> {}
            }
        }

        // 4. Solve for Displacements
        val freeDofs = (0 until numDof).filter { !constrained[it] }
        val dGlobal = DoubleArray(numDof)

        if (freeDofs.isNotEmpty()) {
            val kRed = Array(freeDofs.size) { DoubleArray(freeDofs.size) }
            val fRed = DoubleArray(freeDofs.size)

            for (i in freeDofs.indices) {
                fRed[i] = fGlobal[freeDofs[i]]
                for (j in freeDofs.indices) {
                    kRed[i][j] = kGlobal[freeDofs[i]][freeDofs[j]]
                }
            }

            val dFree = solveLinearSystem(kRed, fRed)
            for (i in freeDofs.indices) {
                dGlobal[freeDofs[i]] = dFree[i]
            }
        }

        // 5. Post-process: Calculate Internal Forces and Diagrams
        var globalMaxM = 0.0
        var globalMaxV = 0.0
        val spanResults = mutableListOf<SpanAnalysisResult>()

        for (idx in 0 until n) {
            val span = spans[idx]
            val l = span.length.meters
            val dLocal = doubleArrayOf(dGlobal[idx * 2], dGlobal[idx * 2 + 1], dGlobal[(idx + 1) * 2], dGlobal[(idx + 1) * 2 + 1])
            val kLocal = calculateBeamStiffness(ePa, iM4, l)

            // Member end actions due to nodal displacements
            val fDisplacement = DoubleArray(4)
            for (i in 0 until 4) {
                for (j in 0 until 4) {
                    fDisplacement[i] += kLocal[i][j] * dLocal[j]
                }
            }

            val spanLoads = loads.filter { it.spanId == span.id }
            val fTotalEnd = DoubleArray(4) { fDisplacement[it] }
            spanLoads.forEach { load ->
                val fea = calculateFixedEndActions(load, l)
                for (i in 0 until 4) fTotalEnd[i] += fea[i]
            }

            // [V1, M1, V2, M2]
            val v1 = fTotalEnd[0]
            val m1 = fTotalEnd[1]

            val shearPoints = mutableListOf<AnalysisPoint>()
            val momentPoints = mutableListOf<AnalysisPoint>()
            var spanMaxM = 0.0
            var spanMaxV = 0.0

            val steps = 100
            for (s in 0..steps) {
                val x = (s.toDouble() / steps) * l
                val vx = calculateShearAt(x, v1, spanLoads)
                val mx = calculateMomentAt(x, v1, m1, spanLoads)

                shearPoints.add(AnalysisPoint(x, vx))
                momentPoints.add(AnalysisPoint(x, mx))

                if (abs(vx) > spanMaxV) spanMaxV = abs(vx)
                if (abs(mx) > spanMaxM) spanMaxM = abs(mx)
            }

            if (spanMaxM > globalMaxM) globalMaxM = spanMaxM
            if (spanMaxV > globalMaxV) globalMaxV = spanMaxV

            spanResults.add(SpanAnalysisResult(span.id, spanMaxM, spanMaxV, shearPoints, momentPoints))
        }

        return BeamAnalysisResult(globalMaxM, globalMaxV, spanResults)
    }

    private fun calculateBeamStiffness(e: Double, i: Double, l: Double): Array<DoubleArray> {
        val ei = e * i
        val l2 = l * l
        val l3 = l2 * l
        return arrayOf(
            doubleArrayOf(12 * ei / l3, 6 * ei / l2, -12 * ei / l3, 6 * ei / l2),
            doubleArrayOf(6 * ei / l2, 4 * ei / l, -6 * ei / l2, 2 * ei / l),
            doubleArrayOf(-12 * ei / l3, -6 * ei / l2, 12 * ei / l3, -6 * ei / l2),
            doubleArrayOf(6 * ei / l2, 2 * ei / l, -6 * ei / l2, 4 * ei / l)
        )
    }

    private fun calculateFixedEndActions(load: Load, l: Double): DoubleArray {
        // Sign convention: Upwards force positive, Counter-clockwise moment positive
        return when (load) {
            is Load.PointLoad -> {
                val p = load.value
                val a = load.locationStart
                val b = l - a
                val v1 = p * b * b * (3 * a + b) / l.pow(3)
                val m1 = p * a * b * b / l.pow(2)
                val v2 = p * a * a * (a + 3 * b) / l.pow(3)
                val m2 = -p * a * a * b / l.pow(2)
                doubleArrayOf(v1, m1, v2, m2)
            }
            is Load.UniformDistributedLoad -> {
                integrateFixedEndActions(load.value, load.value, load.locationStart, load.locationEnd, l)
            }
            is Load.TrapezoidalLoad -> {
                integrateFixedEndActions(load.valueStart, load.valueEnd, load.locationStart, load.locationEnd, l)
            }
            else -> DoubleArray(4)
        }
    }

    private fun integrateFixedEndActions(w1: Double, w2: Double, a: Double, b: Double, l: Double): DoubleArray {
        val numSegments = 20
        val actions = DoubleArray(4)
        val dx = (b - a) / numSegments
        if (dx <= 0) return actions

        for (i in 0 until numSegments) {
            val xi_start = a + i * dx
            val xi_end = a + (i + 1) * dx
            val xi_mid = (xi_start + xi_end) / 2.0
            
            val w_mid = w1 + (w2 - w1) * ((xi_mid - a) / (b - a))
            val pi = w_mid * dx
            
            val pointActions = calculateFixedEndActions(Load.PointLoad(pi, java.util.UUID.randomUUID(), xi_mid), l)
            for (j in 0 until 4) actions[j] += pointActions[j]
        }
        return actions
    }

    private fun calculateShearAt(x: Double, v1: Double, loads: List<Load>): Double {
        var vx = v1
        loads.forEach { load ->
            when (load) {
                is Load.PointLoad -> if (x > load.locationStart) vx -= load.value
                is Load.UniformDistributedLoad -> {
                    if (x > load.locationStart) {
                        val activeL = minOf(x, load.locationEnd) - load.locationStart
                        if (activeL > 0) vx -= load.value * activeL
                    }
                }
                is Load.TrapezoidalLoad -> {
                    if (x > load.locationStart) {
                        val activeL = minOf(x, load.locationEnd) - load.locationStart
                        if (activeL > 0) {
                            val wStart = load.valueStart
                            val wEndAtX = load.valueStart + (load.valueEnd - load.valueStart) * 
                                          ((minOf(x, load.locationEnd) - load.locationStart) / (load.locationEnd - load.locationStart))
                            val avgW = (wStart + wEndAtX) / 2.0
                            vx -= avgW * activeL
                        }
                    }
                }
                else -> {}
            }
        }
        return vx
    }

    private fun calculateMomentAt(x: Double, v1: Double, m1: Double, loads: List<Load>): Double {
        // M(x) = -M1 + V1*x - Sum(M_loads)
        // m1 is CCW at start node (internal element convention)
        var mx = -m1 + v1 * x
        loads.forEach { load ->
            when (load) {
                is Load.PointLoad -> {
                    if (x > load.locationStart) mx -= load.value * (x - load.locationStart)
                }
                is Load.UniformDistributedLoad -> {
                    if (x > load.locationStart) {
                        val activeL = minOf(x, load.locationEnd) - load.locationStart
                        if (activeL > 0) {
                            mx -= load.value * activeL * (x - (load.locationStart + activeL / 2.0))
                        }
                    }
                }
                is Load.TrapezoidalLoad -> {
                    if (x > load.locationStart) {
                        val activeL = minOf(x, load.locationEnd) - load.locationStart
                        if (activeL > 0) {
                            val wStart = load.valueStart
                            val wEndAtX = load.valueStart + (load.valueEnd - load.valueStart) * 
                                          ((minOf(x, load.locationEnd) - load.locationStart) / (load.locationEnd - load.locationStart))
                            
                            // Rectangular part
                            val wMin = minOf(wStart, wEndAtX)
                            mx -= wMin * activeL * (x - (load.locationStart + activeL / 2.0))
                            
                            // Triangular part
                            val wTri = abs(wEndAtX - wStart)
                            val triTotal = 0.5 * wTri * activeL
                            val triArm = if (wEndAtX > wStart) (1.0/3.0) * activeL else (2.0/3.0) * activeL
                            mx -= triTotal * triArm
                        }
                    }
                }
                else -> {}
            }
        }
        return mx
    }

    private fun solveLinearSystem(matrix: Array<DoubleArray>, vector: DoubleArray): DoubleArray {
        val n = vector.size
        for (i in 0 until n) {
            var max = abs(matrix[i][i])
            var maxRow = i
            for (k in i + 1 until n) {
                if (abs(matrix[k][i]) > max) {
                    max = abs(matrix[k][i])
                    maxRow = k
                }
            }
            val temp = matrix[maxRow]
            matrix[maxRow] = matrix[i]
            matrix[i] = temp
            val t = vector[maxRow]
            vector[maxRow] = vector[i]
            vector[i] = t

            for (k in i + 1 until n) {
                val c = -matrix[k][i] / matrix[i][i]
                for (j in i until n) {
                    if (i == j) matrix[k][j] = 0.0 else matrix[k][j] += c * matrix[i][j]
                }
                vector[k] += c * vector[i]
            }
        }
        val x = DoubleArray(n)
        for (i in n - 1 downTo 0) {
            x[i] = vector[i] / matrix[i][i]
            for (k in i - 1 downTo 0) vector[k] -= matrix[k][i] * x[i]
        }
        return x
    }
}
