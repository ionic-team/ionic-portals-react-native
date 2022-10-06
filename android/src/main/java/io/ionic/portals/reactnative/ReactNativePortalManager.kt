package io.ionic.portals.reactnative

import com.facebook.react.bridge.*
import com.getcapacitor.Plugin
import io.ionic.liveupdates.LiveUpdate
import io.ionic.liveupdates.LiveUpdateManager
import io.ionic.portals.Portal
import io.ionic.portals.PortalBuilder
import io.ionic.portals.PortalManager
import io.ionic.portals.PortalsPlugin
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException

internal object RNPortalManager {
    private val manager = PortalManager
    internal val indexMap: MutableMap<String, String> = mutableMapOf()
    private lateinit var reactApplicationContext: ReactApplicationContext
    private var usesSecureLiveUpdates = false

    fun register(key: String) = manager.register(key)
    fun addPortal(map: ReadableMap): Portal? {
        val name = map.getString("name") ?: return null
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
                Pair(LiveUpdate(appId, channel, usesSecureLiveUpdates), syncOnAdd)
            }
            ?.let { (liveUpdate, updateOnAppLoad) ->
                portalBuilder.setLiveUpdateConfig(
                    context = reactApplicationContext,
                    liveUpdateConfig = liveUpdate,
                    updateOnAppLoad = updateOnAppLoad
                )
            }

        val portal = portalBuilder
            .addPlugin(PortalsPlugin::class.java)
            .create()

        manager.addPortal(portal)
        return portal
    }

    fun getPortal(name: String): Portal = manager.getPortal(name)

    fun enableSecureLiveUpdates(keyPath: String) {
        LiveUpdateManager.secureLiveUpdatePEM = keyPath
        usesSecureLiveUpdates = true
    }

    fun registerFromConfigIfAvailable(context: ReactApplicationContext) {
        reactApplicationContext = context
        val config = try {
            context.assets.open("portals.config.json")
                .bufferedReader()
                .use(BufferedReader::readText)
        } catch (e: IOException) {
            return
        }

        val configJson = try {
            JSONObject(config)
        } catch (e: JSONException) {
            throw Error("Portals config data is malformed. Aborting.", e)
        }

        val registrationKey =
            if (!configJson.isNull("registrationKey")) configJson.getString("registrationKey") else null
        registrationKey?.let(::register)

        val liveUpdatesKey =
            if (!configJson.isNull("liveUpdatesKey")) configJson.getString("liveUpdatesKey") else null
        liveUpdatesKey?.let {
            LiveUpdateManager.secureLiveUpdatePEM = it
            usesSecureLiveUpdates = true
        }

        val portalJsonArray = configJson.getJSONArray("portals")

        for (index in 0 until portalJsonArray.length()) {
            val portalJson = portalJsonArray.getJSONObject(index)
            addPortal(portalJson.toReactMap())
        }
    }
}

internal fun JSONObject.toReactMap(): ReadableMap =
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
