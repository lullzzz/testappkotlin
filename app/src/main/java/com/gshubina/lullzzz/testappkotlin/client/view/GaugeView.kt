package com.gshubina.lullzzz.testappkotlin.client.view

import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.gshubina.lullzzz.testappkotlin.R
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToLong

class GaugeView : View {

    private val SCALE_ARC_ANGLE = 260.0f
    private val MIN_VALUE_ANGLE = -40
    private val MAX_VALUE_ANGLE = 220
    private val MIN_TEXT_STEP_ANGLE = 26.0f

    private val MAX_TEXT_STEP_COUNT = (SCALE_ARC_ANGLE / MIN_TEXT_STEP_ANGLE).toInt()
    private val MIN_VALUE = 0.0

    private val VALUE_ANIMATION_DURATION = 300

    private val TEXT_TICK_FACTOR = 15

    private var mArrowColor = 0
    private var mTextColor = 0
    private var mTickColor = 0
    private var mUnitString: String? = null

    private var mMaxValue = 0f
    private var mCurrentValue = 0.0

    private lateinit var mGaugeContainer: RectF
    private lateinit var mValueTextContainer: RectF

    private var mTextTickStep = 0f
    private var mTextTickCount: Long = 0
    private var mTickCount: Long = 0
    private var mScaleFactor = 1f
    private var mScaleFactorText: String? = null

    private var mCashedBitmap: Bitmap? = null
    private lateinit var mCashedCanvas: Canvas

    private var mTicksPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mRimPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mArrowPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mTextPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mBackground1Paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mBackground2Paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mTextMarkPointArray = FloatArray(0)
    private var mMarkPointArray = FloatArray(0)
    private var mTextMarkStepAngle = 0f
    private val mMarkLabelArray = ArrayList<Label>()

    private var mBackground1GradientColors = IntArray(0)
    private var mBackground2GradientColors = IntArray(0)

    private val mValueAnimator = ValueAnimator.ofObject(
        TypeEvaluator { fraction: Float,
                        startValue: Double,
                        endValue: Double ->
            startValue + (endValue - startValue) * fraction
        } as TypeEvaluator<Double>,
        0,
        0)

