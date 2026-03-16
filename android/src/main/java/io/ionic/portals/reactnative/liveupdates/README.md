# Mock Live Updates Provider

A mock implementation of the Live Updates Provider API for testing in React Native Portals.

## Quick Start

### 1. Initialize the Provider

In your `MainApplication.kt`:

```kotlin
import io.ionic.portals.reactnative.liveupdates.MockLiveUpdatesProvider

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize mock provider
        MockLiveUpdatesProvider.initialize()
    }
}
```

### 2. Use the Provider

```kotlin
import io.ionic.liveupdatesprovider.LiveUpdatesRegistry
import io.ionic.liveupdatesprovider.models.ProviderConfig

val config = ProviderConfig(mapOf(
    "appId" to "my-app-id",
    "channel" to "production",
    "didUpdate" to true,        // Simulate update available
    "shouldFail" to false        // Don't simulate failure
))

val provider = LiveUpdatesRegistry.require("ionic-mock")
val manager = provider.createManager(context, config)

manager.sync(object : SyncCallback {
    override fun onComplete(result: SyncResult) {
        Log.d("MyApp", "Sync complete: didUpdate=${result.didUpdate}")
    }

    override fun onError(error: LiveUpdatesError.SyncFailed) {
        Log.e("MyApp", "Sync failed: ${error.message}")
    }
})
```

### 3. Run Example Tests

```kotlin
import io.ionic.portals.reactnative.liveupdates.MockProviderExample

// Run all test scenarios
MockProviderExample.runAllTests(context)
```

## Files

- **MockLiveUpdatesProvider.kt**: Main provider implementation
- **MockLiveUpdatesManager.kt**: Manager that handles sync operations
- **MockProviderExample.kt**: Example usage and test scenarios

## Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `appId` | String | *required* | App identifier |
| `channel` | String | "production" | Channel name |
| `didUpdate` | Boolean | false | Simulate update available |
| `shouldFail` | Boolean | false | Simulate sync failure |
| `failureDetails` | String | "Mock sync failed" | Error message |

## Testing

View logs with:
```bash
adb logcat -s MockLiveUpdatesProvider MockLiveUpdatesManager MockProviderExample
```

## See Also

- [MOCK_LIVE_UPDATES.md](../../../../MOCK_LIVE_UPDATES.md) - Full documentation
- [Live Updates Provider SDK](https://github.com/ionic-team/live-updates-provider-sdk)
