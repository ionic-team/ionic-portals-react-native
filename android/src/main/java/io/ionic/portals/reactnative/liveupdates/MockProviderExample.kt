package io.ionic.portals.reactnative.liveupdates

import android.content.Context
import android.util.Log
import io.ionic.liveupdatesprovider.LiveUpdatesRegistry
import io.ionic.liveupdatesprovider.SyncCallback
import io.ionic.liveupdatesprovider.models.ProviderConfig
import io.ionic.liveupdatesprovider.models.SyncResult
import io.ionic.liveupdatesprovider.LiveUpdatesError

/**
 * Example demonstrating how to use the MockLiveUpdatesProvider.
 * This can be called from your Android application code for testing.
 */
object MockProviderExample {
    private const val TAG = "MockProviderExample"

    /**
     * Example 1: Successful sync with update
     */
    fun testSuccessfulSync(context: Context) {
        Log.d(TAG, "=== Testing Successful Sync ===")

        val config = ProviderConfig(
            mapOf(
                "appId" to "test-app-123",
                "channel" to "production",
                "didUpdate" to true,
                "shouldFail" to false
            )
        )

        val provider = LiveUpdatesRegistry.resolve("ionic-mock")
        if (provider == null) {
            Log.e(TAG, "Mock provider not registered!")
            return
        }

        val manager = provider.createManager(context, config)

        manager.sync(object : SyncCallback {
            override fun onComplete(result: SyncResult) {
                Log.d(TAG, "✅ Sync successful!")
                Log.d(TAG, "  - Did update: ${result.didUpdate}")
                Log.d(TAG, "  - Latest dir: ${result.latestAppDirectory}")
            }

            override fun onError(error: LiveUpdatesError.SyncFailed) {
                Log.e(TAG, "❌ Sync failed: ${error.message}")
            }
        })
    }

    /**
     * Example 2: Sync without update
     */
    fun testNoUpdateSync(context: Context) {
        Log.d(TAG, "=== Testing No Update Sync ===")

        val config = ProviderConfig(
            mapOf(
                "appId" to "test-app-456",
                "channel" to "development",
                "didUpdate" to false,
                "shouldFail" to false
            )
        )

        val provider = LiveUpdatesRegistry.require("ionic-mock")
        val manager = provider.createManager(context, config)

        manager.sync(object : SyncCallback {
            override fun onComplete(result: SyncResult) {
                Log.d(TAG, "✅ Sync successful!")
                Log.d(TAG, "  - Did update: ${result.didUpdate}")
                Log.d(TAG, "  - Latest dir: ${result.latestAppDirectory}")
            }

            override fun onError(error: LiveUpdatesError.SyncFailed) {
                Log.e(TAG, "❌ Sync failed: ${error.message}")
            }
        })
    }

    /**
     * Example 3: Failed sync
     */
    fun testFailedSync(context: Context) {
        Log.d(TAG, "=== Testing Failed Sync ===")

        val config = ProviderConfig(
            mapOf(
                "appId" to "test-app-789",
                "channel" to "staging",
                "shouldFail" to true,
                "failureDetails" to "Network timeout error"
            )
        )

        val provider = LiveUpdatesRegistry.require("ionic-mock")
        val manager = provider.createManager(context, config)

        manager.sync(object : SyncCallback {
            override fun onComplete(result: SyncResult) {
                Log.d(TAG, "✅ Sync successful!")
                Log.d(TAG, "  - Did update: ${result.didUpdate}")
            }

            override fun onError(error: LiveUpdatesError.SyncFailed) {
                Log.e(TAG, "❌ Sync failed (expected): ${error.message}")
            }
        })
    }

    /**
     * Run all tests
     */
    fun runAllTests(context: Context) {
        Log.d(TAG, "\n========================================")
        Log.d(TAG, "Running Mock Provider Tests")
        Log.d(TAG, "========================================\n")

        testSuccessfulSync(context)
        Thread.sleep(600) // Wait for async completion

        testNoUpdateSync(context)
        Thread.sleep(600)

        testFailedSync(context)
        Thread.sleep(600)

        Log.d(TAG, "\n========================================")
        Log.d(TAG, "Tests Complete")
        Log.d(TAG, "========================================\n")
    }
}
