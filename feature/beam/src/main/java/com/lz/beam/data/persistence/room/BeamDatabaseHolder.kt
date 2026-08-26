package com.lz.beam.data.persistence.room

import android.content.Context

/**
 * Caches the single BeamDatabase instance for the process lifetime.
 *
 * BeamDatabase isn't Hilt-managed (see BeamEntryPoint for why), so nothing
 * else enforces singleton scoping for it. Without this holder, each
 * BeamViewModel construction would open a new Room connection against the
 * same physical "vectos.db" file that AppDatabase also uses.
 */
internal object BeamDatabaseHolder {

    @Volatile
    private var instance: BeamDatabase? = null

    fun get(context: Context): BeamDatabase =
        instance ?: synchronized(this) {
            instance ?: BeamDatabase.create(context.applicationContext).also {
                instance = it
            }
        }
}
