package com.ghtnql.kkkeyboard

import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout

class KoreanKeyboardService : InputMethodService() {
    private val composer = HangulComposer()
    private val shiftedCharacterButtons = mutableListOf<Pair<Button, String>>()
    private val handler = Handler(Looper.getMainLooper())
    private var shiftEnabled = false
    private var shiftButton: Button? = null
    private var deleting = false

    private val deleteRepeat = object : Runnable {
        override fun run() {
            if (!deleting) return
            currentInputConnection?.let(::handleBackspace)
            handler.postDelayed(this, 55L)
        }
    }

    override fun onCreateInputView(): View {
        shiftedCharacterButtons.clear()
        shiftEnabled = false
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(6), dp(4), dp(8))
            TwoBeolsikLayout.characterRows.forEach { addView(createCharacterRow(it)) }
            addView(createBottomCharacterRow())
            addView(createActionRow())
        }
    }

    override fun onFinishInput() {
        stopDeleteRepeat()
        composer.reset()
        shiftEnabled = false
        super.onFinishInput()
    }

    override fun onDestroy() {
        stopDeleteRepeat()
        super.onDestroy()
    }

    private fun createCharacterRow(labels: List<String>) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        labels.forEach { addView(createCharacterButton(it)) }
    }

    private fun createBottomCharacterRow() = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        addView(createActionButton("Shift", 1.35f) { toggleShift() }.also { shiftButton = it })
        TwoBeolsikLayout.bottomRow.forEach { addView(createCharacterButton(it)) }
    }

    private fun createActionRow() = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        addView(createActionButton("NEXT", 1f) {
            currentInputConnection?.let { c -> commitPending(c); switchToNextInputMethod(false) }
        })
        addView(createActionButton("Space", 2.2f) {
            currentInputConnection?.let { c -> commitPending(c); c.commitText(" ", 1) }
        })
        addView(createActionButton("Enter", 1.2f) { currentInputConnection?.let(::handleEnter) })
        addView(createBackspaceButton())
    }

    private fun createCharacterButton(baseLabel: String) = Button(this).apply {
        text = baseLabel
        textSize = 18f
        isAllCaps = false
        minWidth = 0
        minimumWidth = 0
        setPadding(dp(1), 0, dp(1), 0)
        layoutParams = keyLayoutParams()
        setOnClickListener { handleCharacter(baseLabel) }
    }.also { button ->
        if (TwoBeolsikLayout.hasShiftVariant(baseLabel)) shiftedCharacterButtons += button to baseLabel
    }

    private fun createActionButton(label: String, weight: Float, onClick: () -> Unit) = Button(this).apply {
        text = label
        textSize = 14f
        isAllCaps = false
        minWidth = 0
        minimumWidth = 0
        setPadding(dp(1), 0, dp(1), 0)
        layoutParams = keyLayoutParams(weight)
        setOnClickListener { onClick() }
    }

    private fun createBackspaceButton() = Button(this).apply {
        text = "⌫"
        textSize = 14f
        isAllCaps = false
        minWidth = 0
        minimumWidth = 0
        setPadding(dp(1), 0, dp(1), 0)
        layoutParams = keyLayoutParams(1f)
        setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    deleting = true
                    currentInputConnection?.let(::handleBackspace)
                    handler.postDelayed(deleteRepeat, 400L)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> stopDeleteRepeat()
            }
            true
        }
    }

    private fun stopDeleteRepeat() {
        deleting = false
        handler.removeCallbacks(deleteRepeat)
    }

    private fun handleCharacter(baseLabel: String) {
        val connection = currentInputConnection ?: return
        val ch = TwoBeolsikLayout.labelFor(baseLabel, shiftEnabled).singleOrNull() ?: return
        applyEdit(connection, composer.input(ch))
        if (shiftEnabled) setShift(false)
    }

    private fun toggleShift() = setShift(!shiftEnabled)

    private fun setShift(enabled: Boolean) {
        if (shiftEnabled == enabled) return
        shiftEnabled = enabled
        shiftedCharacterButtons.forEach { (button, baseLabel) ->
            button.text = TwoBeolsikLayout.labelFor(baseLabel, enabled)
        }
        shiftButton?.isActivated = enabled
    }

    private fun handleEnter(connection: InputConnection) {
        commitPending(connection)
        val actionId = EnterActionResolver.actionId(currentInputEditorInfo?.imeOptions ?: 0)
        if (actionId != null) connection.performEditorAction(actionId)
        else sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
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
        if (edit.commit.isNotEmpty()) connection.commitText(edit.commit, 1)
        if (edit.composing != null) connection.setComposingText(edit.composing, 1)
    }

    private fun commitPending(connection: InputConnection) {
        val pending = composer.flush()
        if (pending.isNotEmpty()) connection.commitText(pending, 1)
        else connection.finishComposingText()
    }

    private fun keyLayoutParams(weight: Float = 1f) =
        LinearLayout.LayoutParams(0, dp(50), weight).apply { setMargins(dp(1), dp(2), dp(1), dp(2)) }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
