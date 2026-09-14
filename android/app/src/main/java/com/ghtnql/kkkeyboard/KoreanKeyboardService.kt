package com.ghtnql.kkkeyboard

import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout

class KoreanKeyboardService : InputMethodService() {
    private val composer = HangulComposer()
    private val candidateInput = CandidateInputBuffer()
    private val shiftedCharacterButtons = mutableListOf<Pair<Button, String>>()
    private val candidateButtons = mutableListOf<Button>()
    private val handler = Handler(Looper.getMainLooper())
    private var shiftEnabled = false
    private var shiftButton: Button? = null
    private var modeButton: Button? = null
    private var deleting = false
    private var keyboardRoot: LinearLayout? = null
    private var candidateRow: LinearLayout? = null
    private var activeFieldMode = InputFieldMode.TEXT
    private var japaneseCandidateMode = false
    private var displayedCandidates: List<String> = emptyList()
    private var layoutOrientation = KeyboardOrientation.PORTRAIT
    private var keyHeightDp = KeyboardHeight.NORMAL.keyHeightDp
    private var numberRowEnabled = false
    private var cursorRowEnabled = false

    private val deleteRepeat = object : Runnable {
        override fun run() {
            if (!deleting) return
            currentInputConnection?.let(::handleBackspace)
            handler.postDelayed(this, 55L)
        }
    }

    override fun onCreateInputView(): View {
        activeFieldMode = InputFieldModeResolver.fromInputType(currentInputEditorInfo?.inputType ?: 0)
        layoutOrientation = currentKeyboardOrientation()
        keyHeightDp = KeyboardLayoutSettings.readHeight(this, layoutOrientation).keyHeightDp
        numberRowEnabled = KeyboardLayoutSettings.readNumberRowEnabled(this, layoutOrientation)
        cursorRowEnabled = KeyboardLayoutSettings.readCursorRowEnabled(this, layoutOrientation)
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(6), dp(4), dp(8))
            keyboardRoot = this
            renderKeyboard(this, activeFieldMode)
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        stopDeleteRepeat()
        resetInputState()
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        stopDeleteRepeat()

        val nextMode = InputFieldModeResolver.fromInputType(info?.inputType ?: 0)
        val modeChanged = nextMode != activeFieldMode
        val nextOrientation = currentKeyboardOrientation()
        val orientationChanged = nextOrientation != layoutOrientation
        val nextHeightDp = KeyboardLayoutSettings.readHeight(this, nextOrientation).keyHeightDp
        val heightChanged = nextHeightDp != keyHeightDp
        val nextNumberRowEnabled = KeyboardLayoutSettings.readNumberRowEnabled(this, nextOrientation)
        val numberRowChanged = nextNumberRowEnabled != numberRowEnabled
        val nextCursorRowEnabled = KeyboardLayoutSettings.readCursorRowEnabled(this, nextOrientation)
        val cursorRowChanged = nextCursorRowEnabled != cursorRowEnabled
        activeFieldMode = nextMode
        layoutOrientation = nextOrientation
        keyHeightDp = nextHeightDp
        numberRowEnabled = nextNumberRowEnabled
        cursorRowEnabled = nextCursorRowEnabled

        if (!restarting) resetInputState()

        keyboardRoot?.let { root ->
            if (!restarting || modeChanged || orientationChanged || heightChanged || numberRowChanged || cursorRowChanged) {
                renderKeyboard(root, nextMode)
            }
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        stopDeleteRepeat()
        super.onFinishInputView(finishingInput)
    }

    override fun onWindowHidden() {
        stopDeleteRepeat()
        super.onWindowHidden()
    }

    override fun onFinishInput() {
        stopDeleteRepeat()
        resetInputState()
        super.onFinishInput()
    }

    override fun onDestroy() {
        stopDeleteRepeat()
        keyboardRoot = null
        candidateRow = null
        candidateButtons.clear()
        modeButton = null
        super.onDestroy()
    }

    private fun resetInputState() {
        composer.reset()
        candidateInput.clear()
        shiftEnabled = false
        displayedCandidates = emptyList()
        hideCandidateButtons()
    }

    private fun renderKeyboard(root: LinearLayout, mode: InputFieldMode) {
        shiftedCharacterButtons.clear()
        candidateButtons.clear()
        shiftButton = null
        modeButton = null
        candidateRow = null
        shiftEnabled = false
        displayedCandidates = emptyList()
        root.removeAllViews()

        when (mode) {
            InputFieldMode.NUMBER -> renderNumberKeyboard(root)
            InputFieldMode.PHONE -> renderPhoneKeyboard(root)
            InputFieldMode.EMAIL, InputFieldMode.URI, InputFieldMode.TEXT -> renderTextKeyboard(root, mode)
        }
    }

    private fun renderTextKeyboard(root: LinearLayout, mode: InputFieldMode) {
        root.addView(createCandidateRow().also { candidateRow = it })
        if (numberRowEnabled) root.addView(createLiteralRow(TwoBeolsikLayout.auxiliaryNumberRow))
        TwoBeolsikLayout.characterRows.forEach { root.addView(createCharacterRow(it)) }
        root.addView(createBottomCharacterRow())
        if (cursorRowEnabled) root.addView(createCursorRow())

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
        if (cursorRowEnabled) root.addView(createCursorRow())
        root.addView(createCompactActionRow())
    }

