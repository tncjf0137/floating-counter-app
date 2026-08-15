package com.example.floatingcounter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var etTarget: EditText
    private lateinit var etExamDate: EditText
    private lateinit var tvDDay: TextView
    private lateinit var tvExamDate: TextView
    private lateinit var tvTodayCount: TextView
    private lateinit var tvTotalCount: TextView
    private lateinit var heatmapView: HeatmapView

    private val prefs by lazy { getSharedPreferences("counter_prefs", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etTarget = findViewById(R.id.etTarget)
        etExamDate = findViewById(R.id.etExamDate)
        tvDDay = findViewById(R.id.tvDDay)
        tvExamDate = findViewById(R.id.tvExamDate)
        tvTodayCount = findViewById(R.id.tvTodayCount)
        tvTotalCount = findViewById(R.id.tvTotalCount)
        heatmapView = findViewById(R.id.heatmapView)

        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnStop = findViewById<Button>(R.id.btnStop)

        val savedTarget = prefs.getInt("saved_target", 50)
        val savedExamDate = prefs.getString("exam_date", "2026-12-06") ?: "2026-12-06"

        etTarget.setText(savedTarget.toString())
        etExamDate.setText(savedExamDate)

        btnStart.setOnClickListener {
            saveSettings()
            checkOverlayPermissionAndStart()
        }

        btnStop.setOnClickListener {
            val intent = Intent(this, FloatingService::class.java)
            stopService(intent)
            Toast.makeText(this, "플로팅 카운터를 종료했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        updateDDayUI()
        loadRecordData()
    }

    private fun updateDDayUI() {
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

                tvDDay.text = when {
                    diffDays > 0 -> "시험 D-$diffDays"
                    diffDays == 0 -> "시험 D-Day (오늘!)"
                    else -> "시험 D+${-diffDays}"
                }
                tvExamDate.text = "목표 시험일: $examDateStr"
            }
        } catch (e: Exception) {
            tvDDay.text = "D-Day 계산 불가"
        }
    }

    private fun loadRecordData() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayCount = prefs.getInt("daily_$todayStr", 0)
        val totalCount = prefs.getInt("total_count", 0)

        tvTodayCount.text = "오늘 학습: ${todayCount}개"
        tvTotalCount.text = "누적 학습: ${totalCount}개"

        val allPrefs = prefs.all
        val records = mutableMapOf<String, Int>()
        for ((key, value) in allPrefs) {
            if (key.startsWith("daily_") && value is Int) {
                val date = key.removePrefix("daily_")
                records[date] = value
            }
        }
        heatmapView.setRecords(records)
    }

    private fun saveSettings() {
        val targetStr = etTarget.text.toString().trim()
        val targetVal = if (targetStr.isNotEmpty()) targetStr.toInt() else 50
        val examDateStr = etExamDate.text.toString().trim().ifEmpty { "2026-12-06" }

        prefs.edit()
            .putInt("saved_target", targetVal)
            .putString("exam_date", examDateStr)
            .apply()

        updateDDayUI()
    }

    private fun checkOverlayPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
                Toast.makeText(this, "다른 앱 위에 그리기 권한을 허용해 주세요.", Toast.LENGTH_LONG).show()
            } else {
                startFloatingService()
            }
        } else {
            startFloatingService()
        }
    }

    private fun startFloatingService() {
        val intent = Intent(this, FloatingService::class.java)
        startService(intent)
    }
}