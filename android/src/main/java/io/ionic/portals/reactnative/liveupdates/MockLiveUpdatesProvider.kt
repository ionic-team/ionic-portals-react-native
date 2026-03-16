package io.ionic.portals.reactnative.liveupdates

import android.content.Context
import android.util.Log
import io.ionic.liveupdatesprovider.LiveUpdatesError
import io.ionic.liveupdatesprovider.LiveUpdatesManager
import io.ionic.liveupdatesprovider.LiveUpdatesProvider
import io.ionic.liveupdatesprovider.LiveUpdatesRegistry
import io.ionic.liveupdatesprovider.models.ProviderConfig
import java.io.File

/**
 * Mock implementation of LiveUpdatesProvider for testing purposes in React Native.
 * This allows testing the live updates API without making actual network requests.
 */
class MockLiveUpdatesProvider private constructor() : LiveUpdatesProvider {
    companion object {
        private const val TAG = "MockLiveUpdatesProvider"
        private const val PROVIDER_ID = "ionic-mock"

        val INSTANCE: MockLiveUpdatesProvider by lazy { MockLiveUpdatesProvider() }
        private var isRegistered = false

        /**
         * Initializes and registers the mock provider with the LiveUpdatesRegistry.
         * This should be called in the Application onCreate() before any live updates are used.
         */
        @JvmStatic
        fun initialize() {
            if (!isRegistered) {
                Log.d(TAG, "Registering MockLiveUpdatesProvider")
                LiveUpdatesRegistry.register(INSTANCE)
                isRegistered = true
            }
        }
    }

    override val id: String
        get() = PROVIDER_ID

    @Throws(LiveUpdatesError.InvalidConfiguration::class)
    override fun createManager(
        context: Context,
        config: ProviderConfig
    ): LiveUpdatesManager {
        val configData = config.data

        // Extract appId (required)
        val appId = configData["appId"] as? String
            ?: throw LiveUpdatesError.InvalidConfiguration(
                "Mock provider requires 'appId' in config",
                null
            )

        if (appId.trim().isEmpty()) {
            throw LiveUpdatesError.InvalidConfiguration(
                "Mock provider requires non-empty 'appId' in config",
                null
            )
        }

        // Extract optional parameters
        val channel = configData["channel"] as? String ?: "production"
        val shouldFail = configData["shouldFail"] as? Boolean ?: false
        val failureDetails = configData["failureDetails"] as? String ?: "Mock sync failed"
        val didUpdate = configData["didUpdate"] as? Boolean ?: false

        // For testing, create a mock directory path
        // In a real scenario, this would point to actual app assets
        val latestAppDir = File(context.filesDir, "mock_live_updates/$appId/$channel")

        Log.d(
            TAG,
            "Creating MockLiveUpdatesManager for appId: $appId, channel: $channel, shouldFail: $shouldFail"
        )

        return MockLiveUpdatesManager(
            appId = appId,
            channel = channel,
            latestAppDir = latestAppDir,
            shouldFail = shouldFail,
            failureDetails = failureDetails,
            didUpdate = didUpdate
        )
    }
}
