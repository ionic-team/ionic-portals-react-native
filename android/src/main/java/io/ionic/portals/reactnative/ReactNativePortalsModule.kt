package io.ionic.portals.reactnative

import com.facebook.react.bridge.*
import io.ionic.portals.Portal

internal class PortalManagerModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {
    override fun getName() = "IONPortalsReactNative"

    init {
        RNPortalManager.registerFromConfigIfAvailable(reactContext)
    }

    @ReactMethod
    fun register(key: String, promise: Promise) {
        RNPortalManager.register(key)
        promise.resolve(null)
    }

    @ReactMethod
    fun addPortal(map: ReadableMap, promise: Promise) {
        val portal = RNPortalManager.addPortal(map)
        if (portal == null) {
            promise.reject(null, "Invalid Portal configuration.")
        } else {
            promise.resolve(portal.toReadableMap())
        }
    }

    @ReactMethod
    fun addPortals(array: ReadableArray, promise: Promise) {
        val portals = WritableNativeArray()

        for (i in 0 until array.size()) {
            val map = array.getMap(i) ?: continue
            val portal = RNPortalManager.addPortal(map) ?: continue
            portals.pushMap(portal.toReadableMap())
        }

        promise.resolve(portals)
    }

    @ReactMethod
    fun getPortal(name: String, promise: Promise) {
        try {
            val portal = RNPortalManager.getPortal(name)
            promise.resolve(portal.toReadableMap())
        } catch (e: IllegalStateException) {
            promise.reject(null, "Portal named $name not registered.")
        }
    }

    @ReactMethod
    fun enableSecureLiveUpdates(keyPath: String, promise: Promise) {
        RNPortalManager.enableSecureLiveUpdates(keyPath)
        promise.resolve(null)
    }

    @ReactMethod
    fun syncOne(appId: String, promise: Promise) {
        LiveUpdatesModule.syncOne(appId, reactApplicationContext, promise)
    }

    @ReactMethod
    fun syncSome(appIds: ReadableArray, promise: Promise) {
        LiveUpdatesModule.syncSome(appIds, reactApplicationContext, promise)
    }

    @ReactMethod
    fun syncAll(promise: Promise) {
        LiveUpdatesModule.syncAll(reactApplicationContext, promise)
    }
}

fun Map<*, *>.toReadableMap(): ReadableMap {
    return keys.fold(WritableNativeMap()) { map, key ->
        val key = key as String
        when (val value = get(key)) {
            is String -> map.putString(key, value)
            is Boolean -> map.putBoolean(key, value)
            is Int -> map.putInt(key, value)
            is Double -> map.putDouble(key, value)
            is Map<*, *> -> map.putMap(key, value.toReadableMap())
            is List<*> -> map.putArray(key, value.toReadableArray())
            null -> map.putNull(key)
            else -> map.putString(key, value.toString())
        }
        return@fold map
    }
}

fun List<*>.toReadableArray(): ReadableArray = fold(WritableNativeArray()) { array, value ->
    when (value) {
        is String -> array.pushString(value)
        is Boolean -> array.pushBoolean(value)
        is Int -> array.pushInt(value)
        is Double -> array.pushDouble(value)
        is PortalPlugin -> array.pushMap(value.toReadableMap())
        is Map<*, *> -> array.pushMap(value.toReadableMap())
        is List<*> -> array.pushArray(value.toReadableArray())
        null -> array.pushNull()
        else -> array.pushString(value.toString())
    }
    return@fold array
}

internal fun RNPortal.toReadableMap(): ReadableMap {
    val map = WritableNativeMap()
    val portal = builder.create()
    map.putString("name", portal.name)
    map.putString("startDir", portal.startDir)
    map.putArray("plugins", plugins.toReadableArray())
    index?.let { map.putString("index", it) }
    portal.initialContext?.let {
        if (it is Map<*, *>) map.putMap("initialContext", it.toReadableMap())
    }

    portal.liveUpdateConfig?.let { map.putMap("liveUpdate", it.toReadableMap()) }

    return map
}
