package io.ionic.portals.reactnative

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.modules.core.DeviceEventManagerModule

internal class PortalWebVitalsModule(reactContext: ReactApplicationContext): ReactContextBaseJavaModule(reactContext) {
    override fun getName() = "IONPortalsWebVitals"

    @ReactMethod
    fun registerOnFirstContentfulPaint(portalName: String, promise: Promise) {
        RNPortalManager.getPortal(portalName).onFCP = { duration ->
            reactApplicationContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit("vitals:fcp", mapOf("duration" to duration, "portalName" to portalName).toReadableMap())
        }

        promise.resolve(null)
    }

    @ReactMethod
    fun registerOnFirstInputDelay(portalName: String, promise: Promise) {
        RNPortalManager.getPortal(portalName).onFID = { duration ->
            reactApplicationContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit("vitals:fid", mapOf("duration" to duration, "portalName" to portalName).toReadableMap())
        }

        promise.resolve(null)
    }

    @ReactMethod
    fun registerOnTimeToFirstByte(portalName: String, promise: Promise) {
        RNPortalManager.getPortal(portalName).onTTFB = { duration ->
            reactApplicationContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit("vitals:ttfb", mapOf("duration" to duration, "portalName" to portalName).toReadableMap())
        }

        promise.resolve(null)
    }

    @ReactMethod
    fun addListener(eventName: String) {
    }

    @ReactMethod
    fun removeListeners(count: Int) {
    }
}
