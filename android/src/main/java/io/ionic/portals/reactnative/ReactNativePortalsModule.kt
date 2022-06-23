package io.ionic.portals.reactnative

import android.view.Choreographer
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactProp
import com.getcapacitor.JSObject
import io.ionic.liveupdates.LiveUpdate
import io.ionic.liveupdates.LiveUpdateManager
import io.ionic.liveupdates.network.FailStep
import io.ionic.liveupdates.network.SyncCallback
import io.ionic.portals.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.toCollection
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.Executors
import kotlin.coroutines.coroutineContext

internal class PortalManagerModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {
    override fun getName(): String {
        return "IONPortalManager"
    }

    @ReactMethod
    fun register(key: String) {
        PortalManager.register(key)
    }

    @ReactMethod
    fun addPortal(map: ReadableMap) {
        map.getString("name")?.let { name ->
            val startDir = map.getString("startDir")
            val initialContext = map.getMap("initialContext")?.toHashMap()

            val portalBuilder = PortalBuilder(name)

            if (startDir != null) {
                portalBuilder.setStartDir(startDir)
            }

            if (initialContext != null) {
                portalBuilder.setInitialContext(initialContext)
            }

            // TODO: We need to figure out if we can register plugins from javascript
            val portal = portalBuilder
                .addPlugin(PortalsPlugin::class.java)
                .create()

            PortalManager.addPortal(portal)
        }
    }
}

internal class PortalsPubSubModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {
    override fun getName(): String {
        return "IONPortalPubSub"
    }

    @ReactMethod
    fun subscribe(topic: String, promise: Promise) {
        val reference = PortalsPlugin.subscribe(topic) { result ->
            reactApplicationContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit("PortalsSubscription", result.toJSObject().toReactMap())
        }

        promise.resolve(reference)
    }

    @ReactMethod
    fun publish(topic: String, data: ReadableMap) {
        PortalsPlugin.publish(topic, data.toJSObject())
    }

    @ReactMethod
    fun unsubscribe(topic: String, reference: Int) {
        PortalsPlugin.unsubscribe(topic, reference)
    }

    // These are required to be an EventEmitter in javascript

    @ReactMethod
    fun addListener(eventName: String) {
    }

    @ReactMethod
    fun removeListeners(count: Int) {
    }
}

private fun JSONObject.toReactMap(): ReadableMap =
    keys().asSequence().fold(WritableNativeMap()) { map, key ->
        try {
            when (val value = get(key)) {
                is JSONObject -> map.putMap(key, value.toReactMap())
                is JSONArray -> map.putArray(key, value.toReactArray())
                is Boolean -> map.putBoolean(key, value)
                is Int -> map.putInt(key, value)
                is Double -> map.putDouble(key, value)
                is String -> map.putString(key, value)
                null -> map.putNull(key)
                else -> map.putString(key, value.toString())
            }
        } catch (_: JSONException) {
        }

        return@fold map
    }

private fun JSONArray.toReactArray(): ReadableArray =
    (0 until length()).fold(WritableNativeArray()) { array, index ->
        try {
            when (val value = get(index)) {
                is JSONObject -> array.pushMap(value.toReactMap())
                is JSONArray -> array.pushArray(value.toReactArray())
                is Boolean -> array.pushBoolean(value)
                is Int -> array.pushInt(value)
                is Double -> array.pushDouble(value)
                is String -> array.pushString(value)
                null -> array.pushNull()
                else -> array.pushString(value.toString())
            }
        } catch (_: JSONException) {
        }

        return@fold array
    }

private fun ReadableMap.toJSObject(): JSObject = JSObject.fromJSONObject(JSONObject(toHashMap()))

