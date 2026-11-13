package com.example.teleprompter

import android.app.Application
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 全局异常捕获：把未捕获崩溃的堆栈写入 filesDir/crash.txt，
 * 下次打开 App 时在首页弹窗展示，方便无数据线也能把错误发给我排查。
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val sb = StringBuilder()
                sb.append("时间：$time\n")
                sb.append("异常：${throwable.javaClass.name}：${throwable.message}\n\n")
                sb.append(sw.toString())
                File(filesDir, "crash.txt").writeText(sb.toString())
            } catch (_: Exception) {
                // 忽略，避免崩溃处理器自身再崩
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
