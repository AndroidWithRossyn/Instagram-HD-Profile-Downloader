package com.instagram.hdprofile.downloader.customview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * A custom ViewPager that allows enabling or disabling page swiping.
 *
 * @param context The context in which the ViewPager is running.
 * @param attrs The attributes of the XML tag that is inflating the ViewPager.
 */
class CustomViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {

    /**
     * Flag to enable or disable paging.
     */
    private var isPagingEnabled = false

    init {
        // Initializes the ViewPager with a no-op page transformer.
        setPageTransformer(true) { _, _ -> }
    }

    /**
     * Intercepts touch events based on the paging enabled flag.
     *
     * @param ev The motion event.
     * @return True if paging is enabled and the event should be intercepted.
     */
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return isPagingEnabled && super.onInterceptTouchEvent(ev)
    }

    /**
     * Handles touch events based on the paging enabled flag.
     *
     * @param ev The motion event.
     * @return True if paging is enabled and the event should be handled.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return isPagingEnabled && super.onTouchEvent(ev)
    }

    /**
     * Enables or disables paging.
     *
     * @param enabled True to enable paging, false to disable.
     */
    fun setPagingEnabled(enabled: Boolean) {
        isPagingEnabled = enabled
    }
}
