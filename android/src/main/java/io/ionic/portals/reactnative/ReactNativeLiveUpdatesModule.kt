package io.ionic.portals.reactnative

import android.content.Context
import com.facebook.react.bridge.*
import io.ionic.liveupdates.LiveUpdate
import io.ionic.liveupdates.LiveUpdateManager
import io.ionic.liveupdates.data.model.FailResult
import io.ionic.liveupdates.data.model.Snapshot
import io.ionic.liveupdates.data.model.SyncResult
import io.ionic.liveupdates.network.SyncCallback
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.fold
import java.util.concurrent.Executors

internal object LiveUpdatesModule {
    private val liveUpdateScope = CoroutineScope(
        Executors.newFixedThreadPool(4)
            .asCoroutineDispatcher()
    )

    fun syncOne(appId: String, context: Context, promise: Promise) {
        LiveUpdateManager.sync(
            context = context,
            appId = appId,
            callback = object : SyncCallback {
                override fun onAppComplete(syncResult: SyncResult) {
                    promise.resolve(syncResult.toReadableMap())

                }

                override fun onAppComplete(failResult: FailResult) {
                    promise.resolve(failResult.toReadableMap())
                }

                override fun onSyncComplete() {
                    // do nothing
                }
            }
        )
    }

    fun syncSome(appIds: ReadableArray, context: Context, promise: Promise) {
        @Suppress("NAME_SHADOWING")
        val appIds = (0 until appIds.size())
            .mapNotNull(appIds::getString)
            .toTypedArray()

        sync(appIds, context, promise)
    }

    fun syncAll(context: Context, promise: Promise) {
        sync(emptyArray(), context, promise)
    }

    private fun sync(appIds: Array<String>, context: Context, promise: Promise) {
        liveUpdateScope.launch {
            val results = callbackFlow {
                LiveUpdateManager.sync(
                    context = context,
                    appIds = appIds,
                    callback = object : SyncCallback {
                        override fun onAppComplete(syncResult: SyncResult) {
                            trySend(LiveUpdateSuccess(syncResult))
                        }

                        override fun onAppComplete(failResult: FailResult) {
                            trySend(LiveUpdateFailure(failResult))
                        }

                        override fun onSyncComplete() {
                            close()
                        }
                    }
                )

                awaitClose { cancel() }
            }

            val syncResults = results.fold(SyncResults.empty()) { syncResults, result ->
                when (result) {
                    is LiveUpdateSuccess -> syncResults.results.add(result)
                    is LiveUpdateFailure -> syncResults.errors.add(result)
                }

                return@fold syncResults
            }

            promise.resolve(syncResults.asReadableMap)
        }
    }
}

private sealed class LiveUpdateResult
private data class LiveUpdateFailure(val failure: FailResult) : LiveUpdateResult()
private data class LiveUpdateSuccess(val success: SyncResult): LiveUpdateResult()

private data class SyncResults(
    val results: MutableList<LiveUpdateSuccess>,
    val errors: MutableList<LiveUpdateFailure>
) {
    val asReadableMap: ReadableMap
        get() {
            val map = WritableNativeMap()

            val syncResultsArray = WritableNativeArray()
            results.forEach { syncResultsArray.pushMap(it.success.toReadableMap()) }
            map.putArray("results", syncResultsArray)

            val errorsArray = WritableNativeArray()
            errors.forEach { errorsArray.pushMap(it.failure.toReadableMap()) }
            map.putArray("errors", errorsArray)

            return map
        }

    companion object {
        fun empty() = SyncResults(mutableListOf(), mutableListOf())
    }
}

fun LiveUpdate.toReadableMap(): ReadableMap {
    val map = WritableNativeMap()
    map.putString("appId", appId)
    map.putString("channel", channelName)
    return map
}

fun Snapshot.toReadableMap(): ReadableMap {
    val map = WritableNativeMap()
    map.putString("id", id)
    map.putString("buildId", buildId)
    return map
}

fun SyncResult.toReadableMap(): ReadableMap {
    val map = WritableNativeMap()
    map.putMap("liveUpdate", liveUpdate.toReadableMap())
    map.putMap("snapshot", snapshot?.let(Snapshot::toReadableMap))
    map.putString("source", source.name.lowercase())
    map.putBoolean("activeApplicationPathChanged", latestAppDirectoryChanged)
    return map
}

fun FailResult.toReadableMap(): ReadableMap {
    val map = WritableNativeMap()
    map.putMap("liveUpdate", liveUpdate.toReadableMap())
    map.putString("failStep", failStep.name)
    map.putString("message", failMsg ?: "Sync failed for unknown reason.")
    return map
}
