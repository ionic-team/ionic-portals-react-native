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
import io.ionic.portals.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class PortalManagerModule(reactContext: ReactApplicationContext) :
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

class PortalsPubSubModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {
    override fun getName(): String {
        return "IONPortalsPubSub"
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

fun JSONObject.toReactMap(): ReadableMap =
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

fun JSONArray.toReactArray(): ReadableArray =
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

fun ReadableMap.toJSObject(): JSObject = JSObject.fromJSONObject(JSONObject(toHashMap()))

class PortalViewManager(private val context: ReactApplicationContext) :
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
