package com.shaikh.storentry

import com.shaikh.storentry.data.sync.SyncConflictResolution
import com.shaikh.storentry.data.sync.SyncConflictState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests validating sync conflict states and resolution strategies.
 */
class SyncConflictTest {

    @Test
    fun testSyncConflictResolutionEnumValues() {
        val cloudMain = SyncConflictResolution.MERGE_CLOUD_MAIN
        val localMain = SyncConflictResolution.MERGE_LOCAL_MAIN
        val overwriteLocal = SyncConflictResolution.OVERWRITE_LOCAL_BY_CLOUD
        val overwriteCloud = SyncConflictResolution.OVERWRITE_CLOUD_BY_LOCAL

        assertEquals("MERGE_CLOUD_MAIN", cloudMain.name)
        assertEquals("MERGE_LOCAL_MAIN", localMain.name)
        assertEquals("OVERWRITE_LOCAL_BY_CLOUD", overwriteLocal.name)
        assertEquals("OVERWRITE_CLOUD_BY_LOCAL", overwriteCloud.name)
    }

    @Test
    fun testSyncConflictStateMapping() {
        val idleState = SyncConflictState.Idle
        val conflictState = SyncConflictState.Conflict(localCount = 42, remoteCount = 88)
        val resolvingState = SyncConflictState.Resolving

        assertTrue(idleState is SyncConflictState.Idle)
        assertTrue(resolvingState is SyncConflictState.Resolving)

        assertTrue(conflictState is SyncConflictState.Conflict)
        assertEquals(42, conflictState.localCount)
        assertEquals(88, conflictState.remoteCount)
    }
}
