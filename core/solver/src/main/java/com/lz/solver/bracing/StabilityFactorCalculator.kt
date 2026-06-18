package com.lz.solver.bracing

import com.lz.model.structural.StationDemand

/**
 * Material-specific contract for calculating the stability modification factor
 * applied to an unbraced segment.
 *
 * AISC 360:  Cb  (F1-1) — moment gradient factor for LTB
 * NDS:       CL       — beam stability factor (Section 3.3.3)
 * ADM:       Cb       — analogous to AISC per Part I Section F
 * TMS 402:   not applicable (slenderness handled differently)
 *
 * Implementors live in solver/material/ and are injected into BracingLogic
 * by the feature-layer solver (BeamAnalysisSolver).
 */
fun interface StabilityFactorCalculator {
    /**
     * @param segmentDemands Station demands within the unbraced segment.
     * @param isCantilever   True if one end of the segment is a free end.
     * @return               The stability factor (e.g. Cb >= 1.0). Return 1.0 as
     *                       conservative default when insufficient data.
     */
    fun calculate(segmentDemands: List<StationDemand>, isCantilever: Boolean): Double
}