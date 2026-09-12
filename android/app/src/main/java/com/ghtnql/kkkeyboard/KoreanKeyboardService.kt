package com.ghtnql.kkkeyboard

import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
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
    private var keyboardRoot: LinearLayout? = null
    private var activeFieldMode = InputFieldMode.TEXT

    private val deleteRepeat = object : Runnable {
        override fun run() {
            if (!deleting) return
            currentInputConnection?.let(::handleBackspace)
            handler.postDelayed(this, 55L)
        }
    }

    override fun onCreateInputView(): View {
        activeFieldMode = InputFieldModeResolver.fromInputType(currentInputEditorInfo?.inputType ?: 0)
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(6), dp(4), dp(8))
            keyboardRoot = this
            renderKeyboard(this, activeFieldMode)
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        stopDeleteRepeat()

        val nextMode = InputFieldModeResolver.fromInputType(info?.inputType ?: 0)
        val modeChanged = nextMode != activeFieldMode
        activeFieldMode = nextMode

        if (!restarting) {
            composer.reset()
            shiftEnabled = false
        }

        keyboardRoot?.let { root ->
            if (!restarting || modeChanged) renderKeyboard(root, nextMode)
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
        keyboardRoot = null
        super.onDestroy()
    }

    private fun renderKeyboard(root: LinearLayout, mode: InputFieldMode) {
        shiftedCharacterButtons.clear()
        shiftButton = null
        shiftEnabled = false
        root.removeAllViews()

        when (mode) {
            InputFieldMode.NUMBER -> renderNumberKeyboard(root)
            InputFieldMode.PHONE -> renderPhoneKeyboard(root)
            InputFieldMode.EMAIL, InputFieldMode.URI, InputFieldMode.TEXT -> renderTextKeyboard(root, mode)
        }
    }

    private fun renderTextKeyboard(root: LinearLayout, mode: InputFieldMode) {
        TwoBeolsikLayout.characterRows.forEach { root.addView(createCharacterRow(it)) }
        root.addView(createBottomCharacterRow())

        when (mode) {
            InputFieldMode.EMAIL -> root.addView(createLiteralRow(listOf("@", ".", "-", "_")))
            InputFieldMode.URI -> root.addView(createLiteralRow(listOf("/", ".", ":", "-", "_")))
            else -> Unit
        }

        root.addView(createActionRow())
    }

    private fun renderNumberKeyboard(root: LinearLayout) {
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
        ).forEach { root.addView(createLiteralRow(it)) }
        root.addView(createLiteralRow(listOf("-", "0", ".")))
        root.addView(createCompactActionRow())
    }

    private fun renderPhoneKeyboard(root: LinearLayout) {
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("*", "0", "#"),
        ).forEach { root.addView(createLiteralRow(it)) }
        root.addView(createLiteralRow(listOf("+", "-", "(" , ")")))
        root.addView(createCompactActionRow())
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

    private fun createLiteralRow(labels: List<String>) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        labels.forEach { label -> addView(createLiteralButton(label)) }
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

    private fun createCompactActionRow() = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        addView(createActionButton("NEXT", 1f) {
            currentInputConnection?.let { c -> commitPending(c); switchToNextInputMethod(false) }
        })
        addView(createActionButton("Enter", 1.3f) { currentInputConnection?.let(::handleEnter) })
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

    private fun createLiteralButton(label: String) = Button(this).apply {
        text = label
        textSize = 18f
        isAllCaps = false
        minWidth = 0
        minimumWidth = 0
        setPadding(dp(1), 0, dp(1), 0)
        layoutParams = keyLayoutParams()
        setOnClickListener {
            currentInputConnection?.let { connection ->
                commitPending(connection)
                connection.commitText(label, 1)
            }
        }
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
