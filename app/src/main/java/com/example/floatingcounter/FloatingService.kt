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
import kotlin.math.abs

class FloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var tvCount: TextView
    private lateinit var tvProgress: TextView
    private lateinit var btnClose: TextView

    private var count = 0
    private var target = 50
    private val prefs by lazy { getSharedPreferences("counter_prefs", Context.MODE_PRIVATE) }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        count = prefs.getInt("saved_count", 0)
        target = prefs.getInt("saved_target", 50)

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
        btnClose = floatingView.findViewById(R.id.btnClose)

        updateUI()

        // 닫기(X) 버튼
        btnClose.setOnClickListener {
            stopSelf()
        }

        // 터치 (드래그, 클릭, 길게 누르기) 처리
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
                        // 500ms 이상 길게 누르면 리셋
                        if (touchDuration >= 500) {
                            count = 0
                            Toast.makeText(this, "카운터가 리셋되었습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            // 짧게 누르면 +1
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

    private fun updateUI() {
        tvCount.text = count.toString()
        val percent = if (target > 0) (count * 100) / target else 0
        tvProgress.text = "$count / $target ($percent%)"
    }

    private fun saveCount() {
        prefs.edit().putInt("saved_count", count).apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}