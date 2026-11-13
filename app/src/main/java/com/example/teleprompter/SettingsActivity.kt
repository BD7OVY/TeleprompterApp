package com.example.teleprompter

import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/** 设置页：调整默认速度 / 字号 / 护眼 / 镜像，立即生效并持久化 */
class SettingsActivity : AppCompatActivity() {
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        prefs = Prefs(this)

        val speedSeek = findViewById<SeekBar>(R.id.seekSpeed)
        val fontSeek = findViewById<SeekBar>(R.id.seekFont)
        val tvSpeedVal = findViewById<TextView>(R.id.tvSpeedVal)
        val tvFontVal = findViewById<TextView>(R.id.tvFontVal)

        speedSeek.max = 390
        fontSeek.max = 106
        speedSeek.progress = (prefs.defaultSpeed - 10f).toInt().coerceAtLeast(0)
        fontSeek.progress = (prefs.defaultFontSize - 14f).toInt().coerceAtLeast(0)
        tvSpeedVal.text = "默认速度：${prefs.defaultSpeed.toInt()}"
        tvFontVal.text = "默认字号：${prefs.defaultFontSize.toInt()}"

        speedSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                val v = (p + 10f)
                prefs.defaultSpeed = v
                tvSpeedVal.text = "默认速度：${v.toInt()}"
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        fontSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                val v = (p + 14f)
                prefs.defaultFontSize = v
                tvFontVal.text = "默认字号：${v.toInt()}"
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })

        val swEye = findViewById<Switch>(R.id.swEye)
        val swMirror = findViewById<Switch>(R.id.swMirror)
        swEye.isChecked = prefs.eyeCareDefault
        swMirror.isChecked = prefs.mirrorDefault
        swEye.setOnCheckedChangeListener { _, c -> prefs.eyeCareDefault = c }
        swMirror.setOnCheckedChangeListener { _, c -> prefs.mirrorDefault = c }

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }
}
