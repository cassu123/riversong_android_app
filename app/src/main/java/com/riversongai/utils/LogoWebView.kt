package com.riversongai.utils

import android.content.Context
import android.graphics.Color
import android.webkit.WebView
import com.riversongai.utils.ThemeManager

object LogoWebView {

    fun buildLogoHtml(context: Context): String {
        val themeKey = ThemeManager.getSelectedTheme(context)
        
        val (primary, secondary, core, darkBg) = when (themeKey) {
            ThemeManager.THEME_HALO ->      data4("#35A7FF", "#2070CC", "#EAF5FF", true)
            ThemeManager.THEME_CRIMSON ->   data4("#C53A1F", "#8B2A18", "#FFE8E5", true)
            ThemeManager.THEME_COMBAT ->    data4("#3DCC79", "#2A8F55", "#E8FFF2", true)
            ThemeManager.THEME_VIOLET ->    data4("#9B6B9E", "#6B4A7A", "#F5EEFF", true)
            ThemeManager.THEME_PEACH ->     data4("#D66C59", "#A04A38", "#FFF0EC", false)
            ThemeManager.THEME_ARCTIC ->    data4("#4A7AA8", "#336080", "#EAF2FA", false)
            ThemeManager.THEME_CYBERPUNK -> data4("#E8FF00", "#A8BB00", "#FDFFD9", true)
            ThemeManager.THEME_DUNE ->      data4("#DEB651", "#A08030", "#FFF8E8", true)
            else ->                         data4("#96CBFF", "#6B7FCC", "#E8F4FF", true)
        }

        val glow = hexToRgba(primary, 0.85f)
        val textSecondary = hexToRgba(primary, 0.40f)
        
        val wordmarkOverride = if (!darkBg) ".wordmark { fill: #1A1A2E; }" else ""

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <link href="https://fonts.googleapis.com/css2?family=Orbitron:wght@500;600&display=swap" rel="stylesheet">
                <style>
                    :root {
                        --rs-primary: $primary;
                        --rs-secondary: $secondary;
                        --rs-glow: $glow;
                        --rs-text-secondary: $textSecondary;
                        --rs-core: $core;
                    }
                    html, body {
                        margin: 0;
                        padding: 0;
                        background: transparent;
                        overflow: hidden;
                        width: 100%;
                        height: 100%;
                    }
                    svg {
                        width: 100%;
                        height: auto;
                        display: block;
                    }
                    .node { fill: var(--rs-primary); filter: url(#nodeGlow); }
                    .node-core { fill: var(--rs-core); filter: url(#coreGlow); }
                    .line { stroke: var(--rs-primary); stroke-width: 2.2; stroke-linecap: round; opacity: 0.9; filter: url(#lineGlow); }
                    .line-dim { stroke: var(--rs-secondary); stroke-width: 1.5; stroke-linecap: round; opacity: 0.45; }
                    .wordmark { font-family: 'Orbitron', sans-serif; font-size: 88px; font-weight: 600; letter-spacing: 0.12em; fill: var(--rs-primary); }
                    .submark { font-family: 'Orbitron', sans-serif; font-size: 28px; font-weight: 500; letter-spacing: 0.8em; fill: var(--rs-text-secondary); }
                    .rs-trace { stroke: var(--rs-core); stroke-width: 2; opacity: 0.35; fill: none; stroke-linecap: round; stroke-linejoin: round; }
                    $wordmarkOverride
                </style>
            </head>
            <body>
                <svg width="1200" height="400" viewBox="0 0 1200 400" fill="none" xmlns="http://www.w3.org/2000/svg" class="river-song-logo" preserveAspectRatio="xMidYMid meet">
                    <defs>
                        <filter id="lineGlow" x="-100%" y="-100%" width="300%" height="300%"><feGaussianBlur stdDeviation="2.5" result="blur" /><feMerge><feMergeNode in="blur" /><feMergeNode in="SourceGraphic" /></feMerge></filter>
                        <filter id="nodeGlow" x="-100%" y="-100%" width="300%" height="300%"><feGaussianBlur stdDeviation="5" result="blur" /><feMerge><feMergeNode in="blur" /><feMergeNode in="SourceGraphic" /></feMerge></filter>
                        <filter id="coreGlow" x="-100%" y="-100%" width="300%" height="300%"><feGaussianBlur stdDeviation="9" result="blur" /><feMerge><feMergeNode in="blur" /><feMergeNode in="SourceGraphic" /></feMerge></filter>
                    </defs>
                    <g transform="translate(40,60)">
                        <line class="line" x1="150" y1="140" x2="90" y2="80" /><line class="line" x1="150" y1="140" x2="220" y2="95" /><line class="line" x1="150" y1="140" x2="85" y2="200" /><line class="line" x1="150" y1="140" x2="230" y2="210" /><line class="line" x1="150" y1="140" x2="145" y2="245" />
                        <line class="line-dim" x1="90" y1="80" x2="220" y2="95" /><line class="line-dim" x1="90" y1="80" x2="85" y2="200" /><line class="line-dim" x1="220" y1="95" x2="230" y2="210" /><line class="line-dim" x1="85" y1="200" x2="145" y2="245" /><line class="line-dim" x1="145" y1="245" x2="230" y2="210" />
                        <line class="line-dim" x1="50" y1="135" x2="90" y2="80" /><line class="line-dim" x1="50" y1="135" x2="85" y2="200" /><line class="line-dim" x1="255" y1="150" x2="220" y2="95" /><line class="line-dim" x1="255" y1="150" x2="230" y2="210" />
                        <line class="line-dim" x1="120" y1="45" x2="90" y2="80" /><line class="line-dim" x1="120" y1="45" x2="220" y2="95" />
                        <line class="line-dim" x1="180" y1="275" x2="145" y2="245" /><line class="line-dim" x1="180" y1="275" x2="230" y2="210" />
                        <path class="rs-trace" d="M95 75 L145 75 Q175 75 175 105 Q175 132 145 132 L105 132 L175 205" />
                        <path class="rs-trace" d="M220 95 Q250 105 245 130 Q242 152 205 158 Q175 164 180 188 Q188 212 228 214" />
                        <circle class="node" cx="50" cy="135" r="5" /><circle class="node" cx="120" cy="45" r="4" /><circle class="node" cx="255" cy="150" r="5" /><circle class="node" cx="180" cy="275" r="5" />
                        <circle class="node" cx="90" cy="80" r="8" /><circle class="node" cx="220" cy="95" r="8" /><circle class="node" cx="85" cy="200" r="9" /><circle class="node" cx="230" cy="210" r="9" /><circle class="node" cx="145" cy="245" r="8" />
                        <circle class="node-core" cx="150" cy="140" r="18" />
                        <circle class="node" cx="105" cy="132" r="3" /><circle class="node" cx="175" cy="105" r="3" /><circle class="node" cx="205" cy="158" r="3" /><circle class="node" cx="188" cy="212" r="3" />
                    </g>
                    <g transform="translate(380,175)">
                        <text class="wordmark" x="0" y="0">RIVER SONG</text>
                        <text class="submark" x="4" y="55">A I</text>
                    </g>
                </svg>
            </body>
            </html>
        """.trimIndent()
    }

    private fun hexToRgba(hex: String, opacity: Float): String {
        val color = Color.parseColor(hex)
        return "rgba(${Color.red(color)}, ${Color.green(color)}, ${Color.blue(color)}, $opacity)"
    }

    private data class data4(val p: String, val s: String, val c: String, val d: Boolean)
}

fun WebView.applyRiverSongLogo(context: Context) {
    settings.javaScriptEnabled = false
    settings.loadWithOverviewMode = true
    settings.useWideViewPort = true
    setBackgroundColor(Color.TRANSPARENT)
    background = null
    isHorizontalScrollBarEnabled = false
    isVerticalScrollBarEnabled = false
    val html = LogoWebView.buildLogoHtml(context)
    loadDataWithBaseURL(
        "https://fonts.googleapis.com",
        html,
        "text/html",
        "UTF-8",
        null
    )
}
