package io.ionic.portals.reactnative

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

internal class PortalWebVitalsModule(reactContext: ReactApplicationContext): ReactContextBaseJavaModule(reactContext) {
    override fun getName() = "IONPortalsWebVitals"

    private fun registerVital(portalName: String, vitalName: String) {
        val portal = RNPortalManager.getPortal(portalName) ?: return
        if (portal.vitals == null) {
            portal.vitals = mutableListOf()
        }
        portal.vitals?.add(vitalName)
    }

    @ReactMethod
    fun registerOnFirstContentfulPaint(portalName: String, promise: Promise) {
        registerVital(portalName,"fcp")
        promise.resolve(null)
    }

    @ReactMethod
    fun registerOnFirstInputDelay(portalName: String, promise: Promise) {
        registerVital(portalName,"fid")
        promise.resolve(null)
    }

    @ReactMethod
    fun registerOnTimeToFirstByte(portalName: String, promise: Promise) {
        registerVital(portalName,"ttfb")
        promise.resolve(null)
    }

    @ReactMethod
    fun addListener(eventName: String) {
    }

    @ReactMethod
    fun removeListeners(count: Int) {
    }
}
