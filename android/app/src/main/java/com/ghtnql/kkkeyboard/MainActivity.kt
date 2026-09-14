package com.ghtnql.kkkeyboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(48, 72, 48, 48)
        }

        content.addView(TextView(this).apply {
            text = getString(R.string.onboarding_title)
            textSize = 30f
        })

        content.addView(TextView(this).apply {
            text = getString(R.string.onboarding_instructions)
            textSize = 17f
            setPadding(0, 36, 0, 24)
        })

        addLayoutControls(content, KeyboardOrientation.PORTRAIT, getString(R.string.orientation_portrait))
        addLayoutControls(content, KeyboardOrientation.LANDSCAPE, getString(R.string.orientation_landscape))

        content.addView(Button(this).apply {
            text = getString(R.string.open_keyboard_settings)
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        })

        content.addView(Button(this).apply {
            text = getString(R.string.select_keyboard)
            setOnClickListener {
                getSystemService(InputMethodManager::class.java)?.showInputMethodPicker()
            }
        })

        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            addView(
                content,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                ),
            )
        }
        setContentView(scrollView)
    }

    private fun addLayoutControls(
        root: LinearLayout,
        orientation: KeyboardOrientation,
        orientationLabel: String,
    ) {
        root.addView(TextView(this).apply {
            text = getString(R.string.layout_section_format, orientationLabel)
            textSize = 19f
            setPadding(0, 18, 0, 8)
        })

        val heightStatus = TextView(this).apply { textSize = 16f }
        fun heightLabel(height: KeyboardHeight): String = when (height) {
            KeyboardHeight.COMPACT -> getString(R.string.height_name_compact)
            KeyboardHeight.NORMAL -> getString(R.string.height_name_normal)
            KeyboardHeight.TALL -> getString(R.string.height_name_tall)
        }
        fun refreshHeightStatus() {
            val height = KeyboardLayoutSettings.readHeight(this, orientation)
            heightStatus.text = getString(
                R.string.height_status_format,
                heightLabel(height),
                height.keyHeightDp,
            )
        }
        refreshHeightStatus()
        root.addView(heightStatus)

        val heightRow = LinearLayout(this).apply {
            this.orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        listOf(
            R.string.height_compact to KeyboardHeight.COMPACT,
            R.string.height_normal to KeyboardHeight.NORMAL,
            R.string.height_tall to KeyboardHeight.TALL,
        ).forEach { (labelRes, height) ->
            heightRow.addView(Button(this).apply {
                text = getString(labelRes)
                isAllCaps = false
                setOnClickListener {
                    KeyboardLayoutSettings.writeHeight(this@MainActivity, orientation, height)
                    refreshHeightStatus()
                }
            })
        }
        root.addView(heightRow)

        val numberRowStatus = TextView(this).apply {
            textSize = 16f
            setPadding(0, 10, 0, 4)
        }
        val numberRowToggle = Button(this).apply { isAllCaps = false }
        fun refreshNumberRowStatus() {
            val enabled = KeyboardLayoutSettings.readNumberRowEnabled(this, orientation)
            numberRowStatus.text = getString(
                R.string.number_row_status_format,
                getString(if (enabled) R.string.state_on else R.string.state_off),
            )
            numberRowToggle.text = getString(
                if (enabled) R.string.number_row_disable else R.string.number_row_enable,
            )
        }
        numberRowToggle.setOnClickListener {
            val enabled = KeyboardLayoutSettings.readNumberRowEnabled(this@MainActivity, orientation)
            KeyboardLayoutSettings.writeNumberRowEnabled(this@MainActivity, orientation, !enabled)
            refreshNumberRowStatus()
        }
        refreshNumberRowStatus()
        root.addView(numberRowStatus)
        root.addView(numberRowToggle)

        val cursorRowStatus = TextView(this).apply {
            textSize = 16f
            setPadding(0, 10, 0, 4)
        }
        val cursorRowToggle = Button(this).apply { isAllCaps = false }
        fun refreshCursorRowStatus() {
            val enabled = KeyboardLayoutSettings.readCursorRowEnabled(this, orientation)
            cursorRowStatus.text = getString(
                R.string.cursor_row_status_format,
                getString(if (enabled) R.string.state_on else R.string.state_off),
            )
            cursorRowToggle.text = getString(
                if (enabled) R.string.cursor_row_disable else R.string.cursor_row_enable,
            )
        }
        cursorRowToggle.setOnClickListener {
            val enabled = KeyboardLayoutSettings.readCursorRowEnabled(this@MainActivity, orientation)
            KeyboardLayoutSettings.writeCursorRowEnabled(this@MainActivity, orientation, !enabled)
            refreshCursorRowStatus()
        }
        refreshCursorRowStatus()
        root.addView(cursorRowStatus)
        root.addView(cursorRowToggle)
    }
}
