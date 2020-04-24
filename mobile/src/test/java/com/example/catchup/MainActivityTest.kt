package com.example.catchup

import android.os.Build
import android.view.View
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

    private var activity: MainActivity? = null

    @Before
    fun init() {
        activity = activity ?: Robolectric.buildActivity(MainActivity::class.java).create().resume().get()
    }

    @Test
    fun preferences_page_visibility() {
        val preferencesButton: ImageButton = activity!!.findViewById(R.id.toolbar_main_preferences)
        val recyclerView: RecyclerView = activity!!.findViewById(R.id.rvMain)

        assert(preferencesButton.isClickable)
        assertEquals(View.VISIBLE, recyclerView.visibility)

        preferencesButton.performClick()

        assertFalse(preferencesButton.isClickable)
        assertEquals(View.GONE, recyclerView.visibility)

        activity!!.onBackPressed()

        assert(preferencesButton.isClickable)
        assertEquals(View.VISIBLE, recyclerView.visibility)
    }
}