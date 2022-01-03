package com.luollb.kotlin.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class LoadingView : View {

    //边距
    private val PADDING: Int = 20

    //一段的最大长度
    private val CHIP: Float = 8f
    private val SPACE: Float = 10f

    //线宽度
    private val STROKE_WIDTH: Float = 8f

    private val paintArc = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintChip = Paint(Paint.ANTI_ALIAS_FLAG)

    private val rectF = RectF()

    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var radius: Int = 100 //px

    private var startAngle: Float = 0f
    private var sweepAngle: Float = 2f
    private var degress: Float = 0f

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        paintArc.color = Color.RED
        paintArc.strokeWidth = STROKE_WIDTH
        paintArc.style = Paint.Style.STROKE
        paintArc.strokeCap = Paint.Cap.ROUND

        paintChip.color = Color.RED
        paintChip.strokeWidth = STROKE_WIDTH
        paintChip.style = Paint.Style.STROKE
        paintChip.strokeCap = Paint.Cap.ROUND
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        screenWidth = MeasureSpec.getSize(widthMeasureSpec)
        screenHeight = MeasureSpec.getSize(heightMeasureSpec)
        radius = min(min(screenHeight, screenWidth) / 2, radius)
        setMeasuredDimension(radius * 2 + PADDING * 2, radius * 2 + PADDING * 2)

        rectF.set(
            PADDING.toFloat(), PADDING.toFloat(), (PADDING + radius * 2).toFloat(),
            (PADDING + radius * 2).toFloat()
        )
    }

    override fun onDraw(canvas: Canvas) {

        canvas.save()
        canvas.rotate(degress, (width / 2).toFloat(), (height / 2).toFloat())
        canvas.drawArc(rectF, startAngle, sweepAngle, false, paintArc)

        val residue = 360f - sweepAngle - startAngle
        var chipStart = sweepAngle + SPACE
        chipStart += residue % (CHIP + SPACE)
        val number: Int = (residue / (CHIP + SPACE)).roundToInt()
        var count = 0

        while (residue > (CHIP + SPACE * 2) && chipStart < 360) {
            count++
            paintChip.color = Color.argb(min(255, count * 230 / number), min(255, count * 255 / number), 0, 0)
            canvas.drawArc(rectF, chipStart, CHIP, false, paintChip)
            chipStart += CHIP + SPACE
        }
        canvas.restore()

        if (++degress > 360f) {
            degress = 0f
        }

        if (startAngle == 0f && sweepAngle < 360f) {
            sweepAngle += 2
            if (sweepAngle > 360f) {
                sweepAngle = 360f
            }
        } else {
            startAngle += 2
            sweepAngle = 360f - startAngle
            if (startAngle >= 360f) {
                startAngle = 0f
                sweepAngle = 2f
            }
        }

        invalidate()
    }
}