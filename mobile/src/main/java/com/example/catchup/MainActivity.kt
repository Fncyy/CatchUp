package com.example.catchup

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.RotateAnimation
import android.widget.ImageButton
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.catchup.adapter.CategoryAdapter
import com.example.catchup.fragments.SettingsFragment
import com.example.catchup.shared.library.BrowseTree
import com.example.catchup.shared.model.CategoryResponse
import com.example.catchup.shared.network.CurrentsInteractor
import com.example.catchup.shared.network.isConnected
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt


class MainActivity : AppCompatActivity() {

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private val currentsInteractor = CurrentsInteractor()

    private var btnRefresh: ImageButton? = null
    private var btnPreferences: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.toolbar_main)

        btnRefresh = supportActionBar?.customView?.findViewById(R.id.toolbar_main_refresh)
        btnPreferences = supportActionBar?.customView?.findViewById(R.id.toolbar_main_preferences)

        btnRefresh?.setOnClickListener {
            getAvailableCategories()
            it.clearAnimation()
            val anim =
                RotateAnimation(0F, 360F, (it.width / 2).toFloat(), (it.height / 2).toFloat())
            anim.fillAfter = true
            anim.repeatCount = 0
            anim.duration = 1000
            it.startAnimation(anim)
        }

        btnPreferences?.setOnClickListener {
            btnPreferences?.isClickable = false
            rvMain.visibility = View.GONE
            supportFragmentManager
                .beginTransaction()
                .addToBackStack("Settings")
                .replace(R.id.layoutMain, SettingsFragment())
                .commit()
        }

        categoryAdapter = CategoryAdapter(this)
        rvMain.adapter = categoryAdapter
        layoutManager = LinearLayoutManager(this)
        rvMain.layoutManager = layoutManager
        getAvailableCategories()

        if (isFirstStart())
            MaterialTapTargetPrompt.Builder(this)
                .setTarget(R.id.toolbar_main_refresh)
                .setFocalColour(getColor(R.color.choiceItemActiveBackground))
                .setBackgroundColour(getColor(R.color.primary_dark))
                .setPrimaryText("Refresh")
                .setSecondaryText("Reloads the available categories")
                .show()

        saveFirstStart()
    }

    override fun onBackPressed() {
        btnPreferences?.isClickable = true
        rvMain.visibility = View.VISIBLE
        super.onBackPressed()
    }

    private fun getAvailableCategories() {
        currentsInteractor.getAvailableCategories(
            onSuccess = this::showCategories,
            onError = this::showError
        )
    }

    private fun showCategories(response: CategoryResponse) {
        categoryAdapter.setList(response.categories)
    }

    private fun showError(e: Throwable) {
        e.printStackTrace()
        val alertMessage: String
        val alertTitle: String
        if (!isConnected()) {
            alertMessage =
                "It seems you are not connected to the internet. \nPress \"Retry\" to try to establish connection again or \"Cancel\" to close this dialog."
            alertTitle = "No connection"
        } else {
            alertMessage =
                "It seems the server is unavailable at this time. \nPress \"Retry\" to try to establish connection again or \"Cancel\" to close this dialog."
            alertTitle = "Server unavailable"
        }
        AlertDialog.Builder(this)
            .setNegativeButton("Cancel") { _, _ -> }
            .setPositiveButton("Retry") { _, _ -> getAvailableCategories() }
            .setMessage(alertMessage)
            .setTitle(alertTitle)
            .show()

    }

    private fun saveFirstStart() = with(this.getPreferences(Context.MODE_PRIVATE).edit()) {
        putBoolean("KEY_STARTED", true)
        commit()
    }

    private fun isFirstStart(): Boolean =
        !(this.getPreferences(Context.MODE_PRIVATE).getBoolean("KEY_STARTED", false))
}
