package com.shaikh.storentry.data.sync

/**
 * Resolution strategies for Firestore & Room synchronization conflict.
 */
enum class SyncConflictResolution {
    /**
     * Keep all records. For matching product IDs, the cloud/remote version is kept.
     */
    MERGE_CLOUD_MAIN,

    /**
     * Keep all records. For matching product IDs, the local/device version is kept.
     */
    MERGE_LOCAL_MAIN,

    /**
     * Overwrites the local Room database with remote Firestore data.
     */
    OVERWRITE_LOCAL_BY_CLOUD,

    /**
     * Overwrites remote Firestore data with the local Room database.
     */
    OVERWRITE_CLOUD_BY_LOCAL
}

/**
 * States representing the status of synchronization conflicts.
 */
sealed class SyncConflictState {
    object Idle : SyncConflictState()
    
    data class Conflict(
        val localCount: Int,
        val remoteCount: Int
    ) : SyncConflictState()
    
    object Resolving : SyncConflictState()
}
