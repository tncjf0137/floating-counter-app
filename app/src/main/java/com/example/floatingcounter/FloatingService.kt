package com.example.floatingcounter

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class FloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var tvCount: TextView
    private lateinit var tvProgress: TextView
    private lateinit var tvWidgetDDay: TextView
    private lateinit var btnClose: TextView

    private var count = 0
    private var target = 50
    private var dDayStr = "D-Day"
    private val prefs by lazy { getSharedPreferences("counter_prefs", Context.MODE_PRIVATE) }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        loadTodayData()

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_widget, null)

        val layoutParamsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val sizeInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            160f,
            resources.displayMetrics
        ).toInt()

        val params = WindowManager.LayoutParams(
            sizeInPx,
            sizeInPx,
            layoutParamsType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(floatingView, params)

        tvCount = floatingView.findViewById(R.id.tvCount)
        tvProgress = floatingView.findViewById(R.id.tvProgress)
        tvWidgetDDay = floatingView.findViewById(R.id.tvWidgetDDay)
        btnClose = floatingView.findViewById(R.id.btnClose)

        updateUI()

        btnClose.setOnClickListener {
            stopSelf()
        }

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var touchStartTime = 0L

        floatingView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    touchStartTime = System.currentTimeMillis()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(floatingView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val diffX = abs(event.rawX - initialTouchX)
                    val diffY = abs(event.rawY - initialTouchY)
                    val touchDuration = System.currentTimeMillis() - touchStartTime

                    if (diffX < 25 && diffY < 25) {
                        loadTodayData()

                        if (touchDuration >= 500) {
                            if (count > 0) {
                                count--
                                Toast.makeText(this, "-1 차감되었습니다.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            count++
                        }
                        saveCount()
                        updateUI()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun loadTodayData() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        count = prefs.getInt("daily_$todayStr", 0)
        target = prefs.getInt("saved_target", 50)

        val examDateStr = prefs.getString("exam_date", "2026-12-06") ?: "2026-12-06"
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val examDate = sdf.parse(examDateStr)
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            if (examDate != null) {
                val diffMillis = examDate.time - today.time
                val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()
                dDayStr = when {
                    diffDays > 0 -> "D-$diffDays"
                    diffDays == 0 -> "D-Day"
                    else -> "D+${-diffDays}"
                }
            }
        } catch (e: Exception) {
            dDayStr = "D-Day"
        }
    }

    private fun updateUI() {
        tvCount.text = count.toString()
        tvWidgetDDay.text = dDayStr
        val percent = if (target > 0) (count * 100) / target else 0
        tvProgress.text = "$count / $target ($percent%)"
    }

    private fun saveCount() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val oldCount = prefs.getInt("daily_$todayStr", 0)
        val diff = count - oldCount
        val totalCount = prefs.getInt("total_count", 0) + diff

        prefs.edit()
            .putInt("daily_$todayStr", count)
            .putInt("total_count", if (totalCount < 0) 0 else totalCount)
            .apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}