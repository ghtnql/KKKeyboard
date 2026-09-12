package com.ghtnql.kkkeyboard

import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout

class KoreanKeyboardService : InputMethodService() {
    private val composer = HangulComposer()

    override fun onCreateInputView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(6), dp(4), dp(8))
        }

        KEY_ROWS.forEach { row ->
            root.addView(createRow(row.map { it.toString() }))
        }

        root.addView(createRow(listOf("NEXT", "Space", "Enter", "⌫"), actionRow = true))
        return root
    }

    override fun onFinishInput() {
        composer.reset()
        super.onFinishInput()
    }

    private fun createRow(labels: List<String>, actionRow: Boolean = false): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER

            labels.forEach { label ->
                addView(Button(this@KoreanKeyboardService).apply {
                    text = label
                    textSize = if (actionRow) 14f else 20f
                    isAllCaps = false
                    minWidth = 0
                    minimumWidth = 0
                    setPadding(dp(2), 0, dp(2), 0)
                    layoutParams = LinearLayout.LayoutParams(0, dp(52), 1f).apply {
                        setMargins(dp(2), dp(2), dp(2), dp(2))
                    }
                    setOnClickListener { handleKey(label) }
                })
            }
        }
    }

    private fun handleKey(label: String) {
        val connection = currentInputConnection ?: return

        when (label) {
            "⌫" -> handleBackspace(connection)
            "Space" -> {
                commitPending(connection)
                connection.commitText(" ", 1)
            }
            "Enter" -> {
                commitPending(connection)
                sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
            }
            "NEXT" -> {
                commitPending(connection)
                switchToNextInputMethod(false)
            }
            else -> {
                val ch = label.singleOrNull() ?: return
                applyEdit(connection, composer.input(ch))
            }
        }
    }

    private fun handleBackspace(connection: InputConnection) {
        val edit = composer.backspace()
        if (!edit.consumed) {
            connection.deleteSurroundingText(1, 0)
            return
        }

        if (edit.composing.isNullOrEmpty()) {
            connection.setComposingText("", 1)
            connection.finishComposingText()
        } else {
            connection.setComposingText(edit.composing, 1)
        }
    }

    private fun applyEdit(connection: InputConnection, edit: HangulComposer.Edit) {
        if (edit.commit.isNotEmpty()) {
            connection.commitText(edit.commit, 1)
        }

        if (edit.composing != null) {
            connection.setComposingText(edit.composing, 1)
        }
    }

    private fun commitPending(connection: InputConnection) {
        val pending = composer.flush()
        if (pending.isNotEmpty()) {
            connection.commitText(pending, 1)
        } else {
            connection.finishComposingText()
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private val KEY_ROWS = listOf(
            charArrayOf('ㅂ', 'ㅈ', 'ㄷ', 'ㄱ', 'ㅅ'),
            charArrayOf('ㅛ', 'ㅕ', 'ㅑ', 'ㅐ', 'ㅔ'),
            charArrayOf('ㅁ', 'ㄴ', 'ㅇ', 'ㄹ', 'ㅎ'),
            charArrayOf('ㅋ', 'ㅌ', 'ㅊ', 'ㅍ', 'ㅠ'),
            charArrayOf('ㅜ', 'ㅡ', 'ㅣ', 'ㅏ', 'ㅓ'),
        )
    }
}
