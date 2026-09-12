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
            text = "1. 키보드를 활성화합니다.\n2. 입력창에서 ㅋㅋ키보드를 선택합니다.\n3. 현재 MVP는 기본 한글 조합 입력을 검증합니다."
            textSize = 17f
            setPadding(0, 36, 0, 36)
        })

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
