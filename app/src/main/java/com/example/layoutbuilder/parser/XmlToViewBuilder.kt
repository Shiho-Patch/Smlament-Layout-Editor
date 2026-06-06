package com.example.layoutbuilder.parser

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.layoutbuilder.model.ViewNode
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

/**
 * 将 [ViewNode] 树动态实例化为真实的 Android [View] 树。
 *
 * 为了能在设计器中实时预览，只实现有限的属性集合。未实现的属性会被安全忽略，
 * 这样既不会崩溃，也能持续渲染关键布局行为（宽/高/间距/文本/颜色/方向 等）。
 */
object XmlToViewBuilder {

    fun build(ctx: Context, node: ViewNode): View {
        val view = instantiate(ctx, node)
        applyAttributes(view, node)
        if (view is ViewGroup) {
            for (child in node.children) {
                val cv = build(ctx, child)
                view.addView(cv, cv.layoutParams ?: defaultLayoutParams())
            }
        }
        view.setTag(VIEW_NODE_ID_TAG, node.id)
        return view
    }

    // ------------- 视图实例化 -------------

    private fun instantiate(ctx: Context, node: ViewNode): View {
        return when (val tag = node.tag) {
            "View" -> View(ctx)
            "TextView" -> TextView(ctx)
            "Button" -> Button(ctx)
            "EditText" -> EditText(ctx)
            "ImageView" -> ImageView(ctx)
            "CheckBox" -> CheckBox(ctx)
            "RadioButton" -> RadioButton(ctx)
            "ToggleButton" -> ToggleButton(ctx)
            "Switch" -> Switch(ctx)
            "ProgressBar" -> ProgressBar(ctx)
            "SeekBar" -> SeekBar(ctx)
            "LinearLayout" -> LinearLayout(ctx)
            "FrameLayout" -> FrameLayout(ctx)
            "RelativeLayout" -> RelativeLayout(ctx)
            "androidx.constraintlayout.widget.ConstraintLayout" -> ConstraintLayout(ctx)
            "androidx.recyclerview.widget.RecyclerView" -> RecyclerView(ctx)
            "com.google.android.material.button.MaterialButton" -> MaterialButton(ctx)
            "com.google.android.material.card.MaterialCardView" -> MaterialCardView(ctx)
            "com.google.android.material.chip.Chip" -> Chip(ctx)
            "com.google.android.material.textfield.TextInputLayout" -> TextInputLayout(ctx)
            "com.google.android.material.textfield.TextInputEditText" -> TextInputEditText(ctx)
            else -> {
                // 未知控件：退化为 TextView，显示类名以便识别
                TextView(ctx).also {
                    it.text = tag
                    it.gravity = Gravity.CENTER
                    it.setPadding(parseDimen(ctx, "12dp", 0), parseDimen(ctx, "8dp", 0), parseDimen(ctx, "12dp", 0), parseDimen(ctx, "8dp", 0))
                }
            }
        }
    }

    // ------------- 属性应用 -------------

