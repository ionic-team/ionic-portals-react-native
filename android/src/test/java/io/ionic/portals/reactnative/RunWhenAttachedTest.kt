package io.ionic.portals.reactnative

import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Regression tests for the container race behind
 * `IllegalArgumentException: No view found for id … for fragment PortalFragment`.
 *
 * They drive the real androidx [FragmentManager] under Robolectric with plain
 * [Fragment]s, which is enough: `FragmentStateManager.createView` resolves the
 * container by id before it asks the fragment for a view. `PortalFragment`
 * itself is out of scope here, it would boot a Capacitor bridge and a WebView.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RunWhenAttachedTest {
    private lateinit var activity: FragmentActivity
    private lateinit var container: FrameLayout

    private val fragmentManager: FragmentManager
        get() = activity.supportFragmentManager

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()
        container = FrameLayout(activity).apply { id = View.generateViewId() }
    }

    private fun attachContainer() = activity.setContentView(container)

    private fun detachContainer() = (container.parent as ViewGroup).removeView(container)

    private fun idleMainLooper() = shadowOf(Looper.getMainLooper()).idle()

    private fun commitFragmentNow() {
        fragmentManager
            .beginTransaction()
            .replace(container.id, Fragment())
            .commitNowAllowingStateLoss()
    }

    private fun fragmentInContainer(): Fragment? = fragmentManager.findFragmentById(container.id)

    /**
     * Control: this is the pattern the view manager used before the fix and the
     * production crash. It proves the harness reproduces the failure, so the
     * tests below mean something.
     */
    @Test
    fun asyncCommitThrowsWhenTheContainerWasRemovedBeforeItRan() {
        attachContainer()
        fragmentManager.beginTransaction().replace(container.id, Fragment()).commit()
        detachContainer()

        val thrown = runCatching { idleMainLooper() }.exceptionOrNull()

        assertNotNull("expected the pending add to throw", thrown)
        val messages = generateSequence(thrown) { it.cause }
            .mapNotNull { it.message }
            .joinToString(" | ")
        assertTrue(messages, messages.contains("No view found for id"))
    }

    @Test
    fun commitsOnceTheLooperRunsWhenTheContainerIsAttached() {
        attachContainer()

        runWhenAttached(container, isCurrent = { true }, action = ::commitFragmentNow)
        idleMainLooper()

        assertTrue(fragmentInContainer()?.isAdded == true)
    }

    @Test
    fun skipsTheCommitWhenTheViewWasDroppedBeforeItRan() {
        attachContainer()
        var actionRuns = 0

        runWhenAttached(container, isCurrent = { false }) {
            actionRuns++
            commitFragmentNow()
        }
        detachContainer()
        idleMainLooper()

        assertEquals(0, actionRuns)
        assertNull(fragmentInContainer())
    }

    @Test
    fun waitsForTheContainerToAttachBeforeCommitting() {
        // Not attached yet: mirrors a screen that is still animating in.
        runWhenAttached(container, isCurrent = { true }, action = ::commitFragmentNow)
        idleMainLooper()
        assertNull("must not commit before the container is in the window", fragmentInContainer())

        attachContainer()
        idleMainLooper()

        assertTrue(fragmentInContainer()?.isAdded == true)
    }

    @Test
    fun doesNotThrowWhenTheContainerIsDetachedWhileStillCurrent() {
        // Mirrors a screen detached natively while React still has the view mounted.
        attachContainer()
        runWhenAttached(container, isCurrent = { true }, action = ::commitFragmentNow)
        detachContainer()

        idleMainLooper()

        assertNull(fragmentInContainer())

        // ...and the commit still happens once the container comes back.
        attachContainer()
        idleMainLooper()
        assertTrue(fragmentInContainer()?.isAdded == true)
    }
}
