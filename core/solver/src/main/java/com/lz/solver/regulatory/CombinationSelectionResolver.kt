package com.lz.solver.regulatory

import com.lz.model.regulatory.LoadCombinationSet

/** A saved calculation's combination selection resolved against the combination sets that exist now. */
data class CombinationSelection(
    val set: LoadCombinationSet?,
    val enabledNames: Set<String>,
    /** Null = envelope of the enabled combinations is shown. */
    val analysisCombinationName: String?,
    /** Plain-language statements of everything that was not found or not enabled; empty when nothing needed resolving. */
    val notices: List<String>
)

/**
 * Resolves what a saved calculation stored (a combination-set id, enabled combination NAMES and
 * the selected analysis combination NAME; combination row ids are never stored) against the
 * sets seeded now. Never silently produces an empty combination list: with no enabled
 * combinations the solver would run every load unfactored.
 *
 * Rules:
 *  - set id found -> that set; no id saved -> [currentSet]; id not found -> [currentSet] with a notice
 *  - nothing saved as enabled -> every combination of the set
 *  - saved names that no longer exist are dropped with a notice; if none are left, every
 *    combination is enabled (with a notice) rather than none
 *  - combinations in the set that are not in the saved enabled list are kept disabled but
 *    listed in a notice (a saved calculation cannot say whether the user disabled them or the
 *    catalog added them later)
 *  - a selected analysis combination that no longer exists falls back to the envelope with a notice
 */
object CombinationSelectionResolver {

    fun resolve(
        savedSetId: String?,
        savedEnabledNames: Set<String>,
        savedAnalysisName: String?,
        lookup: (String) -> LoadCombinationSet?,
        currentSet: LoadCombinationSet?
    ): CombinationSelection {
        val notices = mutableListOf<String>()

        val found = savedSetId?.let(lookup)
        val set = found ?: currentSet
        if (savedSetId != null && found == null) {
            notices += "The load combination set saved with this calculation (\"$savedSetId\") is not available" +
                (if (currentSet != null) "; \"${currentSet.id}\" is used instead, which may be a different code edition or methodology." else ".")
        }
        if (set == null) {
            notices += "No load combination set is available, so no combinations can be applied."
            return CombinationSelection(null, emptySet(), null, notices)
        }

        val names = set.combinations.map { it.name }
        val nameSet = names.toSet()

        val enabled: Set<String>
        if (savedEnabledNames.isEmpty()) {
            enabled = nameSet
        } else {
            val matched = savedEnabledNames.filter { it in nameSet }.toSet()
            val dropped = savedEnabledNames.filter { it !in nameSet }.sorted()
            if (dropped.isNotEmpty()) {
                notices += "${dropped.size} saved load combination(s) no longer exist in \"${set.id}\" and were dropped: " +
                    dropped.joinToString("; ") + "."
            }
            if (matched.isEmpty()) {
                enabled = nameSet
                notices += "None of the saved combinations matched, so all ${nameSet.size} combinations of \"${set.id}\" are enabled."
            } else {
                enabled = matched
                val notEnabled = names.filter { it !in matched }
                if (notEnabled.isNotEmpty()) {
                    notices += "${notEnabled.size} combination(s) of \"${set.id}\" are not enabled in this saved calculation " +
                        "(disabled by the user or added to the set after it was saved): " + notEnabled.joinToString("; ") + "."
                }
            }
        }

        val analysis = savedAnalysisName?.let { n ->
            if (n in nameSet) n
            else { notices += "The selected combination \"$n\" no longer exists; the envelope of the enabled combinations is shown."; null }
        }
        return CombinationSelection(set, enabled, analysis, notices)
    }
}
