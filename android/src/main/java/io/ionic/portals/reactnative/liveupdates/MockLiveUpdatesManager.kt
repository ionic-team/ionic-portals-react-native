package io.ionic.portals.reactnative.liveupdates

import android.util.Log
import io.ionic.liveupdatesprovider.LiveUpdatesError
import io.ionic.liveupdatesprovider.LiveUpdatesManager
import io.ionic.liveupdatesprovider.SyncCallback
import io.ionic.liveupdatesprovider.models.SyncResult
import java.io.File
import kotlin.concurrent.thread

/**
 * Mock implementation of LiveUpdatesManager for testing purposes.
 * Simulates async sync behavior with configurable success/failure scenarios.
 */
internal class MockLiveUpdatesManager(
    private val appId: String,
    private val channel: String,
    private val latestAppDir: File,
    private val shouldFail: Boolean,
    private val failureDetails: String,
    private val didUpdate: Boolean
) : LiveUpdatesManager {

    companion object {
        private const val TAG = "MockLiveUpdatesManager"
        private const val SIMULATED_DELAY_MS = 500L
    }

    override fun sync(callback: SyncCallback?) {
        Log.d(TAG, "Starting mock sync for appId: $appId, channel: $channel")

        // Simulate async behavior with a background thread
        thread {
            try {
                // Simulate network delay
                Thread.sleep(SIMULATED_DELAY_MS)

                if (shouldFail) {
                    Log.d(TAG, "Mock sync failed for appId: $appId - $failureDetails")
                    val error = LiveUpdatesError.SyncFailed(failureDetails, null)
                    callback?.onError(error)
                } else {
                    Log.d(
                        TAG,
                        "Mock sync completed for appId: $appId, didUpdate: $didUpdate"
                    )

                    // Ensure the mock directory exists if didUpdate is true
                    if (didUpdate && !latestAppDir.exists()) {
                        latestAppDir.mkdirs()
                    }

                    val result = SyncResult(
                        didUpdate = didUpdate,
                        latestAppDirectory = if (didUpdate) latestAppDir else null
                    )
                    callback?.onComplete(result)
                }
            } catch (e: InterruptedException) {
                Log.e(TAG, "Mock sync interrupted for appId: $appId", e)
                val error = LiveUpdatesError.SyncFailed("Sync interrupted", e)
                callback?.onError(error)
            }
        }
    }

    override fun latestAppDirectory(): File? {
        return if (latestAppDir.exists()) latestAppDir else null
    }
}
