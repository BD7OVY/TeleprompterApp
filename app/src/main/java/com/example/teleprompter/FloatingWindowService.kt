package com.example.teleprompter

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.app.NotificationCompat

/**
 * 悬浮窗提词服务：在其他 App 上层显示一个可拖动的小窗，里面滚动播放当前稿本。
 * 需 SYSTEM_ALERT_WINDOW 权限（在播放页引导授予），并以前台服务形式运行。
 */
class FloatingWindowService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private lateinit var scrollView: ScrollView
    private lateinit var textView: TextView
    private lateinit var btnPlay: Button
    private lateinit var scroller: TextScroller
    private var speed = 60f
    private var fontSize = 20f
    private var playing = true

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(1, buildNotification())
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val id = intent?.getStringExtra("id")
        val content = if (id != null) (ScriptStore(this).get(id)?.content ?: "") else ""
        if (floatingView == null) showWindow(content)
        return START_NOT_STICKY
    }

    private fun showWindow(content: String) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.floating_window, null)
        scrollView = floatingView!!.findViewById(R.id.floatingScroll)
        textView = floatingView!!.findViewById(R.id.floatingText)
        btnPlay = floatingView!!.findViewById(R.id.btnPlay)
        textView.text = content
        textView.textSize = fontSize
        scroller = TextScroller(scrollView) { speed }

        val dm = resources.displayMetrics
        val w = (320 * dm.density).toInt()
        val h = (220 * dm.density).toInt()
        val params = WindowManager.LayoutParams(
            w, h,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = (80 * dm.density).toInt()

        floatingView!!.findViewById<View>(R.id.floatingHandle).setOnTouchListener(DragListener(params))
        btnPlay.setOnClickListener {
            scroller.toggle()
            playing = !playing
            btnPlay.text = if (playing) "暂停" else "播放"
        }
        floatingView!!.findViewById<View>(R.id.btnClose).setOnClickListener { stopSelf() }
        floatingView!!.findViewById<View>(R.id.btnSpeedDown).setOnClickListener {
            speed = (speed - 10).coerceAtLeast(10f)
            if (playing) { scroller.pause(); scroller.play() }
        }
        floatingView!!.findViewById<View>(R.id.btnSpeedUp).setOnClickListener {
            speed = (speed + 10).coerceAtMost(400f)
            if (playing) { scroller.pause(); scroller.play() }
        }
        windowManager.addView(floatingView, params)
        scroller.play()
    }

    /** 拖动顶部把手移动悬浮窗位置 */
    private inner class DragListener(private val params: WindowManager.LayoutParams) : View.OnTouchListener {
        private var initX = 0
        private var initY = 0
        private var touchX = 0f
        private var touchY = 0f
        override fun onTouch(v: View, e: MotionEvent): Boolean {
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    initX = params.x
                    initY = params.y
                    touchX = e.rawX
                    touchY = e.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initX + (e.rawX - touchX).toInt()
                    params.y = initY + (e.rawY - touchY).toInt()
                    windowManager.updateViewLayout(floatingView, params)
                }
            }
            return true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingView?.let { windowManager.removeView(it) }
        floatingView = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                "teleprompter_floating",
                "提词器悬浮窗",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, "teleprompter_floating")
            .setContentTitle("提词器悬浮窗运行中")
            .setContentText("返回应用关闭即可停止")
            .setSmallIcon(R.drawable.ic_launcher)
            .setOngoing(true)
            .build()
    }
}
