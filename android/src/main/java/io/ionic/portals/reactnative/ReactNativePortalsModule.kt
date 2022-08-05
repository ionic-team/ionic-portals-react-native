package io.ionic.portals.reactnative

import android.util.Log
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
import com.getcapacitor.CapConfig
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import io.ionic.liveupdates.LiveUpdate
import io.ionic.portals.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

internal object RNPortalManager {
    private val manager = PortalManager
    internal var indexMap: MutableMap<String, String> = mutableMapOf()
    lateinit var reactApplicationContext: ReactApplicationContext

    fun register(key: String) = manager.register(key)
    fun addPortal(map: ReadableMap) {
        val name = map.getString("name") ?: return
        val portalBuilder = PortalBuilder(name)

        map.getString("startDir")
            ?.let(portalBuilder::setStartDir)

        map.getMap("initialContext")
            ?.toHashMap()
            ?.let(portalBuilder::setInitialContext)

        map.getString("index")
            ?.let { indexMap[name] = "/$it" }

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

internal class PortalManagerModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {
    override fun getName() = "IONPortalManager"

    init {
        RNPortalManager.reactApplicationContext = reactContext
    }

    @ReactMethod
    fun register(key: String) {
        RNPortalManager.register(key)
    }

    @ReactMethod
    fun addPortal(map: ReadableMap) {
        RNPortalManager.addPortal(map)
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

private data class PortalViewState(
    var fragment: PortalFragment?,
    var portal: Portal?,
    var initialContext: HashMap<String, Any>?
)

internal class PortalViewManager(private val context: ReactApplicationContext) :
    ViewGroupManager<FrameLayout>() {
    private val createId = 1
    private val fragmentMap = mutableMapOf<Int, PortalViewState>()

    @ReactProp(name = "portal")
    fun setPortal(viewGroup: ViewGroup, portal: ReadableMap) {
        val name = portal.getString("name") ?: return
        when (val viewState = fragmentMap[viewGroup.id]) {
           null -> fragmentMap[viewGroup.id] = PortalViewState(
               fragment = null,
               portal = PortalManager.getPortal(name),
               initialContext = portal.getMap("initialContext")?.toHashMap()
           )
        }
    }

    override fun getName() = "AndroidPortalView"

    override fun createViewInstance(reactContext: ThemedReactContext): FrameLayout {
        return FrameLayout(reactContext)
    }

    override fun getCommandsMap(): MutableMap<String, Int> {
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
        val viewState = fragmentMap[viewId] ?: return
        val portal = viewState.portal ?: return

        val parentView = root.findViewById<ViewGroup>(viewId)
        setupLayout(parentView)

        val portalFragment = PortalFragment(portal)

        val configBuilder = CapConfig.Builder(context)
            .setInitialFocus(false)

        RNPortalManager.indexMap[portal.name]
            ?.let(configBuilder::setStartPath)

        portalFragment.setConfig(configBuilder.create())

        viewState.initialContext?.let(portalFragment::setInitialContext)

        viewState.fragment = portalFragment

        val fragmentActivity = context.currentActivity as? FragmentActivity ?: return
        fragmentActivity.supportFragmentManager
            .beginTransaction()
            .replace(viewId, portalFragment, "$viewId")
            .commit()
    }

    override fun onDropViewInstance(view: FrameLayout) {
        super.onDropViewInstance(view)
        val viewState = fragmentMap[view.id] ?: return

        try {
            viewState.fragment
                ?.parentFragmentManager
                ?.beginTransaction()
                ?.remove(viewState.fragment!!)
                ?.commit()
        } catch (e: IllegalStateException) {
            Log.i("io.ionic.portals.rn", "Parent fragment manager not available")
        }

        fragmentMap.remove(view.id)
    }

    private fun setupLayout(view: View) {
        Choreographer.getInstance().postFrameCallback(object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                layoutPortal(view)
                view.viewTreeObserver.dispatchOnGlobalLayout()
                Choreographer.getInstance().postFrameCallback(this)
            }
        })
    }

    private fun layoutPortal(view: View) {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(view.measuredWidth, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(view.measuredHeight, View.MeasureSpec.EXACTLY)
        )

        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }
}
