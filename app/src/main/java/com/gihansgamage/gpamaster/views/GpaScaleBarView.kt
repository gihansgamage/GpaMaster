package com.gihansgamage.gpamaster.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * A custom view that draws a colour-banded GPA scale bar.
 *
 * Layout (top → bottom):
 *   [class label above pointer]
 *   [▼ pointer triangle]
 *   [━━━━ segmented colour bar ━━━━]
 *   [tick labels: 0 ... thresholds ... maxScale]
 *
 * Each colour band maps to a degree classification zone.
 * The pointer sits at the user's current weighted GPA position.
 */
class GpaScaleBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ── Data ──────────────────────────────────────────────────────────────────

    data class Band(
        val fromGpa: Double,
        val toGpa: Double,
        val color: Int,
        val label: String,
        val shortLabel: String
    )

    private var maxScale: Double = 4.0
    private var currentGpa: Double = 0.0
    private var bands: List<Band> = emptyList()
    private var currentBandLabel: String = ""

    // ── Paints ────────────────────────────────────────────────────────────────

    private val bandPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val pointerStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.WHITE
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.parseColor("#BDBDBD")
    }

    private val tickLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 28f
        color = Color.parseColor("#757575")
        textAlign = Paint.Align.CENTER
    }

    private val classLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 34f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val gpaValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 30f
        textAlign = Paint.Align.CENTER
        color = Color.parseColor("#37474F")
    }

    // ── Sizing constants (dp → px conversion happens in onSizeChanged) ────────

    private val dpScale = context.resources.displayMetrics.density

    private val barHeightPx get() = (18 * dpScale).toInt()
    private val cornerRadiusPx get() = (9 * dpScale)
    private val pointerSizePx get() = (10 * dpScale)
    private val tickHeightPx get() = (6 * dpScale)
    private val paddingHPx get() = (20 * dpScale)

    // Vertical layout constants
    private val classLabelTopMarginPx get() = (6 * dpScale)
    private val pointerGapPx get() = (4 * dpScale)   // gap between label and pointer tip
    private val barTopMarginPx get() = (4 * dpScale)  // gap between pointer base and bar top
    private val tickGapPx get() = (4 * dpScale)
    private val tickLabelGapPx get() = (2 * dpScale)

    // ── Measured heights ──────────────────────────────────────────────────────

    private var classLabelHeight = 0f
    private var gpaValueHeight = 0f

    // ── Public API ────────────────────────────────────────────────────────────

    fun setData(gpa: Double, scale: Double, bandList: List<Band>) {
        currentGpa = gpa
        maxScale = scale
        bands = bandList
        val matchingBand = bands.lastOrNull { gpa >= it.fromGpa }
        currentBandLabel = matchingBand?.shortLabel ?: bands.firstOrNull()?.shortLabel ?: ""

        // Tint pointer and label to the band colour
        val bandColor = matchingBand?.color ?: Color.GRAY
        pointerPaint.color = bandColor
        classLabelPaint.color = bandColor

        requestLayout()
        invalidate()
    }

    // ── Measurement ───────────────────────────────────────────────────────────

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val fm = classLabelPaint.fontMetrics
        classLabelHeight = fm.descent - fm.ascent

        val fm2 = gpaValuePaint.fontMetrics
        gpaValueHeight = fm2.descent - fm2.ascent

        val fm3 = tickLabelPaint.fontMetrics
        val tickLabelHeight = fm3.descent - fm3.ascent

        val totalHeight = (classLabelTopMarginPx
                + classLabelHeight
                + gpaValueHeight
                + pointerGapPx
                + pointerSizePx          // pointer triangle height
                + barTopMarginPx
                + barHeightPx
                + tickGapPx
                + tickHeightPx
                + tickLabelGapPx
                + tickLabelHeight
                + classLabelTopMarginPx).toInt()

        val resolvedWidth = resolveSize(600, widthMeasureSpec)
        val resolvedHeight = resolveSize(totalHeight, heightMeasureSpec)
        setMeasuredDimension(resolvedWidth, resolvedHeight)
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bands.isEmpty() || currentGpa <= 0.0) return

        val w = width.toFloat()
        val barLeft = paddingHPx
        val barRight = w - paddingHPx
        val barWidth = barRight - barLeft

        // Compute Y positions top → bottom
        val fm = classLabelPaint.fontMetrics
        classLabelHeight = fm.descent - fm.ascent
        val fm2 = gpaValuePaint.fontMetrics
        gpaValueHeight = fm2.descent - fm2.ascent
        val fm3 = tickLabelPaint.fontMetrics
        val tickLabelHeight = fm3.descent - fm3.ascent

        var curY = classLabelTopMarginPx

        // 1. Class label
        val classLabelY = curY - fm.ascent      // baseline
        curY += classLabelHeight

        // 2. GPA value sub-label (e.g. "3.45 / 4.0")
        val gpaValueY = curY - fm2.ascent
        curY += gpaValueHeight

        // 3. Pointer tip
        curY += pointerGapPx
        val pointerTipY = curY
        curY += pointerSizePx

        // 4. Bar
        curY += barTopMarginPx
        val barTop = curY
        val barBottom = barTop + barHeightPx
        curY = barBottom

        // 5. Ticks
        curY += tickGapPx
        val tickTop = curY
        val tickBottom = tickTop + tickHeightPx
        curY = tickBottom

        // 6. Tick labels
        curY += tickLabelGapPx
        val tickLabelY = curY - fm3.ascent

        // ── GPA → X mapper ────────────────────────────────────────────────────
        fun gpaToX(gpa: Double): Float {
            val clamped = gpa.coerceIn(0.0, maxScale)
            return barLeft + ((clamped / maxScale) * barWidth).toFloat()
        }

        // ── Draw colour bands ─────────────────────────────────────────────────
        val barRect = RectF(barLeft, barTop, barRight, barBottom)
        val clipPath = Path().apply {
            addRoundRect(barRect, cornerRadiusPx, cornerRadiusPx, Path.Direction.CW)
        }
        canvas.save()
        canvas.clipPath(clipPath)

        for (band in bands) {
            val left = gpaToX(band.fromGpa)
            val right = gpaToX(band.toGpa)
            bandPaint.color = band.color
            canvas.drawRect(left, barTop, right, barBottom, bandPaint)
        }
        canvas.restore()

        // ── Draw pointer ──────────────────────────────────────────────────────
        val clampedGpa = currentGpa.coerceIn(0.0, maxScale)
        val pointerX = gpaToX(clampedGpa)
        val halfBase = pointerSizePx * 0.8f

        val pointerPath = Path().apply {
            moveTo(pointerX, pointerTipY)
            lineTo(pointerX - halfBase, pointerTipY + pointerSizePx)
            lineTo(pointerX + halfBase, pointerTipY + pointerSizePx)
            close()
        }
        canvas.drawPath(pointerPath, pointerPaint)
        canvas.drawPath(pointerPath, pointerStrokePaint)

        // ── Draw class label (above pointer) ──────────────────────────────────
        canvas.drawText(currentBandLabel, pointerX, classLabelY, classLabelPaint)

        // ── Draw GPA value sub-label ──────────────────────────────────────────
        val gpaText = "${String.format("%.2f", currentGpa)} / ${String.format("%.1f", maxScale)}"
        canvas.drawText(gpaText, pointerX, gpaValueY, gpaValuePaint)

        // ── Draw threshold ticks + labels ─────────────────────────────────────
        // Collect unique thresholds (band boundaries + 0 + maxScale)
        val thresholds = mutableSetOf(0.0, maxScale)
        for (band in bands) {
            thresholds.add(band.fromGpa)
            thresholds.add(band.toGpa)
        }

        for (t in thresholds.sorted()) {
            val tx = gpaToX(t)
            canvas.drawLine(tx, tickTop, tx, tickBottom, tickPaint)
            val label = if (t == maxScale) {
                String.format("%.0f", t)
            } else {
                String.format("%.1f", t)
            }
            canvas.drawText(label, tx, tickLabelY, tickLabelPaint)
        }
    }
}
