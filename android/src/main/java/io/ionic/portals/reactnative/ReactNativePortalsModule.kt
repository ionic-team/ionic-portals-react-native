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
import com.getcapacitor.Plugin
import io.ionic.liveupdates.LiveUpdate
import io.ionic.portals.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

internal class PortalManagerModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {
    override fun getName() = "IONPortalManager"

    @ReactMethod
    fun register(key: String) {
        PortalManager.register(key)
    }

    @ReactMethod
    fun addPortal(map: ReadableMap) {
        val name = map.getString("name") ?: return
        val portalBuilder = PortalBuilder(name)

        map.getString("startDir")
            ?.let(portalBuilder::setStartDir)

        map.getMap("initialContext")
            ?.toHashMap()
            ?.let(portalBuilder::setInitialContext)

        map.getArray("androidPlugins")
            ?.toArrayList()
            ?.mapNotNull { it as? String }
            ?.map {
                Class.forName(it)
                    .asSubclass(Plugin::class.java)
            }
            ?.forEach(portalBuilder::addPlugin)

        map.getMap("liveUpdate")
            ?.let { readableMap ->
                val appId = readableMap.getString("appId") ?: return@let null
                val channel = readableMap.getString("channel") ?: return@let null
                val syncOnAdd = readableMap.getBoolean("syncOnAdd")
                Pair(LiveUpdate(appId, channel), syncOnAdd)
            }
            ?.let { pair ->
                portalBuilder.setLiveUpdateConfig(
                    context = reactApplicationContext,
                    liveUpdateConfig = pair.first,
                    updateOnAppLoad = pair.second
                )
            }

        val portal = portalBuilder
            .addPlugin(PortalsPlugin::class.java)
            .create()

        PortalManager.addPortal(portal)
    }
}

internal class PortalsPubSubModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {
    override fun getName() = "IONPortalPubSub"

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
    private var initialContext: HashMap<String, Any>? = null

    @ReactProp(name = "name")
    fun setPortal(viewGroup: ViewGroup, portalName: String) {
        portal = PortalManager.getPortal(portalName)
    }

    @ReactProp(name = "initialContext")
    fun setInitialContext(viewGroup: ViewGroup, initialContext: ReadableMap) {
        this.initialContext = initialContext.toHashMap()
    }

    override fun getName() = "AndroidPortalView"

    override fun createViewInstance(reactContext: ThemedReactContext): FrameLayout {
        return FrameLayout(reactContext)
    }

    override fun getCommandsMap(): MutableMap<String, Int>? {
        return mutableMapOf("create" to createId)
    }

    override fun receiveCommand(root: FrameLayout, commandId: String?, args: ReadableArray?) {
        super.receiveCommand(root, commandId, args)
        val viewId = args?.getInt(0) ?: return

        @Suppress("NAME_SHADOWING")
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
        initialContext?.let(portalFragment::setInitialContext)

        val fragmentActivity = context.currentActivity as? FragmentActivity ?: return
        fragmentActivity.supportFragmentManager
            .beginTransaction()
            .replace(viewId, portalFragment, "$viewId")
            .commit()

        this.portal = null
        this.initialContext = null
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
