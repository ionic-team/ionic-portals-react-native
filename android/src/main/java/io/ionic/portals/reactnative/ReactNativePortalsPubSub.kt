package io.ionic.portals.reactnative

import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.getcapacitor.JSObject
import io.ionic.portals.PortalsPubSub
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

internal class PortalsPubSubModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {
    override fun getName() = "IONPortalPubSub"
    private val subscriptionRefs = ConcurrentHashMap<String, Int>()

    @ReactMethod
    fun publish(topic: String, data: ReadableMap) {
        PortalsPubSub.shared.publish(topic, data.toJSObject())
    }

    @ReactMethod
    fun addListener(eventName: String) {
        val topic = eventName.removePrefix("PortalsSubscription:")
        if (subscriptionRefs[topic] != null) { return }
        val ref = PortalsPubSub.shared.subscribe(topic) { result ->
            reactApplicationContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit("PortalsSubscription:$eventName", result.toJSObject().toReactMap())
        }

        subscriptionRefs[eventName] = ref
    }

    @ReactMethod
    fun removeListeners(count: Int) {
    }
}

private fun ReadableMap.toJSObject(): JSObject = JSObject.fromJSONObject(JSONObject(toHashMap()))
