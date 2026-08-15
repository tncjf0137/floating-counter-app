package com.example.floatingcounter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.*

class HeatmapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var recordData: Map<String, Int> = emptyMap()

    fun setRecords(records: Map<String, Int>) {
        this.recordData = records
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val columns = 14
        val cellSize = if (width > 0) (width / columns.toFloat()) - 8f else 30f
        val padding = 4f

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -(columns - 1))
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (i in 0 until columns) {
            val dateStr = sdf.format(calendar.time)
            val count = recordData[dateStr] ?: 0

            paint.color = when {
                count == 0 -> Color.parseColor("#E0E0E0")
                count in 1..20 -> Color.parseColor("#C6E48B")
                count in 21..50 -> Color.parseColor("#7BC96F")
                else -> Color.parseColor("#239A3B")
            }

            val left = i * (cellSize + 8f) + padding
            val top = padding
            val right = left + cellSize
            val bottom = top + cellSize

            canvas.drawRoundRect(left, top, right, bottom, 8f, 8f, paint)

            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
    }
}