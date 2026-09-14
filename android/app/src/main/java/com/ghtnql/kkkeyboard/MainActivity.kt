package com.ghtnql.kkkeyboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Button
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
            text = "ㅋㅋ키보드"
            textSize = 30f
        })

        content.addView(TextView(this).apply {
            text = "1. 키보드를 활성화합니다.\n2. 입력창에서 ㅋㅋ키보드를 선택합니다.\n3. 세로/가로 각각 키 높이와 숫자열·커서열을 설정할 수 있습니다."
            textSize = 17f
            setPadding(0, 36, 0, 24)
        })

        addLayoutControls(content, KeyboardOrientation.PORTRAIT, "세로")
        addLayoutControls(content, KeyboardOrientation.LANDSCAPE, "가로")

        content.addView(Button(this).apply {
            text = "키보드 활성화 설정 열기"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        })

        content.addView(Button(this).apply {
            text = "키보드 선택"
            setOnClickListener {
                getSystemService(InputMethodManager::class.java)?.showInputMethodPicker()
            }
        })

        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            addView(
                content,
                ScrollView.LayoutParams(
                    ScrollView.LayoutParams.MATCH_PARENT,
                    ScrollView.LayoutParams.WRAP_CONTENT,
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
            text = "$orientationLabel 레이아웃"
            textSize = 19f
            setPadding(0, 18, 0, 8)
        })

        val heightStatus = TextView(this).apply { textSize = 16f }
        fun refreshHeightStatus() {
            val height = KeyboardLayoutSettings.readHeight(this, orientation)
            heightStatus.text = "키 높이: ${height.name.lowercase()} (${height.keyHeightDp}dp)"
        }
        refreshHeightStatus()
        root.addView(heightStatus)

        val heightRow = LinearLayout(this).apply {
            this.orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        listOf(
            "작게" to KeyboardHeight.COMPACT,
            "기본" to KeyboardHeight.NORMAL,
            "크게" to KeyboardHeight.TALL,
        ).forEach { (label, height) ->
            heightRow.addView(Button(this).apply {
                text = label
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
            numberRowStatus.text = "보조 숫자열: ${if (enabled) "켜짐" else "꺼짐"}"
            numberRowToggle.text = if (enabled) "숫자열 끄기" else "숫자열 켜기"
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
            cursorRowStatus.text = "보조 커서열: ${if (enabled) "켜짐" else "꺼짐"}"
            cursorRowToggle.text = if (enabled) "커서열 끄기" else "커서열 켜기"
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
