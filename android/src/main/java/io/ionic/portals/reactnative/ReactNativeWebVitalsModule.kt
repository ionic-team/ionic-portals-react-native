package io.ionic.portals.reactnative

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

internal class PortalWebVitalsModule(reactContext: ReactContext): ReactContextBaseJavaModule(reactContext) {
    override fun getName() = "IONPortalsWebVitals"

    @ReactMethod
    fun registerOnFirstContentfulPaint(portalName: String, promise: Promise) {

    }

    @ReactMethod
    fun addListener(eventName: String) {
    }

    @ReactMethod
    fun removeListeners(count: Int) {
    }
}
