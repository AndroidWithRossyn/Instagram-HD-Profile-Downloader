package com.instagram.hdprofile.downloader.customview

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import com.instagram.hdprofile.downloader.R


import kotlin.math.min
import kotlin.math.pow
/**
 * A custom `ImageView` that displays images in a circular shape, with additional features like
 * borders, circle background color, and support for padding adjustments. This view extends
 * `AppCompatImageView` to provide backward compatibility for older Android versions.
 *
 * It allows you to set various properties, including border width, border color, and circle
 * background color, through XML attributes or programmatically. The image scaling is fixed to
 * `CENTER_CROP` to ensure the image fits within the circular frame.
 *
 * The view also provides options to disable the circular transformation and overlay the border
 * on top of the image.
 *
 * Constructors:
 * - `CircleImageView(Context context)`: Creates the view programmatically with default settings.
 * - `CircleImageView(Context context, AttributeSet attrs, int defStyle)`: Creates the view
 *    programmatically with attributes specified in XML.
 */
class CircleImageView : AppCompatImageView {
    companion object {
        /**
         * The default scale type for the image, ensuring it fits within the circular frame.
         */
        private val SCALE_TYPE: ScaleType = ScaleType.CENTER_CROP
        /**
         * The bitmap configuration for images, using ARGB_8888 for high-quality images.
         */
        private val BITMAP_CONFIG = Bitmap.Config.ARGB_8888
        /**
         * The default dimension for `ColorDrawable` instances.
         */
        private const val COLORDRAWABLE_DIMENSION = 2
        /**
         * The default border width in pixels.
         */
        private const val DEFAULT_BORDER_WIDTH = 0
        /**
         * The default border color, set to black.
         */
        private const val DEFAULT_BORDER_COLOR = Color.BLACK
        /**
         * The default background color for the circle, set to transparent.
         */
        private const val DEFAULT_CIRCLE_BACKGROUND_COLOR = Color.TRANSPARENT
        /**
         * The default image alpha, set to fully opaque.
         */
        private const val DEFAULT_IMAGE_ALPHA = 255
        /**
         * The default setting for border overlay, which determines whether the border is drawn
         * on top of the image.
         */
        private const val DEFAULT_BORDER_OVERLAY = false
    }

    private val mDrawableRect = RectF()
    private val mBorderRect = RectF()

    private val mShaderMatrix = Matrix()
    private val mBitmapPaint = Paint()
    private val mBorderPaint = Paint()
    private val mCircleBackgroundPaint = Paint()

    private var mBorderColor = DEFAULT_BORDER_COLOR
    private var mBorderWidth = DEFAULT_BORDER_WIDTH
    private var mCircleBackgroundColor = DEFAULT_CIRCLE_BACKGROUND_COLOR
    private var mImageAlpha = DEFAULT_IMAGE_ALPHA

    private var mBitmap: Bitmap? = null
    private var mBitmapCanvas: Canvas? = null

    private var mDrawableRadius = 0f
    private var mBorderRadius = 0f

    private var mColorFilter: ColorFilter? = null

    private var mInitialized = false
    private var mRebuildShader = false
    private var mDrawableDirty = false

