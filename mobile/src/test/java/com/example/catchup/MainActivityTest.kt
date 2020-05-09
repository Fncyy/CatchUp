package com.example.catchup

import android.content.Context
import android.os.Build
import android.view.View
import android.view.animation.Animation
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class MainActivityTest {

    @Test
    fun preferences_page_visibility_test() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().resume().get()
        val preferencesButton: ImageButton = activity.findViewById(R.id.toolbar_main_preferences)
        val recyclerView: RecyclerView = activity.findViewById(R.id.rvMain)

        assert(preferencesButton.isClickable)
        assertEquals(View.VISIBLE, recyclerView.visibility)

        preferencesButton.performClick()

        assertFalse(preferencesButton.isClickable)
        assertEquals(View.GONE, recyclerView.visibility)

        activity!!.onBackPressed()

        assert(preferencesButton.isClickable)
        assertEquals(View.VISIBLE, recyclerView.visibility)
    }

    @Test
    fun first_start_test() {
        val activity = Robolectric.buildActivity(MainActivity::class.java)
        var startedBefore = activity.get().getPreferences(Context.MODE_PRIVATE).getBoolean("KEY_STARTED", false)
        assertFalse(startedBefore)
        activity.create()
        startedBefore = activity.get().getPreferences(Context.MODE_PRIVATE).getBoolean("KEY_STARTED", true)
        assert(startedBefore)
    }

    @Test
    fun reload_test() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().resume().get()
        val refreshButton: ImageButton = activity!!.findViewById(R.id.toolbar_main_refresh)

        refreshButton.performClick()
        refreshButton.animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                assertEquals(true, animation?.hasEnded())
            }

            override fun onAnimationStart(animation: Animation?) {
                assertEquals(1000, animation?.duration)
                assertEquals(0, animation?.repeatCount)
                assertEquals(true, animation?.fillAfter)
                assertEquals(true, animation?.hasStarted())
            }
        })
        refreshButton.performClick()
    }
}