package com.example.jetskeyboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import kotlin.math.max
import kotlin.math.min

class KeyboardCanvasView(context: Context) : View(context) {
    interface Listener {
        fun onText(text: String)
        fun onBackspace()
        fun onCursorMove(delta: Int)
        fun onEnter()
        fun onModeChange(mode: KeyboardMode)
    }

    var listener: Listener? = null
    var mode: KeyboardMode = KeyboardMode.ARABIC
        set(value) {
            field = value
            invalidate()
        }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(18, 18, 18) }
    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(30, 30, 30) }
    private val functionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(44, 44, 44) }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }
    private val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(0, 230, 118)
        textAlign = Paint.Align.CENTER
        textSize = dp(9f)
    }
    private val rect = RectF()
    private val handler = Handler(Looper.getMainLooper())
    private var activeKey: KeySpec? = null
    private var downX = 0f
    private var downY = 0f
    private var lastCursorX = 0f
    private var longPressTriggered = false
    private var deleteInterval = 115L
    private var deleteRunnable: Runnable? = null
    private var longPressRunnable: Runnable? = null
    private var popup: PopupWindow? = null
    private var keyRects: List<Pair<RectF, KeySpec>> = emptyList()
    private val horizontalGap = dp(4f)
    private val verticalGap = dp(7f)
    private val outerPadding = dp(4f)
    private val cornerRadius = dp(7f)

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desired = dp(258f).toInt()
        val height = resolveSize(desired, heightMeasureSpec)
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.rgb(18, 18, 18))
        val rows = LayoutData.rows(mode)
        val rowHeight = (height - outerPadding * 2f - verticalGap * (rows.size - 1)) / rows.size
        val built = ArrayList<Pair<RectF, KeySpec>>()
        rows.forEachIndexed { rowIndex, row ->
            val totalWeight = row.sumOf { it.weight.toDouble() }.toFloat()
            val availableWidth = width - outerPadding * 2f - horizontalGap * (row.size - 1)
            var x = outerPadding
            val y = outerPadding + rowIndex * (rowHeight + verticalGap)
            row.forEach { spec ->
                val keyWidth = availableWidth * spec.weight / totalWeight
                val keyRect = RectF(x, y, x + keyWidth, y + rowHeight)
                val isFunction = spec.action != KeyAction.CHARACTER && spec.action != KeyAction.COMMA && spec.action != KeyAction.PERIOD
                canvas.drawRoundRect(keyRect, cornerRadius, cornerRadius, if (isFunction) functionPaint else keyPaint)
                val centerX = keyRect.centerX()
                val centerY = keyRect.centerY()
                textPaint.textSize = when {
                    spec.action == KeyAction.SPACE -> dp(13f)
                    spec.label.length > 2 -> dp(12f)
                    else -> dp(20f)
                }
                val baseline = centerY - (textPaint.ascent() + textPaint.descent()) / 2f
                canvas.drawText(spec.label, centerX, baseline, textPaint)
                if (spec.subLabel != null && rowIndex == rows.lastIndex) {
                    canvas.drawText(spec.subLabel, centerX, keyRect.bottom - dp(3f), subPaint)
                } else if (spec.alternates.isNotEmpty()) {
                    canvas.drawText(spec.alternates.first(), keyRect.right - dp(10f), keyRect.top + dp(12f), subPaint)
                }
                built.add(keyRect to spec)
                x += keyWidth + horizontalGap
            }
        }
        keyRects = built
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                lastCursorX = event.x
                activeKey = findKey(event.x, event.y)
                longPressTriggered = false
                if (activeKey?.action == KeyAction.BACKSPACE) {
                    deleteInterval = 115L
                    listener?.onBackspace()
                    scheduleDelete()
                } else {
                    val key = activeKey
                    if (key != null && (key.alternates.isNotEmpty() || key.action == KeyAction.PERIOD || key.action == KeyAction.COMMA)) {
                        val task = Runnable {
                            longPressTriggered = true
                            showAlternates(key)
                        }
                        longPressRunnable = task
                        handler.postDelayed(task, 430L)
                    }
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (activeKey?.action == KeyAction.SPACE) {
                    val delta = event.x - lastCursorX
                    if (kotlin.math.abs(delta) >= dp(8f)) {
                        val steps = (delta / dp(8f)).toInt()
                        if (steps != 0) {
                            listener?.onCursorMove(steps)
                            performHapticFeedback(HapticFeedbackConstants.TEXT_HANDLE_MOVE)
                            lastCursorX += steps * dp(8f)
                        }
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                stopDelete()
                longPressRunnable?.let(handler::removeCallbacks)
                if (event.actionMasked == MotionEvent.ACTION_UP && !longPressTriggered) {
                    val key = activeKey
                    if (key != null && key.action != KeyAction.BACKSPACE && key.action != KeyAction.SPACE) {
                        handleKey(key)
                    } else if (key?.action == KeyAction.SPACE && kotlin.math.abs(event.x - downX) < dp(8f)) {
                        listener?.onText(" ")
                    }
                }
                popup?.dismiss()
                activeKey = null
                return true
            }
        }
        return true
    }

    private fun findKey(x: Float, y: Float): KeySpec? =
        keyRects.firstOrNull { it.first.contains(x, y) }?.second

    private fun handleKey(key: KeySpec) {
        when (key.action) {
            KeyAction.CHARACTER -> listener?.onText(key.label)
            KeyAction.COMMA -> listener?.onText(if (mode == KeyboardMode.ARABIC) "،" else ",")
            KeyAction.PERIOD -> listener?.onText(".")
            KeyAction.ENTER -> listener?.onEnter()
            KeyAction.SHIFT -> {
                mode = if (mode == KeyboardMode.ENGLISH_CAPS) KeyboardMode.ENGLISH_SMALL else KeyboardMode.ENGLISH_CAPS
                listener?.onModeChange(mode)
            }
            KeyAction.SYMBOLS -> {
                mode = KeyboardMode.SYMBOLS
                listener?.onModeChange(mode)
            }
            KeyAction.LETTERS -> {
                mode = KeyboardMode.ARABIC
                listener?.onModeChange(mode)
            }
            KeyAction.LANGUAGE -> {
                mode = when (mode) {
                    KeyboardMode.ARABIC -> KeyboardMode.ENGLISH_SMALL
                    KeyboardMode.ENGLISH_SMALL, KeyboardMode.ENGLISH_CAPS -> KeyboardMode.ARABIC
                    KeyboardMode.SYMBOLS -> KeyboardMode.ARABIC
                }
                listener?.onModeChange(mode)
            }
            KeyAction.SPACE, KeyAction.BACKSPACE -> Unit
        }
    }

    private fun scheduleDelete() {
        val task = object : Runnable {
            override fun run() {
                if (activeKey?.action != KeyAction.BACKSPACE) return
                listener?.onBackspace()
                deleteInterval = max(32L, (deleteInterval * 0.88f).toLong())
                handler.postDelayed(this, deleteInterval)
            }
        }
        deleteRunnable = task
        handler.postDelayed(task, 380L)
    }

    private fun stopDelete() {
        deleteRunnable?.let(handler::removeCallbacks)
        deleteRunnable = null
    }

    private fun showAlternates(key: KeySpec) {
        val options = if (key.alternates.isNotEmpty()) key.alternates else if (mode == KeyboardMode.ARABIC) {
            listOf("،", "؛", "؟", "!", ":", "…")
        } else {
            listOf(".", ",", "?", "!", ":", ";", "'", "\"")
        }
        val container = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            setPadding(dp(5f).toInt(), dp(5f).toInt(), dp(5f).toInt(), dp(5f).toInt())
            setBackgroundColor(Color.rgb(44, 44, 44))
        }
        options.forEach { value ->
            val item = TextView(context).apply {
                text = value
                textSize = 20f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(dp(9f).toInt(), dp(8f).toInt(), dp(9f).toInt(), dp(8f).toInt())
                setOnClickListener {
                    listener?.onText(value)
                    popup?.dismiss()
                }
            }
            container.addView(item, ViewGroup.LayoutParams(dp(42f).toInt(), dp(48f).toInt()))
        }
        popup?.dismiss()
        popup = PopupWindow(container, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true).apply {
            elevation = dp(8f)
            showAtLocation(this@KeyboardCanvasView, Gravity.TOP or Gravity.START, (downX - dp(30f)).toInt().coerceAtLeast(0), (downY - dp(65f)).toInt().coerceAtLeast(0))
        }
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    override fun onDetachedFromWindow() {
        stopDelete()
        longPressRunnable?.let(handler::removeCallbacks)
        popup?.dismiss()
        super.onDetachedFromWindow()
    }
}
