package com.ramazan.simple.scaleview

import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.core.content.ContextCompat

class ScaleView@JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0):
    LinearLayout(context, attributeSet, defStyleAttr) {

    private val mp = ViewGroup.LayoutParams.MATCH_PARENT
    private val wc = ViewGroup.LayoutParams.WRAP_CONTENT
    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    private var motionTouchEventX = 0f
    private var currentX = 0f

    val progressMaxValue = 20f
    val progressMinValue = 0f

    private val segmentsCount = 11
    private val segments = ArrayList<FrameLayout>(segmentsCount)
    private val mainSegmentsCount = 3

    private var cursor: View? = null
    private var cursorLabel: TextView? = null

    init {
        orientation = VERTICAL
        val frameLayout = FrameLayout(context)

        frameLayout.layoutParams = FrameLayout.LayoutParams(mp, wc)

        addView(frameLayout)

        addBackground(frameLayout)

        addHint(frameLayout)

        addSegments(frameLayout)

        addSegmentLabels()

        addCursor(frameLayout)

        addCursorLabel()

        setOnTouchListener(getButtonTouchListener())
    }

    private fun addCursorLabel() {
        cursorLabel = TextView(context).apply {
            textSize = 18f
            text = "0.0"
        }
        addView(cursorLabel, 0)

        cursorLabel?.layoutParams = LayoutParams(wc, wc)
    }

    private fun addCursor(frameLayout: FrameLayout) {
        cursor = View(context)
        frameLayout.addView(cursor)

        cursor?.layoutParams = FrameLayout.LayoutParams(
            context.toPx(8f),
            context.toPx(32f)
        )
        cursor?.background = ContextCompat.getDrawable(context, R.drawable.button_bg)

    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        when{
            isOutOfMin() -> {
                cursor?.x = getCursorMinX()
                updateCursorLabelX()
            }
            isOutOfMax() -> {
                cursor?.x = getCursorMaxX()
                updateCursorLabelX()
            }
        }
    }

    private fun updateCursorLabelValue(
        cursor: View,
        cursorLabel: TextView
    ) {
        if(segments.isEmpty()) return

        val segmentWidth = segments.last().width
        val totalProgress = width.toFloat() - segmentWidth
        val currentProgress = cursor.x + (cursor.width / 2) - (segmentWidth / 2)

        if(totalProgress > 0){
            val progress = (progressMaxValue * currentProgress / totalProgress).toInt()
            cursorLabel.text = "$progress"
        }
    }

    private fun updateCursorLabelX() {
        cursor?.let { cursor ->
            cursorLabel?.let { cursorLabel ->
                updateCursorLabelValue(cursor, cursorLabel)
                val cursorCenterX = cursor.width.toFloat() / 2
                val cursorLabelCenterX = (cursorLabel.width.toFloat() / 2)
                cursorLabel.x = cursor.x + cursorCenterX - cursorLabelCenterX
            }
        }
    }

    private fun addSegmentLabels() {
        val space = Space(context)
        addView(space)
        space.layoutParams = LayoutParams(mp, context.toPx(4f))

        val container = LinearLayout(context)
        container.orientation = HORIZONTAL
        addView(container)
        container.layoutParams = LayoutParams(mp, wc).apply {
            gravity = Gravity.CENTER
        }

        var counter = 0
        for(i in 0 until segmentsCount){
            when(i){
                0, (segmentsCount / 2), segmentsCount - 1 -> {
                    val label = TextView(context).apply {
                        text = "$counter"
                        textSize = 18f
                        setHorizontalGravity(Gravity.CENTER)
                        setVerticalGravity(Gravity.CENTER)
                        textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    }
                    counter += 10
                    container.addView(label)
                    label.layoutParams = LayoutParams(0, wc, 1f).apply {
                        gravity = Gravity.CENTER
                    }

                }
                else -> {
                    val label = View(context)
                    container.addView(label)
                    label.layoutParams = LayoutParams(0, 0, 1f).apply {
                        gravity = Gravity.CENTER
                    }
                }
            }
        }
    }

    private fun addSegments(frameLayout: FrameLayout) {
        val container = LinearLayout(context)
        container.orientation = HORIZONTAL
        container.isBaselineAligned = false
        frameLayout.addView(container)
        container.layoutParams = FrameLayout.LayoutParams(mp, context.toPx(28f), Gravity.CENTER)
        segments.clear()
        for(i in 0 until segmentsCount){
            val segment = FrameLayout(context)
            segments.add(segment)
            container.addView(segment)
            segment.layoutParams  = LayoutParams(0, mp, 1f).apply {
                gravity = Gravity.CENTER
            }

            val segmentIndicator = View(context)
            segment.addView(segmentIndicator)
            when(i){
                0, (segmentsCount / 2), segmentsCount - 1 -> {
                    segmentIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.main_line_color))
                    segmentIndicator.layoutParams = FrameLayout.LayoutParams(context.toPx(2f), mp, Gravity.CENTER)
                }
                else -> {
                    segmentIndicator.layoutParams = FrameLayout.LayoutParams(context.toPx(1f), mp, Gravity.CENTER)
                    segmentIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.line_color))
                }
            }
        }

    }

    private fun addHint(frameLayout: FrameLayout) {
        val container = LinearLayout(context)
        container.orientation = HORIZONTAL
        frameLayout.addView(container)
        container.layoutParams = FrameLayout.LayoutParams(mp, context.toPx(24f), Gravity.CENTER)
        for(i in 0 until mainSegmentsCount){
            val v = View(context)
            container.addView(v)
            v.layoutParams  = LayoutParams(0, mp, 1f).apply {
                gravity = Gravity.CENTER
            }

            if(i == mainSegmentsCount / 2){
                v.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
            }

        }

    }

    private fun addBackground(frameLayout: FrameLayout) {
        val background = View(context)
        frameLayout.addView(background)
        background.layoutParams = FrameLayout.LayoutParams(mp, context.toPx(24f),Gravity.CENTER)
        background.background = ContextCompat.getDrawable(context, R.drawable.shape_rounded)
    }

    private fun getButtonTouchListener(): OnTouchListener? {
        return OnTouchListener { v, event ->

            motionTouchEventX = event.x

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    onStartTouch()
                    return@OnTouchListener true
                }
                MotionEvent.ACTION_MOVE -> {
                    onMove(event)
                    return@OnTouchListener true
                }
                MotionEvent.ACTION_UP -> {
                    onRelease(event)
                    return@OnTouchListener true
                }
            }
            false
        }
    }

    private fun onStartTouch() {
        currentX = motionTouchEventX
    }

    private fun onRelease(event: MotionEvent) {

        cursor?.x =  when{
            isOutOfMin() -> getCursorMinX()
            isOutOfMax() -> getCursorMaxX()
            else -> event.x
        }

        updateCursorLabelX()

    }

    private fun onMove(event: MotionEvent) {

        val dx = Math.abs(motionTouchEventX - currentX)

        if(dx < touchTolerance)  return

        cursor?.let { cursor ->
            //follow the finger of the user
            if (getCursorMinX() - (cursor.width / 2) < event.x  &&  event.x < getCursorMaxX() + (cursor.width / 2)) {
                cursor.x = event.x

            }

            cursor.x =  when{
                isOutOfMin() -> getCursorMinX()
                isOutOfMax() -> getCursorMaxX()
                else -> cursor.x
            }
            updateCursorLabelX()

        }

    }

    private fun getCursorMinX(): Float {
        return cursor?.let {
            if(segments.isNotEmpty()){
                val btnCenterX = it.width.toFloat() / 2
                val segment = segments.first()
                val segmentCenterX = segment.width.toFloat() / 2

                segment.x + segmentCenterX - btnCenterX
            } else 0f
        }?:  0f
    }

    private fun getCursorMaxX(): Float {
        return cursor?.let {
            if(segments.isNotEmpty()){
                val btnCenterX = it.width.toFloat() / 2
                val segment = segments.last()
                val segmentCenterX = segment.width.toFloat() / 2
                segment.x + segmentCenterX - btnCenterX
            } else 0f
        }?:  0f
    }

    private fun isOutOfMax(): Boolean {
        return cursor?.let {
            if(segments.isNotEmpty()) {
                val lastSegment = segments.last()
                val segmentCenterX = lastSegment.x + (lastSegment.width.toFloat() / 2)
                val btnLeftX = it.x
                val btnRightX = it.x + it.width
                val btnCenterX = (it.x + it.width / 2)
                (btnRightX > segmentCenterX && btnCenterX > segmentCenterX) || btnLeftX > segmentCenterX
            } else false
        }?:false
    }

    private fun isOutOfMin(): Boolean {
        return cursor?.let {
            if(segments.isNotEmpty()) {
                val segment = segments.first()
                val segmentCenterX = segment.x + (segment.width.toFloat() / 2)
                val btnLeftX = it.x
                val btnCenterX = (it.x + it.width / 2)
                val btnRightX = it.x + it.width
                (btnLeftX < segmentCenterX && btnCenterX < segmentCenterX) || btnRightX < segmentCenterX
            } else false
        }?:false
    }
}