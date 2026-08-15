package com.example.floatingcounter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var etTarget: EditText
    private val prefs by lazy { getSharedPreferences("counter_prefs", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etTarget = findViewById(R.id.etTarget)
        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnStop = findViewById<Button>(R.id.btnStop)

        // 저장된 목표값 불러오기 (기본값 50)
        val savedTarget = prefs.getInt("saved_target", 50)
        etTarget.setText(savedTarget.toString())

        btnStart.setOnClickListener {
            saveTargetValue()
            checkOverlayPermissionAndStart()
        }

        btnStop.setOnClickListener {
            val intent = Intent(this, FloatingService::class.java)
            stopService(intent)
            Toast.makeText(this, "플로팅 카운터를 종료했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveTargetValue() {
        val targetStr = etTarget.text.toString().trim()
        val targetVal = if (targetStr.isNotEmpty()) targetStr.toInt() else 50
        prefs.edit().putInt("saved_target", targetVal).apply()
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