    private fun renderPhoneKeyboard(root: LinearLayout) {
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("*", "0", "#"),
        ).forEach { root.addView(createLiteralRow(it)) }
        root.addView(createLiteralRow(listOf("+", "-", "(", ")")))
        if (cursorRowEnabled) root.addView(createCursorRow())
        root.addView(createCompactActionRow())
    }

    private fun createCandidateRow() = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        visibility = View.GONE
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )
        repeat(3) {
            val button = createActionButton("", 1f) {}
            button.visibility = View.GONE
            button.setOnClickListener { view ->
                val candidate = (view as Button).text.toString()
                if (candidate.isNotEmpty()) selectCandidate(candidate)
            }
            candidateButtons += button
            addView(button)
        }
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

    private fun createCursorRow() = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        addView(createActionButton("◀", 1f) { moveCursor(KeyEvent.KEYCODE_DPAD_LEFT) })
        addView(createActionButton("▶", 1f) { moveCursor(KeyEvent.KEYCODE_DPAD_RIGHT) })
    }

    private fun createActionRow() = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        addView(createActionButton(if (japaneseCandidateMode) "日" else "한", 0.8f) {
            toggleCandidateMode()
        }.also { modeButton = it })
        addView(createActionButton("NEXT", 0.9f) {
            currentInputConnection?.let { c -> commitPending(c); clearCandidateTracking(); switchToNextKeyboard() }
        })
        addView(createActionButton("Space", 1.8f) {
            currentInputConnection?.let { c -> commitPending(c); clearCandidateTracking(); c.commitText(" ", 1) }
        })
        addView(createActionButton("Enter", 1.1f) { currentInputConnection?.let(::handleEnter) })
        addView(createBackspaceButton())
    }

    private fun createCompactActionRow() = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        addView(createActionButton("NEXT", 1f) {
            currentInputConnection?.let { c -> commitPending(c); clearCandidateTracking(); switchToNextKeyboard() }
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
                clearCandidateTracking()
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
        val edit = composer.input(ch)
        candidateInput.apply(edit)
        applyEdit(connection, edit)
        refreshCandidates()
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

    private fun toggleCandidateMode() {
        currentInputConnection?.let(::commitPending)
        clearCandidateTracking()
        japaneseCandidateMode = !japaneseCandidateMode
        modeButton?.text = if (japaneseCandidateMode) "日" else "한"
    }

    private fun moveCursor(keyCode: Int) {
        val connection = currentInputConnection ?: return
        commitPending(connection)
        clearCandidateTracking()
        sendDownUpKeyEvents(keyCode)
    }

    private fun switchToNextKeyboard() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            switchToNextInputMethod(false)
            return
        }
        val token = window.window?.attributes?.token ?: return
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.switchToNextInputMethod(token, false)
    }

    private fun refreshCandidates(force: Boolean = false) {
        val row = candidateRow ?: return
        val candidates = if (japaneseCandidateMode) {
            candidateInput.currentForLookup(
                composer.currentText(),
                JapaneseTransliterator.maxInputLength,
            )?.let(JapaneseTransliterator::candidatesExact) ?: emptyList()
        } else {
            emptyList()
        }

        if (!force && candidates == displayedCandidates) return
        displayedCandidates = candidates

        candidateButtons.forEachIndexed { index, button ->
            val candidate = candidates.getOrNull(index)
            if (candidate == null) {
                button.text = ""
                button.visibility = View.GONE
            } else {
                button.text = candidate
                button.visibility = View.VISIBLE
            }
        }
        row.visibility = if (candidates.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun selectCandidate(candidate: String) {
        val connection = currentInputConnection ?: return
        val source = candidateInput.current(composer.currentText())
        if (source.isEmpty()) return

        commitPending(connection)
        connection.deleteSurroundingText(source.length, 0)
        connection.commitText(candidate, 1)
        clearCandidateTracking()
    }

    private fun clearCandidateTracking() {
        candidateInput.clear()
        displayedCandidates = emptyList()
        hideCandidateButtons()
    }

    private fun hideCandidateButtons() {
        candidateButtons.forEach { button ->
            button.text = ""
            button.visibility = View.GONE
        }
        candidateRow?.visibility = View.GONE
    }

    private fun handleEnter(connection: InputConnection) {
        commitPending(connection)
        clearCandidateTracking()
        val actionId = EnterActionResolver.actionId(currentInputEditorInfo?.imeOptions ?: 0)
        if (actionId != null) connection.performEditorAction(actionId)
        else sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
    }

    private fun handleBackspace(connection: InputConnection) {
        val selectedText = connection.getSelectedText(0)
        if (!selectedText.isNullOrEmpty()) {
            composer.reset()
            clearCandidateTracking()
            connection.finishComposingText()
            connection.commitText("", 1)
            return
        }

        val edit = composer.backspace()
        if (!edit.consumed) {
            connection.deleteSurroundingTextInCodePoints(1, 0)
            candidateInput.removeCommittedCodePoint()
            refreshCandidates()
            return
        }
        if (edit.composing.isNullOrEmpty()) {
            connection.setComposingText("", 1)
            connection.finishComposingText()
        } else {
            connection.setComposingText(edit.composing, 1)
        }
        refreshCandidates()
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

    private fun currentKeyboardOrientation(): KeyboardOrientation =
        KeyboardOrientation.fromConfigurationOrientation(resources.configuration.orientation)

    private fun keyLayoutParams(weight: Float = 1f) =
        LinearLayout.LayoutParams(0, dp(keyHeightDp), weight).apply { setMargins(dp(1), dp(2), dp(1), dp(2)) }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
