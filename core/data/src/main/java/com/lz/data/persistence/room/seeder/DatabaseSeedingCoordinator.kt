package com.lz.data.persistence.room.seeder

import kotlinx.coroutines.CompletableDeferred
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Signals when VectosApplication's one-time catalog seeding
 * (BuildingCodeSeeder / StructuralDataSeeder / AiscSectionSeeder /
 * MaterialSeeder) has finished, so consumers that read seeded reference
 * data at startup can wait for it instead of racing it.
 *
 * VectosApplication.seedDatabase() runs on a fire-and-forget
 * applicationScope coroutine — deliberately not runBlocking, since a full
 * cold seed (building codes, load combinations, AISC section catalog,
 * materials) is real disk I/O, not the "microseconds" a genuinely trivial
 * startup check would cost; blocking Application.onCreate() on it would
 * risk an ANR on slower devices. But MainActivity — and any ViewModel it
 * constructs — can start composing essentially immediately after
 * Application.onCreate() returns, with no guarantee seeding has finished
 * by then. On a warm/previously-seeded install (seeders are idempotent —
 * they no-op once their tables are non-empty) the race is invisible,
 * which is why this went unnoticed on repeatedly-reused emulator installs.
 * On a genuine first/cold launch on real device hardware, seeding can lose
 * that race outright, and since the affected reads are one-shot
 * (getAllBuildingCodes() etc. resolve a single suspend snapshot, not an
 * observed Flow) a lost race isn't just briefly-empty — it stays empty for
 * that app session.
 *
 * Any consumer reading seeded catalog data during startup should
 * `awaitSeeded()` before its first read.
 */
@Singleton
class DatabaseSeedingCoordinator @Inject constructor() {

    private val completion = CompletableDeferred<Unit>()

    suspend fun awaitSeeded() = completion.await()

    /** Called once seeding has finished — successfully or not; see
     * VectosApplication's seedDatabase(). Seeding failure is treated as
     * non-fatal there (app can still run with an empty catalog), so this
     * completes either way rather than leaving waiters hanging forever. */
    fun markSeeded() {
        if (!completion.isCompleted) completion.complete(Unit)
    }
}