internal class PortalViewManager(private val context: ReactApplicationContext) :
    ViewGroupManager<FrameLayout>() {
    private val createId = 1
    private var portal: Portal? = null

    @ReactProp(name = "name")
    fun setPortal(viewGroup: ViewGroup, portalName: String) {
        portal = PortalManager.getPortal(portalName)
    }

    @ReactProp(name = "initialContext")
    fun setInitialContext(viewGroup: ViewGroup, initialContext: ReadableMap) {
        portal?.setInitialContext(initialContext.toHashMap())
    }

    override fun getName(): String {
        return "AndroidPortalView"
    }

    override fun createViewInstance(reactContext: ThemedReactContext): FrameLayout {
        return FrameLayout(reactContext)
    }

    override fun getCommandsMap(): MutableMap<String, Int>? {
        return mutableMapOf("create" to createId)
    }

    override fun receiveCommand(root: FrameLayout, commandId: String?, args: ReadableArray?) {
        super.receiveCommand(root, commandId, args)
        val viewId = args?.getInt(0) ?: return
        val commandId = commandId?.toIntOrNull() ?: return

        when (commandId) {
            createId -> createFragment(root, viewId)
        }
    }

    private fun createFragment(root: FrameLayout, viewId: Int) {
        val portal = portal ?: return
        val parentView = root.findViewById<ViewGroup>(viewId)
        setupLayout(parentView)

        val portalFragment = PortalFragment(portal)
        val fragmentActivity = context.currentActivity as? FragmentActivity ?: return
        fragmentActivity.supportFragmentManager
            .beginTransaction()
            .replace(viewId, portalFragment, "$viewId")
            .commit()
    }

    private fun setupLayout(view: ViewGroup) {
        Choreographer.getInstance().postFrameCallback {
            layoutChildren(view)
            view.viewTreeObserver.dispatchOnGlobalLayout()
        }
    }

    private fun layoutChildren(view: ViewGroup) {
        for (i in 0 until view.childCount) {
            val child = view.getChildAt(i)

            child.measure(
                View.MeasureSpec.makeMeasureSpec(view.measuredWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(view.measuredHeight, View.MeasureSpec.EXACTLY)
            )

            child.layout(0, 0, child.measuredWidth, child.measuredHeight)
        }
    }
}

fun LiveUpdate.toReadableMap(): ReadableMap {
    val map = WritableNativeMap()
    map.putString("appId", appId)
    map.putString("channel", channelName)
    return map
}

private sealed class LiveUpdateResult()
private data class LiveUpdateError(val appId: String, val failStep: String, val failMsg: String): LiveUpdateResult() {
   val asReadableMap: ReadableMap
    get() {
        val map = WritableNativeMap()
        map.putString("appId", appId)
        map.putString("failStep", failStep)
        map.putString("message", failMsg)
        return map
    }
}
private data class LiveUpdateSuccess(val liveUpdate: LiveUpdate): LiveUpdateResult()

private data class SyncResults(var liveUpdates: MutableList<LiveUpdate>, var errors: MutableList<LiveUpdateError>) {
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
}

internal class LiveUpdatesModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    init {
        LiveUpdateManager.initialize(reactContext)
    }

    override fun getName() = "IONLiveUpdatesManager"

    private val scope = CoroutineScope(Executors.newFixedThreadPool(4).asCoroutineDispatcher())

    private fun callbackToMap(liveUpdate: LiveUpdate, failStep: FailStep?, failMsg: String?): ReadableMap =
        if (failStep != null) {
            val map = WritableNativeMap()
            map.putString("appId", liveUpdate.appId)
            map.putString("failStep", failStep.name)
            map.putString("message", failMsg ?: "Sync failed for unknown reason")
            map
        } else {
            liveUpdate.toReadableMap()
        }

    private fun callbackToResult(liveUpdate: LiveUpdate, failStep: FailStep?, failMsg: String?): LiveUpdateResult =
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
            callback = object: SyncCallback {
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
        val appIds = (0 until appIds.size())
            .mapNotNull(appIds::getString)
            .toTypedArray()

        scope.launch {
            val flow = callbackFlow<LiveUpdateResult> {
                LiveUpdateManager.sync(
                    context = reactApplicationContext,
                    appIds = appIds,
                    callback = object: SyncCallback {
                        override fun onAppComplete(
                            liveUpdate: LiveUpdate,
                            failStep: FailStep?,
                            failMsg: String?
                        ) {
                            trySend(callbackToResult(liveUpdate, failStep, failMsg))
                        }

                        override fun onSyncComplete() {
                            cancel()
                        }
                    }
                )
            }

            val syncResults = SyncResults(mutableListOf(), mutableListOf())

            flow.toCollection(mutableListOf())
                .forEach {
                    when(it) {
                        is LiveUpdateSuccess -> syncResults.liveUpdates.add(it.liveUpdate)
                        is LiveUpdateError -> syncResults.errors.add(it)
                    }
                }

            promise.resolve(syncResults.asReadableMap)
        }
    }

    @ReactMethod
    fun syncAll(promise: Promise) {
        scope.launch {
           val flow = callbackFlow<LiveUpdateResult> {
               LiveUpdateManager.sync(
                   context = reactApplicationContext,
                   callback = object: SyncCallback {
                       override fun onAppComplete(
                           liveUpdate: LiveUpdate,
                           failStep: FailStep?,
                           failMsg: String?
                       ) {
                           trySend(callbackToResult(liveUpdate, failStep, failMsg))
                       }

                       override fun onSyncComplete() {
                           cancel()
                       }
                   }
               )
           }

            val syncResults = SyncResults(mutableListOf(), mutableListOf())

            flow.toCollection(mutableListOf())
                .forEach {
                    when(it) {
                        is LiveUpdateSuccess -> syncResults.liveUpdates.add(it.liveUpdate)
                        is LiveUpdateError -> syncResults.errors.add(it)
                    }
                }

            promise.resolve(syncResults.asReadableMap)
        }
    }
}
