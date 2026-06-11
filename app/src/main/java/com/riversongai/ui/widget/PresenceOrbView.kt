package com.riversongai.ui.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.ColorUtils
import com.riversongai.R
import com.riversongai.utils.UIStyleManager
import kotlin.math.abs
import kotlin.math.min

/**
 * Android port of riversongai.com's "PresenceOrb" — a small glowing dot that
 * sits in the toolbar and reflects River's current conversational state via
 * glow/scale animations (idle breathe, listening ripple, thinking pulse,
 * speaking pulse, attention burst).
 */
class PresenceOrbView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class OrbState { IDLE, LISTENING, THINKING, SPEAKING, ATTENTION }

    var orbColor: Int = Color.parseColor("#0B6CF5")
        set(value) {
            field = value
            invalidate()
        }

    var state: OrbState = OrbState.IDLE
        private set

    private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ripplePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    // Animated parameters driven by the ValueAnimators below.
    private var coreAlpha = 1f
    private var coreScale = 1f
    private var glowFraction = 0.6f
    private var brightness = 0f // -1f..1f, used by THINKING
    private val ripples = mutableListOf<Float>() // each entry is a ripple's progress, 0f..1f

    private val activeAnimators = mutableListOf<Animator>()

    init {
        context.withStyledAttributes(attrs, R.styleable.PresenceOrbView) {
            orbColor = if (hasValue(R.styleable.PresenceOrbView_orbColor)) {
                getColor(R.styleable.PresenceOrbView_orbColor, orbColor)
            } else {
                UIStyleManager.resolveAttrColor(context, com.google.android.material.R.attr.colorPrimary)
            }
        }
        applyState(OrbState.IDLE)
    }

    fun setState(newState: OrbState) {
        if (state == newState && newState != OrbState.ATTENTION) return
        state = newState
        applyState(newState)
    }

    private fun applyState(newState: OrbState) {
        cancelAnimators()
        ripples.clear()
        coreScale = 1f
        brightness = 0f
        when (newState) {
            OrbState.IDLE -> startBreathe()
            OrbState.LISTENING -> startListening()
            OrbState.THINKING -> startThinking()
            OrbState.SPEAKING -> startSpeaking()
            OrbState.ATTENTION -> startAttention()
        }
    }

    private fun cancelAnimators() {
        activeAnimators.forEach { it.cancel() }
        activeAnimators.clear()
    }

    private fun track(animator: Animator) {
        activeAnimators.add(animator)
        animator.start()
    }

    /** Default idle "breathe": opacity 0.6<->1.0, glow 8dp<->20dp, ~3.5s cycle. */
    private fun startBreathe() {
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 3500
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                val t = it.animatedValue as Float
                coreAlpha = 0.6f + 0.4f * t
                glowFraction = 0.4f + 0.6f * t
                invalidate()
            }
        }
        track(animator)
    }

    /** Listening: a ripple expands and fades outward on a 1.6s loop. */
    private fun startListening() {
        coreAlpha = 1f
        glowFraction = 1f
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1600
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                ripples.clear()
                ripples.add(it.animatedValue as Float)
                invalidate()
            }
        }
        track(animator)
    }

    /** Thinking: brightness/glow pulse, ~2.4s cycle. */
    private fun startThinking() {
        val animator = ValueAnimator.ofFloat(-1f, 1f).apply {
            duration = 2400
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                val t = it.animatedValue as Float
                brightness = t
                coreAlpha = 0.85f + 0.15f * ((t + 1f) / 2f)
                glowFraction = 0.7f + 0.3f * ((t + 1f) / 2f)
                invalidate()
            }
        }
        track(animator)
    }

    /** Speaking: quick scale pulse 1.0<->1.22, ~0.55s cycle. */
    private fun startSpeaking() {
        coreAlpha = 1f
        glowFraction = 1f
        val animator = ValueAnimator.ofFloat(1f, 1.22f).apply {
            duration = 550
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                coreScale = it.animatedValue as Float
                invalidate()
            }
        }
        track(animator)
    }

    /** Attention: three quick ripple bursts, then settles back to idle. */
    private fun startAttention() {
        coreAlpha = 1f
        glowFraction = 1f
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 800
            repeatCount = 2 // plays a total of 3 bursts
            interpolator = LinearInterpolator()
            addUpdateListener {
                ripples.clear()
                ripples.add(it.animatedValue as Float)
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    ripples.clear()
                    if (state == OrbState.ATTENTION) {
                        state = OrbState.IDLE
                        applyState(OrbState.IDLE)
                    }
                }
            })
        }
        track(animator)
    }

    private fun currentColor(): Int {
        if (brightness == 0f) return orbColor
        val target = if (brightness > 0) Color.WHITE else Color.BLACK
        return ColorUtils.blendARGB(orbColor, target, abs(brightness) * 0.35f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return

        val cx = width / 2f
        val cy = height / 2f
        val maxRadius = min(width, height) / 2f
        val color = currentColor()

        // Glow halo
        val glowRadius = maxRadius * (0.55f + 0.45f * glowFraction)
        if (glowRadius > 0f) {
            glowPaint.shader = RadialGradient(
                cx, cy, glowRadius,
                ColorUtils.setAlphaComponent(color, (160 * coreAlpha).toInt()),
                ColorUtils.setAlphaComponent(color, 0),
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(cx, cy, glowRadius, glowPaint)
        }

        // Ripple ring(s) — listening / attention
        if (ripples.isNotEmpty()) {
            ripplePaint.color = color
            ripplePaint.strokeWidth = maxRadius * 0.08f
            for (progress in ripples) {
                val radius = maxRadius * 0.4f + (maxRadius * 0.6f) * progress
                ripplePaint.alpha = (180 * (1f - progress)).toInt().coerceIn(0, 255)
                canvas.drawCircle(cx, cy, radius, ripplePaint)
            }
        }

        // Core dot
        val coreRadius = maxRadius * 0.4f * coreScale
        corePaint.color = ColorUtils.setAlphaComponent(color, (255 * coreAlpha).toInt().coerceIn(0, 255))
        canvas.drawCircle(cx, cy, coreRadius, corePaint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancelAnimators()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (activeAnimators.isEmpty()) {
            applyState(state)
        }
    }
}
