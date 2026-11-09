package com.example.jetskeyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.widget.LinearLayout

class JetsImeService : InputMethodService(), KeyboardCanvasView.Listener {
    private var keyboardView: KeyboardCanvasView? = null
    private var currentMode = KeyboardMode.ARABIC

    override fun onCreateInputView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.rgb(18, 18, 18))
        }
        keyboardView = KeyboardCanvasView(this).apply {
            mode = currentMode
            listener = this@JetsImeService
        }
        root.addView(
            keyboardView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                resources.displayMetrics.density.let { (258f * it).toInt() }
            )
        )
        return root
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        currentMode = KeyboardMode.ARABIC
        keyboardView?.mode = currentMode
    }

    override fun onText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    override fun onBackspace() {
        val connection = currentInputConnection ?: return
        val selected = connection.getSelectedText(0)
        if (!selected.isNullOrEmpty()) {
            connection.commitText("", 1)
        } else {
            val before = connection.getTextBeforeCursor(1, 0)
            if (!before.isNullOrEmpty()) {
                connection.deleteSurroundingText(1, 0)
            } else {
                connection.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DEL))
                connection.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_DEL))
            }
        }
    }

    override fun onCursorMove(delta: Int) {
        val connection = currentInputConnection ?: return
        val extracted = connection.getExtractedText(ExtractedTextRequest(), 0) ?: return
        val selectionStart = extracted.selectionStart
        val selectionEnd = extracted.selectionEnd
        val target = (selectionEnd + delta).coerceIn(0, extracted.text?.length ?: 0)
        connection.setSelection(target, target)
    }

    override fun onEnter() {
        val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
        val connection = currentInputConnection ?: return
        if (action != null && action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            connection.performEditorAction(action)
        } else {
            connection.commitText("\n", 1)
        }
    }

    override fun onModeChange(mode: KeyboardMode) {
        currentMode = mode
    }
}
