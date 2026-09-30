package io.ionic.portals.reactnative

import android.view.View

/**
 * Runs [action] on the main thread once [view] is attached to a window, provided
 * [isCurrent] still returns true at that point.
 *
 * The runnable is posted on the view: [View.post] queues it until the view is
 * attached and always dispatches it through the main handler, so [action] never
 * runs nested inside another FragmentManager transaction that may be executing
 * further up the stack. If the view is not attached when the runnable runs
 * (detached again in between), it re-posts itself and waits for the next
 * attach. If [isCurrent] returns false (the view was dropped), nothing runs.
 */
internal fun runWhenAttached(view: View, isCurrent: () -> Boolean, action: () -> Unit) {
    view.post(object : Runnable {
        override fun run() {
            if (!isCurrent()) return
            if (!view.isAttachedToWindow) {
                view.post(this)
                return
            }
            action()
        }
    })
}
