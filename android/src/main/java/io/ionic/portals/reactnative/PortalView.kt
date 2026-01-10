package io.ionic.portals.reactnative

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Choreographer
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactProp
import io.ionic.portals.PortalFragment
import io.ionic.portals.WebVitals

private data class PortalViewState(
    var fragment: PortalFragment?,
    var portal: RNPortal?,
    var initialContext: HashMap<String, Any?>?,
    var webContentsDebuggingEnabled: Boolean?
)

internal class PortalViewManager(private val context: ReactApplicationContext) :
    ViewGroupManager<FrameLayout>() {
    private val createId = 1
    private val fragmentMap = mutableMapOf<Int, PortalViewState>()

    @ReactProp(name = "portal")
    fun setPortal(viewGroup: ViewGroup, portal: ReadableMap) {
        val name = portal.getString("name") ?: return

        // React Native 0.77 changed ReadableMap.toHashMap() from HashMap<String, Any> to HashMap<String, Any?>
        // Casting is safe and keeps old versions compatible
        val initialContext = portal.getMap("initialContext")?.toHashMap() as HashMap<String, Any?>?

        val state = fragmentMap.getOrPut(viewGroup.id) {
            PortalViewState(null, null, null, null)
        }
        state.portal = RNPortalManager.createPortal(portal)
        state.initialContext = initialContext
    }

    @ReactProp(name = "webContentsDebuggingEnabled")
    fun setWebContentsDebuggingEnabled(viewGroup: ViewGroup, webContentsDebuggingEnabled: Boolean) {
        val state = fragmentMap.getOrPut(viewGroup.id) {
            PortalViewState(null, null, null, null)
        }
        state.webContentsDebuggingEnabled = webContentsDebuggingEnabled
    }

    override fun getName() = "AndroidPortalView"

    override fun createViewInstance(reactContext: ThemedReactContext): FrameLayout {
        return FrameLayout(reactContext)
    }

    override fun getCommandsMap(): MutableMap<String, Int> {
        return mutableMapOf("create" to createId)
    }

    @Deprecated("Deprecated, but using to support New Architecture")
    override fun receiveCommand(root: FrameLayout, commandId: Int, args: ReadableArray?) {
        super.receiveCommand(root, commandId, args)
        val viewId = args?.getInt(0) ?: return

        when (commandId) {
            createId -> createFragment(root, viewId)
        }
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
        val rnPortal = viewState.portal ?: return

        val parentView = root.findViewById<ViewGroup>(viewId)
        setupLayout(parentView)

        val portal = rnPortal.builder.create()
        val vitals: List<String>? = rnPortal.vitals

        if (vitals != null) {
            val vitalsPlugin = WebVitals { name, metric, duration ->
                val stringMetric = metric.toString().lowercase()
                if (vitals.contains(stringMetric)) {
                    context
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                        .emit("vitals:$stringMetric", mapOf("duration" to duration, "portalName" to name).toReadableMap())
                }
            }
            portal.addPluginInstance(vitalsPlugin)
        }

        val portalFragment = PortalFragment(portal)
        viewState.initialContext?.let(portalFragment::setInitialContext)
        viewState.fragment = portalFragment

        portalFragment.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_RESUME) {
                    source.lifecycle.removeObserver(this)

                    viewState.webContentsDebuggingEnabled?.let { enabled ->
                        // Post to the next main loop iteration to avoid racing with WebView initialization
                        Handler(Looper.getMainLooper()).post {
                            WebView.setWebContentsDebuggingEnabled(enabled)
                        }
                    }
                }
            }
        })

        val activity = context.currentActivity as? FragmentActivity ?: return
        activity.supportFragmentManager
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

    private fun setupLayout(view: ViewGroup) {
        Choreographer.getInstance().postFrameCallback(object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                layoutPortal(view)
                view.viewTreeObserver.dispatchOnGlobalLayout()
                Choreographer.getInstance().postFrameCallback(this)
            }
        })
    }

    private fun layoutPortal(view: ViewGroup) {
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