    private fun applyAttributes(view: View, node: ViewNode) {
        val ctx = view.context

        // 基本布局参数：为确保 layout_* 生效，每个视图都附带一个基础 LayoutParams
        val lp = when {
            node.attributes.containsKey("android:layout_width") ||
                node.attributes.containsKey("android:layout_height") -> {
                ViewGroup.MarginLayoutParams(
                    parseSize(ctx, node.attributes["android:layout_width"], ViewGroup.LayoutParams.WRAP_CONTENT),
                    parseSize(ctx, node.attributes["android:layout_height"], ViewGroup.LayoutParams.WRAP_CONTENT)
                )
            }
            else -> defaultLayoutParams()
        }

        // margin
        val margin = node.attributes["android:layout_margin"]?.let { parseDimen(ctx, it, 0) }
        val marginStart = node.attributes["android:layout_marginStart"]?.let { parseDimen(ctx, it, 0) }
            ?: node.attributes["android:layout_marginLeft"]?.let { parseDimen(ctx, it, 0) }
        val marginTop = node.attributes["android:layout_marginTop"]?.let { parseDimen(ctx, it, 0) }
        val marginEnd = node.attributes["android:layout_marginEnd"]?.let { parseDimen(ctx, it, 0) }
            ?: node.attributes["android:layout_marginRight"]?.let { parseDimen(ctx, it, 0) }
        val marginBottom = node.attributes["android:layout_marginBottom"]?.let { parseDimen(ctx, it, 0) }
        val base = margin ?: 0
        lp.setMargins(
            marginStart ?: base,
            marginTop ?: base,
            marginEnd ?: base,
            marginBottom ?: base
        )

        node.attributes["android:layout_weight"]?.toFloatOrNull()?.let {
            if (lp is LinearLayout.LayoutParams) lp.weight = it
        }
        node.attributes["android:layout_gravity"]?.let {
            if (lp is LinearLayout.LayoutParams) lp.gravity = parseGravity(it)
            else if (lp is FrameLayout.LayoutParams) lp.gravity = parseGravity(it)
        }

        view.layoutParams = lp

        // padding
        val pad = node.attributes["android:padding"]?.let { parseDimen(ctx, it, 0) }
        val padStart = node.attributes["android:paddingStart"]?.let { parseDimen(ctx, it, 0) }
            ?: node.attributes["android:paddingLeft"]?.let { parseDimen(ctx, it, 0) }
        val padTop = node.attributes["android:paddingTop"]?.let { parseDimen(ctx, it, 0) }
        val padEnd = node.attributes["android:paddingEnd"]?.let { parseDimen(ctx, it, 0) }
            ?: node.attributes["android:paddingRight"]?.let { parseDimen(ctx, it, 0) }
        val padBottom = node.attributes["android:paddingBottom"]?.let { parseDimen(ctx, it, 0) }
        if (pad != null || padStart != null || padTop != null || padEnd != null || padBottom != null) {
            val b = pad ?: 0
            view.setPadding(padStart ?: b, padTop ?: b, padEnd ?: b, padBottom ?: b)
        }

        // gravity（对于容器/文本）
        node.attributes["android:gravity"]?.let { g ->
            val parsed = parseGravity(g)
            if (view is LinearLayout) view.gravity = parsed
            if (view is FrameLayout) view.foregroundGravity = parsed
            if (view is TextView) view.gravity = parsed
        }
        node.attributes["android:orientation"]?.let {
            if (view is LinearLayout) {
                view.orientation = if (it.equals("horizontal", true))
                    LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
            }
        }
        node.attributes["android:weightSum"]?.toFloatOrNull()?.let {
            if (view is LinearLayout) view.weightSum = it
        }

        // 文本类
        if (view is TextView) {
            node.attributes["android:text"]?.let { view.text = it }
            node.attributes["android:hint"]?.let { view.hint = it }
            node.attributes["android:textColor"]?.let {
                runCatching { view.setTextColor(parseColor(it)) }
            }
            node.attributes["android:textSize"]?.let { raw ->
                val px = parseTextSize(ctx, raw)
                if (px > 0f) view.setTextSize(TypedValue.COMPLEX_UNIT_PX, px)
            }
            node.attributes["android:textStyle"]?.let { style ->
                view.typeface = android.graphics.Typeface.create(
                    view.typeface, when {
                        style.contains("bold", true) && style.contains("italic", true) ->
                            android.graphics.Typeface.BOLD_ITALIC
                        style.contains("bold", true) -> android.graphics.Typeface.BOLD
                        style.contains("italic", true) -> android.graphics.Typeface.ITALIC
                        else -> android.graphics.Typeface.NORMAL
                    }
                )
            }
            node.attributes["android:inputType"]?.let { /* 简化：忽略 */ }
            node.attributes["android:textAlignment"]?.let {
                view.textAlignment = when (it) {
                    "center" -> View.TEXT_ALIGNMENT_CENTER
                    "textEnd" -> View.TEXT_ALIGNMENT_TEXT_END
                    "textStart" -> View.TEXT_ALIGNMENT_TEXT_START
                    "viewEnd" -> View.TEXT_ALIGNMENT_VIEW_END
                    "viewStart" -> View.TEXT_ALIGNMENT_VIEW_START
                    else -> View.TEXT_ALIGNMENT_GRAVITY
                }
            }
        }

        // 选中类
        node.attributes["android:checked"]?.let {
            val checked = it.equals("true", true)
            if (view is Checkable) view.isChecked = checked
        }
        node.attributes["android:enabled"]?.let {
            view.isEnabled = it.equals("true", true)
        }
        node.attributes["android:clickable"]?.let {
            view.isClickable = it.equals("true", true)
        }
        node.attributes["android:focusable"]?.let {
            view.focusable = if (it.equals("true", true))
                View.FOCUSABLE else View.NOT_FOCUSABLE
        }

        // 图像类
        if (view is ImageView) {
            node.attributes["android:scaleType"]?.let { raw ->
                runCatching {
                    view.scaleType = ImageView.ScaleType.valueOf(raw.uppercase())
                }
            }
            node.attributes["android:src"]?.let {
                runCatching {
                    val r = android.R.drawable::class.java
                        .getField(it.substringAfterLast('/'))
                        .getInt(null)
                    view.setImageResource(r)
                }
            }
        }

        // 外观类
        node.attributes["android:background"]?.let { raw ->
            runCatching {
                if (raw.startsWith("#")) view.setBackgroundColor(parseColor(raw))
            }
        }
        node.attributes["android:backgroundTint"]?.let { raw ->
            runCatching {
                androidx.core.view.ViewCompat.setBackgroundTintList(
                    view,
                    android.content.res.ColorStateList.valueOf(parseColor(raw))
                )
            }
        }

        // 可见性 / alpha / elevation
        node.attributes["android:visibility"]?.let {
            view.visibility = when {
                it.equals("invisible", true) -> View.INVISIBLE
                it.equals("gone", true) -> View.GONE
                else -> View.VISIBLE
            }
        }
        node.attributes["android:alpha"]?.toFloatOrNull()?.let { view.alpha = it }
        node.attributes["android:elevation"]?.let { raw ->
            view.elevation = parseDimen(ctx, raw, 0).toFloat()
        }

        // 最小尺寸（保证控件可见）
        if (node.tag != "View" && node.tag != "ImageView" && view !is ViewGroup) {
            view.minimumHeight = view.minimumHeight.coerceAtLeast(parseDimen(ctx, "24dp", 0))
        }
    }

