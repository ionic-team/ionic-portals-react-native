package io.ionic.portals.reactnative

import com.facebook.react.bridge.*
import com.getcapacitor.Plugin
import io.ionic.liveupdates.LiveUpdate
import io.ionic.liveupdates.LiveUpdateManager
import io.ionic.portals.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap


internal data class RNPortal(
    val builder: PortalBuilder,
    val index: String?,
    val plugins: List<PortalPlugin>,
    var onFCP: ((Long) -> Unit)? = null,
    var onTTFB: ((Long) -> Unit)? = null,
    var onFID: ((Long) -> Unit)? = null
)

internal data class PortalPlugin(val androidClassPath: String, val iosClassName: String) {
    companion object {
        const val iosClassNameKey = "iosClassName"
        const val androidClassPathKey = "androidClassPath"
        fun fromReadableMap(map: ReadableMap): PortalPlugin? {
            val androidClassPath = map.getString(androidClassPathKey) ?: return null
            val iosClassName = map.getString(iosClassNameKey) ?: return null
            return PortalPlugin(androidClassPath, iosClassName)
        }
    }

    fun toReadableMap(): ReadableMap {
        val map = WritableNativeMap()
        map.putString(iosClassNameKey, iosClassName)
        map.putString(androidClassPathKey, androidClassPath)
        return map
    }
}

internal object RNPortalManager {
    private val manager = PortalManager
    private val portals: ConcurrentHashMap<String, RNPortal> = ConcurrentHashMap()
    private lateinit var reactApplicationContext: ReactApplicationContext
    private var usesSecureLiveUpdates = false

    fun register(key: String) = manager.register(key)

    fun addPortal(map: ReadableMap): RNPortal? {
        val name = map.getString("name") ?: return null
        val portalBuilder = PortalBuilder(name)

        map.getString("startDir")
            ?.let(portalBuilder::setStartDir)

        map.getMap("initialContext")
            ?.toHashMap()
            ?.let(portalBuilder::setInitialContext)


        val plugins: List<PortalPlugin> = map.getArray("plugins")
            ?.let { rnArray ->
                val list = mutableListOf<PortalPlugin>()
                for (idx in 0 until rnArray.size()) {
                    rnArray.getMap(idx)
                        ?.let(PortalPlugin.Companion::fromReadableMap)
                        ?.let(list::add)
                }
                return@let list
            } ?: listOf()

        plugins.map {
            Class.forName(it.androidClassPath)
                .asSubclass(Plugin::class.java)
        }
            .forEach(portalBuilder::addPlugin)

        val assetMaps: List<AssetMap> = map.getArray("assetMaps")
            ?.let { rnArray ->
                val list = mutableListOf<AssetMap>()

                for (idx in 0 until rnArray.size()) {
                    rnArray.getMap(idx)
                        ?.let assetMap@{ map ->
                            val name = map.getString("name") ?: return@assetMap null
                            AssetMap(
                                name = name,
                                virtualPath = map.getString("virtualPath") ?: "/$name",
                                path = map.getString("startDir") ?: ""
                            )
                        }
                        ?.let(list::add)
                }

                return@let list
            } ?: listOf()

        assetMaps.forEach(portalBuilder::addAssetMap)

        map.getMap("liveUpdate")
            ?.let { readableMap ->
                val appId = readableMap.getString("appId") ?: return@let null
                val channel = readableMap.getString("channel") ?: return@let null
                val syncOnAdd = readableMap.getBoolean("syncOnAdd")
                Pair(LiveUpdate(appId, channel, RNPortalManager.usesSecureLiveUpdates), syncOnAdd)
            }
            ?.let { (liveUpdate, updateOnAppLoad) ->
                portalBuilder.setLiveUpdateConfig(
                    context = RNPortalManager.reactApplicationContext,
                    liveUpdateConfig = liveUpdate,
                    updateOnAppLoad = updateOnAppLoad
                )
            }

         portalBuilder
            .addPlugin(PortalsPlugin::class.java)

        val rnPortal = RNPortal(
            builder = portalBuilder,
            index = map.getString("index"),
            plugins = plugins
        )

        portals[name] = rnPortal
        return rnPortal
    }

    fun getPortal(name: String): RNPortal = portals[name]
        ?: throw IllegalStateException("Portal with portalId $name not found in RNPortalManager")

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
                null, JSONObject.NULL -> map.putNull(key)
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
                null, JSONObject.NULL -> array.pushNull()
                else -> array.pushString(value.toString())
            }
        } catch (_: JSONException) {
        }

        return@fold array
    }
