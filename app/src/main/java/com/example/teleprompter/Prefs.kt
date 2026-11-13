package com.example.teleprompter

import android.content.Context
import android.content.SharedPreferences

/**
 * 全局偏好设置：默认速度 / 字号 / 颜色 / 护眼 / 镜像
 * 播放页会读取这里的默认值，设置页负责修改
 */
class Prefs(context: Context) {
    private val sp: SharedPreferences =
        context.getSharedPreferences("teleprompter_prefs", Context.MODE_PRIVATE)

    /** 滚动速度，单位：像素/秒 */
    var defaultSpeed: Float
        get() = sp.getFloat("defaultSpeed", 60f)
        set(v) = sp.edit().putFloat("defaultSpeed", v).apply()

    /** 正文字号，单位：sp */
    var defaultFontSize: Float
        get() = sp.getFloat("defaultFontSize", 32f)
        set(v) = sp.edit().putFloat("defaultFontSize", v).apply()

    /** 文字颜色，整数色值 */
    var defaultTextColor: Int
        get() = sp.getInt("defaultTextColor", 0xFFFFFFFF.toInt())
        set(v) = sp.edit().putInt("defaultTextColor", v).apply()

    /** 是否默认开启护眼模式 */
    var eyeCareDefault: Boolean
        get() = sp.getBoolean("eyeCareDefault", false)
        set(v) = sp.edit().putBoolean("eyeCareDefault", v).apply()

    /** 是否默认开启镜像翻转 */
    var mirrorDefault: Boolean
        get() = sp.getBoolean("mirrorDefault", false)
        set(v) = sp.edit().putBoolean("mirrorDefault", v).apply()
}
