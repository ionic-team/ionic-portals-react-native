package io.ionic.portals.reactnative

import com.facebook.react.bridge.*
import io.ionic.liveupdates.LiveUpdate
import io.ionic.liveupdates.LiveUpdateManager
import io.ionic.liveupdates.network.FailStep
import io.ionic.liveupdates.network.SyncCallback
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.fold
import java.util.concurrent.Executors

internal class LiveUpdatesModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName() = "IONLiveUpdatesManager"

    private val liveUpdateScope = CoroutineScope(
        Executors.newFixedThreadPool(4)
            .asCoroutineDispatcher()
    )

    private fun callbackToMap(
        liveUpdate: LiveUpdate,
        failStep: FailStep?,
        failMsg: String?
    ): ReadableMap =
        if (failStep != null) {
            val map = WritableNativeMap()
            map.putString("appId", liveUpdate.appId)
            map.putString("failStep", failStep.name)
            map.putString("message", failMsg ?: "Sync failed for unknown reason")
            map
        } else {
            liveUpdate.toReadableMap()
        }

    private fun callbackToResult(
        liveUpdate: LiveUpdate,
        failStep: FailStep?,
        failMsg: String?
    ): LiveUpdateResult =
        if (failStep != null) {
            LiveUpdateError(
                appId = liveUpdate.appId,
                failStep = failStep.name,
                failMsg = failMsg ?: "Sync failed for unknown reason"
            )
        } else {
            LiveUpdateSuccess(liveUpdate)
        }


    @ReactMethod
    fun addLiveUpdate(map: ReadableMap) {
        val appId = map.getString("appId") ?: return
        val channel = map.getString("channel") ?: return

        LiveUpdateManager.addLiveUpdateInstance(
            context = reactApplicationContext,
            liveUpdate = LiveUpdate(appId, channel)
        )
    }

    @ReactMethod
    fun syncOne(appId: String, promise: Promise) {
        LiveUpdateManager.sync(
            context = reactApplicationContext,
            appId = appId,
            callback = object : SyncCallback {
                override fun onAppComplete(
                    liveUpdate: LiveUpdate,
                    failStep: FailStep?,
                    failMsg: String?
                ) {
                    val map = callbackToMap(liveUpdate, failStep, failMsg)
                    promise.resolve(map)
                }

                override fun onSyncComplete() {
                    // do nothing
                }
            }
        )
    }

    @ReactMethod
    fun syncSome(appIds: ReadableArray, promise: Promise) {
        @Suppress("NAME_SHADOWING")
        val appIds = (0 until appIds.size())
            .mapNotNull(appIds::getString)
            .toTypedArray()

        sync(appIds, promise)
    }

    @ReactMethod
    fun syncAll(promise: Promise) {
        sync(emptyArray(), promise)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun sync(appIds: Array<String>, promise: Promise) {
        liveUpdateScope.launch {
            val results = callbackFlow {
                LiveUpdateManager.sync(
                    context = reactApplicationContext,
                    appIds = appIds,
                    callback = object : SyncCallback {
                        override fun onAppComplete(
                            liveUpdate: LiveUpdate,
                            failStep: FailStep?,
                            failMsg: String?
                        ) {
                            trySend(callbackToResult(liveUpdate, failStep, failMsg))
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
                    is LiveUpdateSuccess -> syncResults.liveUpdates.add(result.liveUpdate)
                    is LiveUpdateError -> syncResults.errors.add(result)
                }

                return@fold syncResults
            }

            promise.resolve(syncResults.asReadableMap)
        }
    }
}

fun LiveUpdate.toReadableMap(): ReadableMap {
    val map = WritableNativeMap()
    map.putString("appId", appId)
    map.putString("channel", channelName)
    return map
}

private sealed class LiveUpdateResult

private data class LiveUpdateError(val appId: String, val failStep: String, val failMsg: String) :
    LiveUpdateResult() {
    val asReadableMap: ReadableMap
        get() {
            val map = WritableNativeMap()
            map.putString("appId", appId)
            map.putString("failStep", failStep)
            map.putString("message", failMsg)
            return map
        }
}

private data class LiveUpdateSuccess(val liveUpdate: LiveUpdate) : LiveUpdateResult()

private data class SyncResults(
    val liveUpdates: MutableList<LiveUpdate>,
    val errors: MutableList<LiveUpdateError>
) {
    val asReadableMap: ReadableMap
        get() {
            val map = WritableNativeMap()

            val liveUpdatesArray = WritableNativeArray()
            liveUpdates.forEach { liveUpdatesArray.pushMap(it.toReadableMap()) }
            map.putArray("liveUpdates", liveUpdatesArray)

            val errorsArray = WritableNativeArray()
            errors.forEach { errorsArray.pushMap(it.asReadableMap) }
            map.putArray("errors", errorsArray)

            return map
        }

    companion object {
        fun empty() = SyncResults(mutableListOf(), mutableListOf())
    }
}
