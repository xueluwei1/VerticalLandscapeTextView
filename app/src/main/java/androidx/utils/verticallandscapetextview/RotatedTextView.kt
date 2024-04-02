package androidx.utils.verticallandscapetextview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.text.LineBreaker
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View

class RotatedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mTextColor: Int
    private var mTextSize: Float
    private var mFontFamily: Typeface
    private var mText: String
    private var mIsRtl: Boolean = false

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.RotatedTextView)
        mTextColor = array.getColor(R.styleable.RotatedTextView_android_textColor, 0)
        mTextSize = array.getDimension(R.styleable.RotatedTextView_android_textSize, 0f)
        mFontFamily = array.getString(R.styleable.RotatedTextView_android_fontFamily)?.let {
            Typeface.create(it, Typeface.NORMAL)
        } ?: Typeface.DEFAULT
        mText = array.getString(R.styleable.RotatedTextView_android_text) ?: ""
        array.recycle()
    }

    fun setRotatedTextColor(rotatedTextColor: Int) {
        this.mTextColor = rotatedTextColor
        invalidate()
    }

    fun setTextSize(textSize: Float) {
        this.mTextSize = textSize
        invalidate()
    }

    fun setFontFamily(fontFamily: Typeface) {
        this.mFontFamily = fontFamily
        invalidate()
    }

    fun setText(text: String) {
        this.mText = text
        invalidate()
    }

    fun setRTL(isRtl: Boolean) {
        this.mIsRtl = isRtl
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        drawText(canvas)
    }

    private fun drawText(canvas: Canvas) {
        canvas.translate(0f, 0f)
        canvas.save()
        val textBitmap = getAutoSizeTextBitmap(
            height,
            width,
            mText,
            mTextColor,
            mFontFamily
        )
        val rotator = Matrix()
        if (mIsRtl) {
            rotator.postRotate(270f, 0f, 0f)
            rotator.postTranslate(0f, height.toFloat())
        } else {
            rotator.postRotate(90f, 0f, 0f)
            rotator.postTranslate(width.toFloat(), 0f)
        }
        canvas.drawBitmap(textBitmap, rotator, null)
        textBitmap.recycle()
        canvas.restore()
    }

    private fun getAutoSizeTextBitmap(
        width: Int,
        height: Int,
        text: String,
        color: Int,
        typeface: Typeface
    ): Bitmap {
        var textSize = mTextSize
        var textLayout = getTextLayout(width, text, color, textSize, typeface)
        while (textLayout.height > height) {
            textSize -= 1f
            textLayout = getTextLayout(width, text, color, textSize, typeface)
        }
        return getTextBitmap(width, height, textLayout)
    }

    private fun getTextBitmap(
        width: Int,
        height: Int,
        textLayout: StaticLayout
    ): Bitmap {
        val myBitmap = generateBitmap(width, height)
        val myCanvas = Canvas(myBitmap)
        myCanvas.translate(width / 2f, (height - textLayout.height) / 2f)
        textLayout.draw(myCanvas)
        return myBitmap
    }

    private fun generateBitmap(width: Int, height: Int): Bitmap {
        return try {
            Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
            )
        } catch (e: OutOfMemoryError) {
            generateBitmap(width / 2, height / 2)
        }
    }

    private fun getTextLayout(
        width: Int,
        text: String,
        color: Int,
        size: Float,
        typeface: Typeface
    ): StaticLayout {
        val paint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)
        paint.color = color
        paint.typeface = typeface
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = size
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setTextDirection(TextDirectionHeuristics.LTR)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setJustificationMode(LineBreaker.JUSTIFICATION_MODE_NONE)
                }
            }
            .build()
    }

}