    // ------------- 帮助方法 -------------

    private fun defaultLayoutParams(): ViewGroup.MarginLayoutParams =
        ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

    private fun parseSize(ctx: Context, raw: String?, fallback: Int): Int {
        if (raw == null) return fallback
        return when (raw.lowercase()) {
            "match_parent", "fill_parent" -> ViewGroup.LayoutParams.MATCH_PARENT
            "wrap_content" -> ViewGroup.LayoutParams.WRAP_CONTENT
            else -> parseDimen(ctx, raw, fallback)
        }
    }

    private fun parseDimen(ctx: Context, raw: String, fallback: Int): Int {
        val value = parseRawDimention(ctx, raw)
        return if (value.isNaN()) fallback else value.roundToInt()
    }

    private fun parseTextSize(ctx: Context, raw: String): Float {
        val v = parseRawDimention(ctx, raw)
        return if (v.isNaN()) 0f else v
    }

    /** 解析形如 "16dp" / "20sp" / "10px" 的尺寸字符串（单位必须存在） */
    private fun parseRawDimention(ctx: Context, raw: String): Float {
        val trimmed = raw.trim()
        val m = DIMEN_REGEX.matchEntire(trimmed) ?: return Float.NaN
        val num = m.groupValues[1].toFloatOrNull() ?: return Float.NaN
        val unit = m.groupValues[2].ifEmpty { return Float.NaN }
        return when (unit.lowercase()) {
            "dp", "dip" -> TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, num, ctx.resources.displayMetrics
            )
            "sp" -> TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, num, ctx.resources.displayMetrics
            )
            "px" -> num
            else -> Float.NaN
        }
    }

    private val DIMEN_REGEX = Regex("""([0-9]+(?:\.[0-9]+)?)\s*(dp|dip|sp|px)?""", RegexOption.IGNORE_CASE)

    private fun parseGravity(raw: String): Int {
        var g = Gravity.NO_GRAVITY
        for (part in raw.split('|').map { it.trim() }) {
            g = g or when (part.lowercase()) {
                "top" -> Gravity.TOP
                "bottom" -> Gravity.BOTTOM
                "left" -> Gravity.LEFT
                "right" -> Gravity.RIGHT
                "start" -> Gravity.START
                "end" -> Gravity.END
                "center", "center_vertical|center_horizontal" -> Gravity.CENTER
                "center_vertical", "center_vertical" -> Gravity.CENTER_VERTICAL
                "center_horizontal" -> Gravity.CENTER_HORIZONTAL
                "fill" -> Gravity.FILL
                "fill_vertical" -> Gravity.FILL_VERTICAL
                "fill_horizontal" -> Gravity.FILL_HORIZONTAL
                else -> Gravity.NO_GRAVITY
            }
        }
        return g
    }

    /** 解析 #AARRGGBB / #RRGGBB / #RGB 格式颜色 */
    private fun parseColor(raw: String): Int {
        val s = raw.trim()
        if (!s.startsWith("#")) return 0
        val body = s.substring(1)
        val full = when (body.length) {
            3 -> body.map { "$it$it" }.joinToString("").let { "FF$it" }
            6 -> "FF$body"
            8 -> body
            else -> body.padStart(8, '0').take(8)
        }
        return full.toLongOrNull(16)?.toInt() ?: 0
    }

    /** 用于识别当前选中的视图节点的 Tag key */
    const val VIEW_NODE_ID_TAG = 0x01000000
}