    constructor(context: Context?) : super(context) {
        init(null, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.GaugeView, defStyle, 0
        )
        mMaxValue = a.getInt(
            R.styleable.GaugeView_maxValue, 0
        ).toFloat()
        mTickColor = a.getColor(
            R.styleable.GaugeView_tickColor,
            resources.getColor(R.color.default_tick_color, context.theme)
        )
        mArrowColor = a.getColor(
            R.styleable.GaugeView_arrowColor,
            resources.getColor(R.color.default_arrow_color, context.theme)
        )
        mTextColor = a.getColor(
            R.styleable.GaugeView_textColor,
            resources.getColor(R.color.default_text_color, context.theme)
        )
        mUnitString = a.getString(R.styleable.GaugeView_unit)
        a.recycle()
        mValueAnimator.duration = VALUE_ANIMATION_DURATION.toLong()
        mValueAnimator.addUpdateListener { updatedAnimation: ValueAnimator ->
            val animatedValue = updatedAnimation.animatedValue as Double
            setCurrentValueUi(animatedValue)
        }
        calculateTickArrays()

        mTicksPaint.style = Paint.Style.STROKE
        mTicksPaint.color = mTickColor

        mRimPaint.style = Paint.Style.STROKE
        mRimPaint.color = mTickColor

        mTextPaint.color = mTextColor
        mTextPaint.textAlign = Paint.Align.CENTER
        mTextPaint.isLinearText = true

        mBackground1Paint.style = Paint.Style.FILL
        mBackground1Paint.color = Color.BLACK

        mBackground2Paint.style = Paint.Style.FILL
        mBackground2Paint.color = Color.WHITE

        mArrowPaint.style = Paint.Style.FILL
        mArrowPaint.color = mArrowColor
        mArrowPaint.strokeCap = Paint.Cap.ROUND
        mBackground1GradientColors = intArrayOf(
            resources.getColor(R.color.background1_gradient_start_color, context.theme),
            resources.getColor(R.color.background1_gradient_center_color, context.theme),
            resources.getColor(R.color.background1_gradient_end_color, context.theme)
        )
        mBackground2GradientColors = intArrayOf(
            resources.getColor(R.color.background2_gradient_start_color, context.theme),
            resources.getColor(R.color.background2_gradient_center_color, context.theme),
            resources.getColor(R.color.background2_gradient_center1_color, context.theme),
            resources.getColor(R.color.background2_gradient_end_color, context.theme),
            resources.getColor(R.color.background2_gradient_center1_color, context.theme),
            resources.getColor(R.color.background2_gradient_center_color, context.theme),
            resources.getColor(R.color.background2_gradient_start_color, context.theme)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val paddingStart = if (paddingStart == 0) paddingLeft else paddingStart
        val paddingEnd = if (paddingEnd == 0) paddingRight else paddingEnd
        mGaugeContainer = RectF(
            paddingStart.toFloat(), paddingTop.toFloat(),
            (width - paddingEnd).toFloat(), (height - paddingBottom).toFloat()
        )
        val dim = Math.min(mGaugeContainer.height(), mGaugeContainer.width())
        val diffW = mGaugeContainer.width() - dim
        val diffH = mGaugeContainer.height() - dim
        mGaugeContainer.left += diffW / 2f
        mGaugeContainer.right -= diffW / 2f
        mGaugeContainer.top += diffH / 2f
        mGaugeContainer.bottom -= diffH / 2f
        if (mCashedBitmap != null) {
            mCashedBitmap!!.recycle()
        }
        mCashedBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCashedCanvas = Canvas(mCashedBitmap!!)
        mValueTextContainer = scale(mGaugeContainer, 0.87f)
        mTextPaint.textSize = mGaugeContainer.width() / TEXT_TICK_FACTOR
        mArrowPaint.strokeWidth = mGaugeContainer.width() / (5 * TEXT_TICK_FACTOR)
        mTicksPaint.strokeWidth = mGaugeContainer.width() / (10 * TEXT_TICK_FACTOR)
        mRimPaint.strokeWidth = mGaugeContainer.width() / (10 * TEXT_TICK_FACTOR)
        drawBackground(mCashedCanvas)
        createTickPathArray()
        drawTicks(mCashedCanvas)
        if (mUnitString != null) {
            mCashedCanvas.drawText(
                mUnitString!!,
                mValueTextContainer.centerX(),
                mValueTextContainer.centerY() + mValueTextContainer.height() / 3,
                mTextPaint
            )
        }
        if (mScaleFactorText != null) {
            mCashedCanvas.drawText(
                "x$mScaleFactorText",
                mValueTextContainer.centerX(),
                mValueTextContainer.centerY() - mValueTextContainer.height() / 6,
                mTextPaint
            )
        }
        mCashedBitmap!!.prepareToDraw()
    }

    private fun drawBackground(cashCanvas: Canvas) {
        mBackground2Paint.color = Color.BLACK
        cashCanvas.drawArc(mGaugeContainer, 0f, 360f, true, mBackground2Paint)
        val background1Gradient = LinearGradient(
            mGaugeContainer.left, mGaugeContainer.top,
            mGaugeContainer.right, mGaugeContainer.bottom,
            mBackground1GradientColors, null, Shader.TileMode.MIRROR
        )
        mBackground1Paint.shader = background1Gradient
        cashCanvas.drawArc(scale(mGaugeContainer, 0.99f), 0f, 360f, true, mBackground1Paint)
        mBackground2Paint.color = Color.WHITE
        cashCanvas.drawArc(scale(mGaugeContainer, 0.97f), 0f, 360f, true, mBackground2Paint)
        mBackground2Paint.color = Color.BLACK
        cashCanvas.drawArc(scale(mGaugeContainer, 0.96f), 0f, 360f, true, mBackground2Paint)
        cashCanvas.drawArc(scale(mGaugeContainer, 0.95f), 0f, 360f, true, mBackground1Paint)
        mBackground2Paint.color = Color.BLACK
        cashCanvas.drawArc(scale(mGaugeContainer, 0.93f), 0f, 360f, true, mBackground2Paint)
        val background6Rect = scale(mGaugeContainer, 0.92f)
        val background2Gradient = SweepGradient(
            background6Rect.centerX(), background6Rect.centerY(),
            mBackground2GradientColors, null
        )
        mBackground1Paint.shader = background2Gradient
        cashCanvas.drawArc(background6Rect, 0f, 360f, true, mBackground1Paint)
        mBackground2Paint.color = Color.BLACK
        cashCanvas.drawArc(scale(mGaugeContainer, 0.15f), 0f, 360f, true, mBackground2Paint)
        mBackground2Paint.color = Color.GRAY
        cashCanvas.drawArc(scale(mGaugeContainer, 0.14f), 0f, 360f, true, mBackground2Paint)
        cashCanvas.drawArc(scale(mGaugeContainer, 0.12f), 0f, 360f, true, mBackground1Paint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCashedBitmap!!, 0f, 0f, null)
        drawArrow(canvas)
    }

    private fun calculateTickArrays() {
        /* How many units could be in one step */
        val unitsPerTickStep = mMaxValue / MAX_TEXT_STEP_COUNT
        if (unitsPerTickStep == 1f) { // case when we have max value equals MAX_TEXT_STEP_COUNT
            mTextTickCount = (MAX_TEXT_STEP_COUNT + 1).toLong() // plus zero tick
            mTickCount = MAX_TEXT_STEP_COUNT.toLong() // small marks (without text)
            mTextTickStep = 1f
            mTextMarkStepAngle = MIN_TEXT_STEP_ANGLE
        } else {
            if (unitsPerTickStep > 1) {
                val factor =
                    if (unitsPerTickStep < 5) 2 else 5 //one tick step will be a multiple of 2 or 5 units
                // units per factor step
                var y = unitsPerTickStep / factor.toDouble()
                y = ceil(y)
                mTextTickStep = (y * factor).toFloat()
                mTextTickCount = Math.ceil(mMaxValue / mTextTickStep.toDouble()).toLong()
                mTickCount = mTextTickCount
                // new max value if need
                mMaxValue = mTextTickCount * mTextTickStep
                mTextMarkStepAngle = SCALE_ARC_ANGLE / mTextTickCount
                ++mTextTickCount // plus zero tick
                //scale factor
                if (y > 10) {
                    mScaleFactor = calculateScaleFactor(mMaxValue.toDouble())
                    mScaleFactorText = mScaleFactor.toString()
                    mMaxValue /=  mScaleFactor
                    mTextTickStep /=  mScaleFactor
                }
            } else {
                mScaleFactor = calculateNegativeScaleFactor(mMaxValue.toDouble(), mScaleFactor)
                val scale = 1 / mScaleFactor
                mScaleFactorText = if (scale > 1) "10^-" + scale.toInt() / 10 else null
                val factor1 =
                    if (unitsPerTickStep < 0.5f) 0.2f else 0.5f //one tick step will be a multiple of 0.2 or 0.5 units
                // units per factor step
                val y = unitsPerTickStep / factor1
                mTextTickStep = Math.ceil(y.toDouble()).toInt() * factor1
                mTextTickCount = Math.ceil(mMaxValue / mTextTickStep.toDouble()).toLong()
                mTickCount = mTextTickCount
                mMaxValue = mMaxValue / mScaleFactor
                // new max value if need
                mMaxValue = mTextTickCount * mTextTickStep
                mTextMarkStepAngle = SCALE_ARC_ANGLE / mTextTickCount
                ++mTextTickCount // plus zero tick
            }
        }
    }

    private fun calculateScaleFactor(value: Double): Float {
        var calc_value = value
        return if (calc_value < 100) {
            1f
        } else {
            mScaleFactor *= 10f
            calc_value = calc_value / mScaleFactor
            mScaleFactor * calculateScaleFactor(calc_value)
        }
    }

    private fun calculateNegativeScaleFactor(value: Double, factor: Float): Float {
        var calc_value = value
        var calc_factor = factor
        return if (calc_value == 0.0 || calc_value >= 1) {
            calc_factor
        } else {
            calc_factor /= 10.0f
            calc_value = calc_value * 10.0f
            calculateNegativeScaleFactor(calc_value, calc_factor)
        }
    }

    private fun drawTicks(canvas: Canvas) {
        canvas.drawLines(mTextMarkPointArray, mTicksPaint)
        canvas.drawLines(mMarkPointArray, mTicksPaint)
        for (valueLabel in mMarkLabelArray) {
            canvas.drawText(valueLabel.text!!, valueLabel.x, valueLabel.y, mTextPaint)
        }
        canvas.drawArc(
            mValueTextContainer, (-1 * MIN_VALUE_ANGLE).toFloat(), -1 * SCALE_ARC_ANGLE, false,
            mRimPaint
        )
    }

    private fun drawArrow(canvas: Canvas) {
        val arrowRadius = mValueTextContainer.width() * 0.48f
        val shank = scale(mValueTextContainer, 0.03f)
        val angle = MIN_VALUE_ANGLE + (getCurrentValue() / mMaxValue * 260).toFloat()
        canvas.drawLine(
            (mValueTextContainer.centerX() + Math.cos((180 - angle) / 180 * Math.PI)).toFloat(),
            (mValueTextContainer.centerY() - Math.sin(angle / 180 * Math.PI)).toFloat(),
            (mValueTextContainer.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * arrowRadius).toFloat(),
            (mValueTextContainer.centerY() - Math.sin(angle / 180 * Math.PI) * arrowRadius).toFloat(),
            mArrowPaint
        )
        canvas.drawArc(shank, 0f, 360f, true, mArrowPaint)
    }

    private fun createTickPathArray() {
        val minorStep = mTextMarkStepAngle / 2
        val textTicksLength = mGaugeContainer.width() / TEXT_TICK_FACTOR
        val ticksLength = textTicksLength / 2
        var currentAngle = MIN_VALUE_ANGLE.toFloat()
        var curProgress = 0f
        val radius = mValueTextContainer.width() / 2
        mTextMarkPointArray = FloatArray((mTextTickCount * 4).toInt())
        mMarkPointArray = FloatArray((mTickCount * 4).toInt())
        mMarkLabelArray.clear()
        var i = -1
        var k = -1
        while (currentAngle <= MAX_VALUE_ANGLE) {
            if (mTextMarkPointArray.size > 0) {
                mTextMarkPointArray[++i] =
                    (mValueTextContainer.centerX() + Math.cos((180 - currentAngle) / 180 * Math.PI) * (radius - textTicksLength)).toFloat()
                mTextMarkPointArray[++i] =
                    (mValueTextContainer.centerY() - Math.sin(currentAngle / 180 * Math.PI) * (radius - textTicksLength)).toFloat()
                mTextMarkPointArray[++i] =
                    (mValueTextContainer.centerX() + Math.cos((180 - currentAngle) / 180 * Math.PI) * radius).toFloat()
                mTextMarkPointArray[++i] =
                    (mValueTextContainer.centerY() - Math.sin(currentAngle / 180 * Math.PI) * radius).toFloat()
                val txtX =
                    (mValueTextContainer.centerX() + Math.cos((180 - currentAngle) / 180 * Math.PI)
                            * (radius - textTicksLength - mGaugeContainer.width() / TEXT_TICK_FACTOR)).toFloat()
                val txtY = (mValueTextContainer.centerY() - Math.sin(currentAngle / 180 * Math.PI)
                        * (radius - textTicksLength - mGaugeContainer.width() / TEXT_TICK_FACTOR)).toFloat()
                mMarkLabelArray.add(Label(curProgress, txtX, txtY))
            }
            val angle = currentAngle + minorStep
            if (angle >= MAX_VALUE_ANGLE) {
                break
            }
            if (mMarkPointArray.size > 0) {
                mMarkPointArray[++k] =
                    (mValueTextContainer.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * (radius - ticksLength)).toFloat()
                mMarkPointArray[++k] =
                    (mValueTextContainer.centerY() - Math.sin(angle / 180 * Math.PI) * (radius - ticksLength)).toFloat()
                mMarkPointArray[++k] =
                    (mValueTextContainer.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * radius).toFloat()
                mMarkPointArray[++k] =
                    (mValueTextContainer.centerY() - Math.sin(angle / 180 * Math.PI) * radius).toFloat()
            }
            currentAngle += mTextMarkStepAngle
            curProgress += mTextTickStep
        }
    }

    private fun scale(rect: RectF?, factor: Float): RectF {
        val result = RectF(rect)
        val diffHorizontal = (result.right - result.left) * (factor - 1f)
        val diffVertical = (result.bottom - result.top) * (factor - 1f)
        result.top -= diffVertical / 2f
        result.bottom += diffVertical / 2f
        result.left -= diffHorizontal / 2f
        result.right += diffHorizontal / 2f
        return result
    }

    inner class Label(value: Float, x: Float, y: Float) {
        var text: String? = null
        val x: Float
        val y: Float

        init {
            if (mScaleFactor > 1 || mMaxValue >= 10) {
                text = Math.round(value).toString()
            } else {
                text = String.format("%.1f", value)
            }
            this.x = x
            this.y = y
        }
    }

    fun getCurrentValue() = mCurrentValue

    fun setCurrentValue(value: Double) {
        var calc_value = value
        calc_value /= mScaleFactor
        require(!(calc_value < MIN_VALUE || calc_value > mMaxValue)) { "Value is out of bounds" }
        if (mValueAnimator != null) {
            if (mValueAnimator.isStarted) mValueAnimator.cancel()
            mValueAnimator.setObjectValues(mCurrentValue, calc_value)
            mValueAnimator.start()
        }
    }

    private fun setCurrentValueUi(valueUi: Double) {
        mCurrentValue = valueUi
        invalidate()
    }
}