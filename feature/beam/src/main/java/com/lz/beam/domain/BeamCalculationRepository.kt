package com.lz.beam.domain

import com.lz.beam.model.BeamCalculation
import java.util.UUID

/**
 * Authoritative persistence contract dedicated exclusively to the Beam design module.
 * Lives entirely inside the decoupled :feature:beam workspace.
 */
interface BeamCalculationRepository {

    /**
     * Loads a full, multi-span beam layout complete with cross-section states,
     * physical loading patterns, and analysis profiles.
     */
    suspend fun getBeamCalculation(id: UUID): BeamCalculation?

    /**
     * Serializes and writes a complete beam configuration back to persistent storage.
     */
    suspend fun saveBeamCalculation(calculation: BeamCalculation)

    suspend fun deleteBeamCalculation(id: UUID)
}