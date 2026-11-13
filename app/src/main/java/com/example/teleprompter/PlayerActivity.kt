package com.example.teleprompter

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/** 播放页（核心）：全屏滚动字幕 + 速度/字号/颜色/护眼/镜像/手势/进度/悬浮窗 */
class PlayerActivity : AppCompatActivity() {
    private lateinit var store: ScriptStore
    private lateinit var prefs: Prefs

    private lateinit var svContent: ScrollView
    private lateinit var tvContent: TextView
    private lateinit var flEyeCare: View
    private lateinit var tvProgress: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnPlay: Button
    private lateinit var tvSpeed: TextView

    private lateinit var scroller: TextScroller
    private var speed = 60f
    private var fontSize = 32f
    private var textColor = Color.WHITE
    private var mirror = false
    private var eyeCare = false
    private var scriptId = ""

    private val handler = Handler(Looper.getMainLooper())
    private val progressRunnable = object : Runnable {
        override fun run() {
            val p = (scroller.progress() * 100).toInt()
            progressBar.progress = p
            tvProgress.text = "进度 $p%"
            handler.postDelayed(this, 200)
        }
    }

    private lateinit var gestureDetector: GestureDetector
    private lateinit var scaleDetector: ScaleGestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        store = ScriptStore(this)
        prefs = Prefs(this)
        scriptId = intent.getStringExtra("id") ?: ""

        svContent = findViewById(R.id.svContent)
        tvContent = findViewById(R.id.tvContent)
        flEyeCare = findViewById(R.id.flEyeCare)
        tvProgress = findViewById(R.id.tvProgress)
        progressBar = findViewById(R.id.progressBar)
        btnPlay = findViewById(R.id.btnPlay)
        tvSpeed = findViewById(R.id.tvSpeed)

        speed = prefs.defaultSpeed
        fontSize = prefs.defaultFontSize
        textColor = prefs.defaultTextColor
        mirror = prefs.mirrorDefault
        eyeCare = prefs.eyeCareDefault

        val script = store.get(scriptId)
        if (script == null) {
            Toast.makeText(this, "稿本不存在", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        tvContent.text = script.content
        applyStyle()

        scroller = TextScroller(svContent) { speed }

        setupGestures()
        bindControls()
        updateSpeedUi()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        keepScreenOn()
        handler.post(progressRunnable)
    }

    override fun onPause() {
        super.onPause()
        scroller.pause()
        handler.removeCallbacks(progressRunnable)
    }

    private fun applyStyle() {
        tvContent.textSize = fontSize
        tvContent.setTextColor(textColor)
        tvContent.scaleX = if (mirror) -1f else 1f
        flEyeCare.visibility = if (eyeCare) View.VISIBLE else View.GONE
    }

    private fun bindControls() {
        btnPlay.setOnClickListener { togglePlay() }
        findViewById<View>(R.id.btnRestart).setOnClickListener { scroller.restart() }
        findViewById<View>(R.id.btnSpeedDown).setOnClickListener { changeSpeed(-10f) }
        findViewById<View>(R.id.btnSpeedUp).setOnClickListener { changeSpeed(10f) }
        findViewById<View>(R.id.btnFontDown).setOnClickListener { changeFont(-2f) }
        findViewById<View>(R.id.btnFontUp).setOnClickListener { changeFont(2f) }
        findViewById<View>(R.id.btnColor).setOnClickListener { cycleTextColor() }
        findViewById<View>(R.id.btnEyeCare).setOnClickListener { toggleEyeCare() }
        findViewById<View>(R.id.btnMirror).setOnClickListener { toggleMirror() }
        findViewById<View>(R.id.btnFloating).setOnClickListener { openFloating() }
    }

    private fun setupGestures() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                togglePlay()
                return true
            }
            // 上下拖动调速：向上拖加快，向下拖减慢
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dx: Float, dy: Float): Boolean {
                changeSpeed(-dy * 0.4f)
                return true
            }
        })
        scaleDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                changeFont(detector.scaleFactor - 1f)
                return true
            }
        })
        svContent.setOnTouchListener { _, event ->
            scaleDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun togglePlay() {
        scroller.toggle()
        btnPlay.text = if (scroller.isPlaying()) "暂停" else "播放"
    }

    private fun changeSpeed(delta: Float) {
        speed = (speed + delta).coerceIn(10f, 400f)
        prefs.defaultSpeed = speed
        updateSpeedUi()
        if (scroller.isPlaying()) { scroller.pause(); scroller.play() }
    }

    private fun updateSpeedUi() {
        tvSpeed.text = "速度 ${speed.toInt()}"
    }

    private fun changeFont(delta: Float) {
        fontSize = (fontSize + delta).coerceIn(14f, 120f)
        tvContent.textSize = fontSize
        prefs.defaultFontSize = fontSize
    }

    private fun cycleTextColor() {
        val colors = listOf(
            Color.WHITE, Color.YELLOW, Color.CYAN, Color.GREEN, Color.RED, Color.LTGRAY
        )
        val idx = colors.indexOf(textColor)
        textColor = colors[(idx + 1) % colors.size]
        tvContent.setTextColor(textColor)
        prefs.defaultTextColor = textColor
    }

    private fun toggleEyeCare() {
        eyeCare = !eyeCare
        flEyeCare.visibility = if (eyeCare) View.VISIBLE else View.GONE
        prefs.eyeCareDefault = eyeCare
    }

    private fun toggleMirror() {
        mirror = !mirror
        tvContent.scaleX = if (mirror) -1f else 1f
        prefs.mirrorDefault = mirror
    }

    private fun hideSystemUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            )
        }
    }

    /** 锁定屏幕常亮 + 拉高亮度，防止自动息屏或调暗 */
    private fun keepScreenOn() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val lp = window.attributes
        lp.screenBrightness = 1.0f
        window.attributes = lp
    }

    /** 开启悬浮窗提词（需“显示在其他应用上层”权限） */
    private fun openFloating() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "请先授予「显示在其他应用上层」权限", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            return
        }
        val intent = Intent(this, FloatingWindowService::class.java)
        intent.putExtra("id", scriptId)
        ContextCompat.startForegroundService(this, intent)
        Toast.makeText(this, "已开启悬浮窗，返回即可看到滚动字幕", Toast.LENGTH_SHORT).show()
    }
}
