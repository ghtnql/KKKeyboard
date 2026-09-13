package com.ghtnql.kkkeyboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(48, 72, 48, 48)
        }

        root.addView(TextView(this).apply {
            text = "ㅋㅋ키보드"
            textSize = 30f
        })

        root.addView(TextView(this).apply {
            text = "1. 키보드를 활성화합니다.\n2. 입력창에서 ㅋㅋ키보드를 선택합니다.\n3. 아래에서 키 높이와 숫자열을 설정할 수 있습니다."
            textSize = 17f
            setPadding(0, 36, 0, 24)
        })

        val heightStatus = TextView(this).apply {
            textSize = 16f
            setPadding(0, 0, 0, 12)
        }

        fun refreshHeightStatus() {
            val height = KeyboardLayoutSettings.readHeight(this)
            heightStatus.text = "키 높이: ${height.name.lowercase()} (${height.keyHeightDp}dp)"
        }

        refreshHeightStatus()
        root.addView(heightStatus)

        val heightRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
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
                    KeyboardLayoutSettings.writeHeight(this@MainActivity, height)
                    refreshHeightStatus()
                }
            })
        }
        root.addView(heightRow)

        val numberRowStatus = TextView(this).apply {
            textSize = 16f
            setPadding(0, 20, 0, 8)
        }
        val numberRowToggle = Button(this).apply { isAllCaps = false }

        fun refreshNumberRowStatus() {
            val enabled = KeyboardLayoutSettings.readNumberRowEnabled(this)
            numberRowStatus.text = "보조 숫자열: ${if (enabled) "켜짐" else "꺼짐"}"
            numberRowToggle.text = if (enabled) "숫자열 끄기" else "숫자열 켜기"
        }

        numberRowToggle.setOnClickListener {
            val enabled = KeyboardLayoutSettings.readNumberRowEnabled(this@MainActivity)
            KeyboardLayoutSettings.writeNumberRowEnabled(this@MainActivity, !enabled)
            refreshNumberRowStatus()
        }
        refreshNumberRowStatus()
        root.addView(numberRowStatus)
        root.addView(numberRowToggle)

        root.addView(Button(this).apply {
            text = "키보드 활성화 설정 열기"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        })

        root.addView(Button(this).apply {
            text = "키보드 선택"
            setOnClickListener {
                getSystemService(InputMethodManager::class.java)?.showInputMethodPicker()
            }
        })

        setContentView(root)
    }
}
