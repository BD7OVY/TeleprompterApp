package com.example.teleprompter

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import android.widget.ScrollView

/**
 * 文本滚动控制器
 * 通过 ValueAnimator 让 ScrollView 匀速向上滚动，速度由外部 lambda 实时提供，
 * 这样在播放中调节速度/字体也能立刻生效。播放页和悬浮窗共用本类。
 */
class TextScroller(
    private val scrollView: ScrollView,
    private val getSpeedPxPerSec: () -> Float
) {
    private var animator: ValueAnimator? = null
    private var playing = false

    val isPlaying: Boolean
        get() = playing

    fun play() {
        if (playing) return
        val child = scrollView.getChildAt(0) ?: return
        val maxY = (child.height - scrollView.height).coerceAtLeast(0)
        var from = scrollView.scrollY
        if (from >= maxY) {
            scrollView.scrollTo(0, 0)
            from = 0
        }
        val dist = (maxY - from).toFloat()
        if (dist <= 0) {
            playing = true
            return
        }
        val speed = getSpeedPxPerSec().coerceAtLeast(1f)
        val duration = (dist / speed * 1000).toLong().coerceAtLeast(1)
        animator = ValueAnimator.ofInt(from, maxY).apply {
            this.duration = duration
            interpolator = LinearInterpolator()
            addUpdateListener { scrollView.scrollTo(0, it.animatedValue as Int) }
            start()
        }
        playing = true
    }

    fun pause() {
        animator?.cancel()
        animator = null
        playing = false
    }

    fun toggle() {
        if (playing) pause() else play()
    }

    fun restart() {
        pause()
        scrollView.scrollTo(0, 0)
        play()
    }

    /** 当前播放进度 0.0 ~ 1.0 */
    fun progress(): Float {
        val child = scrollView.getChildAt(0) ?: return 0f
        val maxY = (child.height - scrollView.height).coerceAtLeast(1)
        return (scrollView.scrollY.toFloat() / maxY).coerceIn(0f, 1f)
    }
}