    private var mBorderOverlay = false
    private var mDisableCircularTransformation = false
    /**
     * Constructor that initializes the view programmatically.
     *
     * @param context The context of the application.
     */
    constructor(context: Context?) : super(context!!) {
        init()
    }
    /**
     * Constructor that initializes the view with attributes specified in XML.
     *
     * @param context The context of the application.
     * @param attrs The attribute set containing the XML attributes.
     * @param defStyle The default style to apply to this view.
     */
    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int = 0)
            : super(context, attrs, defStyle) {
        val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyle, 0)

        mBorderWidth = a.getDimensionPixelSize(
            R.styleable.CircleImageView_civ_border_width,
            DEFAULT_BORDER_WIDTH
        )
        mBorderColor =
            a.getColor(R.styleable.CircleImageView_civ_border_color, DEFAULT_BORDER_COLOR)
        mBorderOverlay =
            a.getBoolean(R.styleable.CircleImageView_civ_border_overlay, DEFAULT_BORDER_OVERLAY)
        mCircleBackgroundColor = a.getColor(
            R.styleable.CircleImageView_civ_circle_background_color,
            DEFAULT_CIRCLE_BACKGROUND_COLOR
        )

        a.recycle()

        init()
    }
    /**
     * Initializes the custom view by setting up paint objects and scale type.
     */
    private fun init() {
        mInitialized = true

        super.setScaleType(SCALE_TYPE)

        mBitmapPaint.isAntiAlias = true
        mBitmapPaint.isDither = true
        mBitmapPaint.isFilterBitmap = true
        mBitmapPaint.alpha = mImageAlpha
        mBitmapPaint.setColorFilter(mColorFilter)

        mBorderPaint.style = Paint.Style.STROKE
        mBorderPaint.isAntiAlias = true
        mBorderPaint.color = mBorderColor
        mBorderPaint.strokeWidth = mBorderWidth.toFloat()

        mCircleBackgroundPaint.style = Paint.Style.FILL
        mCircleBackgroundPaint.isAntiAlias = true
        mCircleBackgroundPaint.color = mCircleBackgroundColor

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            outlineProvider =
                OutlineProvider()
        }
    }
    /**
     * Sets the scale type of the view. Only `CENTER_CROP` is supported.
     *
     * @param scaleType The scale type to set.
     */
    override fun setScaleType(scaleType: ScaleType) {
        if (scaleType != SCALE_TYPE) {
            throw IllegalArgumentException("ScaleType $scaleType not supported.")
        }
    }
    /**
     * Overrides the ability to adjust view bounds, which is not supported in this view.
     *
     * @param adjustViewBounds Should always be false.
     */
    override fun setAdjustViewBounds(adjustViewBounds: Boolean) {
        require(!adjustViewBounds) { "adjustViewBounds not supported." }
    }
    /**
     * Draws the circular image, background, and border.
     *
     * @param canvas The canvas on which to draw.
     */
    @SuppressLint("CanvasSize")
    override fun onDraw(canvas: Canvas) {
        if (mDisableCircularTransformation) {
            super.onDraw(canvas)
            return
        }

        if (mCircleBackgroundColor != Color.TRANSPARENT) {
            canvas.drawCircle(
                mDrawableRect.centerX(),
                mDrawableRect.centerY(),
                mDrawableRadius,
                mCircleBackgroundPaint
            )
        }

        if (mBitmap != null) {
            if (mDrawableDirty && mBitmapCanvas != null) {
                mDrawableDirty = false
                val drawable = drawable
                drawable.setBounds(0, 0, mBitmapCanvas!!.width, mBitmapCanvas!!.height)
                drawable.draw(mBitmapCanvas!!)
            }

            if (mRebuildShader) {
                mRebuildShader = false

                val bitmapShader =
                    BitmapShader(mBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                bitmapShader.setLocalMatrix(mShaderMatrix)

                mBitmapPaint.setShader(bitmapShader)
            }

            canvas.drawCircle(
                mDrawableRect.centerX(),
                mDrawableRect.centerY(),
                mDrawableRadius,
                mBitmapPaint
            )
        }

        if (mBorderWidth > 0) {
            canvas.drawCircle(
                mBorderRect.centerX(),
                mBorderRect.centerY(),
                mBorderRadius,
                mBorderPaint
            )
        }
    }
    /**
     * Marks the drawable as dirty, indicating it needs to be redrawn.
     *
     * @param dr The drawable that is being invalidated.
     */
    override fun invalidateDrawable(dr: Drawable) {
        mDrawableDirty = true
        invalidate()
    }
    /**
     * Updates the dimensions when the size of the view changes.
     *
     * @param w The new width of the view.
     * @param h The new height of the view.
     * @param oldw The old width of the view.
     * @param oldh The old height of the view.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateDimensions()
        invalidate()
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        updateDimensions()
        invalidate()
    }

    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        super.setPaddingRelative(start, top, end, bottom)
        updateDimensions()
        invalidate()
    }

    /**
     * Sets the border color for the circular image.
     *
     * @param borderColor The color of the border.
     */
    var borderColor: Int
        get() = mBorderColor
        set(borderColor) {
            if (borderColor == mBorderColor) {
                return
            }

            mBorderColor = borderColor
            mBorderPaint.color = borderColor
            invalidate()
        }
    /**
     * Sets the background color for the circular image.
     *
     * @param circleBackgroundColor The background color for the circle.
     */
    var circleBackgroundColor: Int
        get() = mCircleBackgroundColor
        set(circleBackgroundColor) {
            if (circleBackgroundColor == mCircleBackgroundColor) {
                return
            }

            mCircleBackgroundColor = circleBackgroundColor
            mCircleBackgroundPaint.color = circleBackgroundColor
            invalidate()
        }



    fun setCircleBackgroundColorResource(@ColorRes circleBackgroundRes: Int) {
        circleBackgroundColor = context.resources.getColor(circleBackgroundRes, context.theme)
    }
    /**
     * Sets the border width for the circular image.
     *
     * @param borderWidth The width of the border in pixels.
     */
    var borderWidth: Int
        get() = mBorderWidth
        set(borderWidth) {
            if (borderWidth == mBorderWidth) {
                return
            }

            mBorderWidth = borderWidth
            mBorderPaint.strokeWidth = borderWidth.toFloat()
            updateDimensions()
            invalidate()
        }

    var isBorderOverlay: Boolean
        get() = mBorderOverlay
        set(borderOverlay) {
            if (borderOverlay == mBorderOverlay) {
                return
            }

            mBorderOverlay = borderOverlay
            updateDimensions()
            invalidate()
        }

    var isDisableCircularTransformation: Boolean
        get() = mDisableCircularTransformation
        set(disableCircularTransformation) {
            if (disableCircularTransformation == mDisableCircularTransformation) {
                return
            }

            mDisableCircularTransformation = disableCircularTransformation

            if (disableCircularTransformation) {
                mBitmap = null
                mBitmapCanvas = null
                mBitmapPaint.setShader(null)
            } else {
                initializeBitmap()
            }

            invalidate()
        }

    override fun setImageBitmap(bm: Bitmap) {
        super.setImageBitmap(bm)
        initializeBitmap()
        invalidate()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        initializeBitmap()
        invalidate()
    }

    override fun setImageResource(@DrawableRes resId: Int) {
        super.setImageResource(resId)
        initializeBitmap()
        invalidate()
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        initializeBitmap()
        invalidate()
    }

    override fun setImageAlpha(alpha: Int) {
        val newAlpha = alpha and 0xFF

        if (newAlpha == mImageAlpha) {
            return
        }

        mImageAlpha = newAlpha

        if (mInitialized) {
            mBitmapPaint.alpha = newAlpha
            invalidate()
        }
    }

    override fun getImageAlpha(): Int {
        return mImageAlpha
    }

    override fun setColorFilter(cf: ColorFilter) {
        if (cf === mColorFilter) {
            return
        }

        mColorFilter = cf

        // This might be called during ImageView construction before
        // member initialization has finished on API level <= 19.
        if (mInitialized) {
            mBitmapPaint.setColorFilter(cf)
            invalidate()
        }
    }

    override fun getColorFilter(): ColorFilter {
        return mColorFilter!!
    }

    private fun getBitmapFromDrawable(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }

        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        try {
            val bitmap = if (drawable is ColorDrawable) {
                Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG)
            } else {
                Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    BITMAP_CONFIG
                )
            }

            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun initializeBitmap() {
        mBitmap = getBitmapFromDrawable(drawable)

        mBitmapCanvas = if (mBitmap != null && mBitmap!!.isMutable) {
            Canvas(mBitmap!!)
        } else {
            null
        }

        if (!mInitialized) {
            return
        }

        if (mBitmap != null) {
            updateShaderMatrix()
        } else {
            mBitmapPaint.setShader(null)
        }
    }

    private fun updateDimensions() {
        mBorderRect.set(calculateBounds())
        mBorderRadius = min(
            ((mBorderRect.height() - mBorderWidth) / 2.0f).toDouble(),
            ((mBorderRect.width() - mBorderWidth) / 2.0f).toDouble()
        ).toFloat()

        mDrawableRect.set(mBorderRect)
        if (!mBorderOverlay && mBorderWidth > 0) {
            mDrawableRect.inset(mBorderWidth - 1.0f, mBorderWidth - 1.0f)
        }
        mDrawableRadius = min(
            (mDrawableRect.height() / 2.0f).toDouble(),
            (mDrawableRect.width() / 2.0f).toDouble()
        ).toFloat()

        updateShaderMatrix()
    }

    private fun calculateBounds(): RectF {
        val availableWidth = width - paddingLeft - paddingRight
        val availableHeight = height - paddingTop - paddingBottom

        val sideLength = min(availableWidth.toDouble(), availableHeight.toDouble())
            .toInt()

        val left = paddingLeft + (availableWidth - sideLength) / 2f
        val top = paddingTop + (availableHeight - sideLength) / 2f

        return RectF(left, top, left + sideLength, top + sideLength)
    }

    private fun updateShaderMatrix() {
        if (mBitmap == null) {
            return
        }

        val scale: Float
        var dx = 0f
        var dy = 0f

        mShaderMatrix.set(null)

        val bitmapHeight = mBitmap!!.height
        val bitmapWidth = mBitmap!!.width

        if (bitmapWidth * mDrawableRect.height() > mDrawableRect.width() * bitmapHeight) {
            scale = mDrawableRect.height() / bitmapHeight.toFloat()
            dx = (mDrawableRect.width() - bitmapWidth * scale) * 0.5f
        } else {
            scale = mDrawableRect.width() / bitmapWidth.toFloat()
            dy = (mDrawableRect.height() - bitmapHeight * scale) * 0.5f
        }

        mShaderMatrix.setScale(scale, scale)
        mShaderMatrix.postTranslate(
            (dx + 0.5f).toInt() + mDrawableRect.left,
            (dy + 0.5f).toInt() + mDrawableRect.top
        )

        mRebuildShader = true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mDisableCircularTransformation) {
            return super.onTouchEvent(event)
        }

        return inTouchableArea(event.x, event.y) && super.onTouchEvent(event)
    }

    private fun inTouchableArea(x: Float, y: Float): Boolean {
        if (mBorderRect.isEmpty) {
            return true
        }

        return ((x - mBorderRect.centerX()).toDouble().pow(2.0) +
                (y - mBorderRect.centerY()).toDouble().pow(2.0) <=
                mBorderRadius.toDouble().pow(2.0))
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private inner class OutlineProvider : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            if (mDisableCircularTransformation) {
                BACKGROUND.getOutline(view, outline)
            } else {
                val bounds = Rect()
                mBorderRect.roundOut(bounds)
                outline.setRoundRect(bounds, bounds.width() / 2.0f)
            }
        }
    }


}
