package com.example.layoutbuilder.model

/**
 * 控件库：预定义了一批常用控件模板。
 *
 * 每个模板包含：
 *  - tag：视图的完整类名（或短名）
 *  - title：中文显示名
 *  - isContainer：是否可以放置子视图（ViewGroup）
 *  - defaultAttrs：生成节点时默认的属性
 */
data class WidgetTemplate(
    val tag: String,
    val title: String,
    val isContainer: Boolean = false,
    val defaultAttrs: Map<String, String> = emptyMap(),
    val iconRes: Int? = null
)

data class WidgetCategory(
    val id: String,
    val title: String,
    val items: List<WidgetTemplate>
)

object WidgetLibrary {

    /** 所有分类 */
    val categories: List<WidgetCategory> by lazy { buildCategories() }

    /** 扁平化的所有模板（tag -> template） */
    val byTag: Map<String, WidgetTemplate> by lazy {
        categories.flatMap { it.items }.associateBy { it.tag }
    }

    /** 通用基础属性：任意新增的控件都会带上这些属性 */
    private val baseLayoutAttrs: Map<String, String> = mapOf(
        "android:layout_width" to "wrap_content",
        "android:layout_height" to "wrap_content"
    )

    /** 可选的、针对某类控件的常用属性（用于属性编辑面板的快捷列表） */
    val commonAttributeKeys: List<String> = listOf(
        "android:id",
        "android:layout_width",
        "android:layout_height",
        "android:layout_margin",
        "android:layout_marginStart",
        "android:layout_marginTop",
        "android:layout_marginEnd",
        "android:layout_marginBottom",
        "android:padding",
        "android:paddingStart",
        "android:paddingTop",
        "android:paddingEnd",
        "android:paddingBottom",
        "android:gravity",
        "android:layout_gravity",
        "android:orientation",
        "android:weightSum",
        "android:layout_weight",
        "android:text",
        "android:textSize",
        "android:textColor",
        "android:textStyle",
        "android:hint",
        "android:inputType",
        "android:background",
        "android:backgroundTint",
        "android:src",
        "android:scaleType",
        "android:visibility",
        "android:alpha",
        "android:elevation",
        "android:checked",
        "android:enabled",
        "android:clickable",
        "android:focusable"
    )

    private fun buildCategories(): List<WidgetCategory> {
        val common = WidgetCategory(
            id = "common",
            title = "常用",
            items = listOf(
                WidgetTemplate(
                    tag = "TextView",
                    title = "文本框",
                    defaultAttrs = baseLayoutAttrs + mapOf(
                        "android:text" to "文本框"
                    )
                ),
                WidgetTemplate(
                    tag = "Button",
                    title = "按钮",
                    defaultAttrs = baseLayoutAttrs + mapOf("android:text" to "按钮")
                ),
                WidgetTemplate(
                    tag = "ImageView",
                    title = "图片框",
                    defaultAttrs = baseLayoutAttrs + mapOf(
                        "android:src" to "@android:drawable/sym_def_app_icon",
                        "android:scaleType" to "centerInside"
                    )
                ),
                WidgetTemplate(
                    tag = "EditText",
                    title = "输入框",
                    defaultAttrs = baseLayoutAttrs + mapOf(
                        "android:hint" to "请输入…",
                        "android:text" to ""
                    )
                ),
                WidgetTemplate(
                    tag = "View",
                    title = "占位视图",
                    defaultAttrs = baseLayoutAttrs + mapOf(
                        "android:background" to "#6750A4",
                        "android:layout_width" to "120dp",
                        "android:layout_height" to "120dp"
                    )
                )
            )
        )

        val layouts = WidgetCategory(
            id = "layout",
            title = "布局",
            items = listOf(
                WidgetTemplate(
                    tag = "LinearLayout",
                    title = "线性布局",
                    isContainer = true,
                    defaultAttrs = baseLayoutAttrs + mapOf(
                        "android:layout_width" to "match_parent",
                        "android:layout_height" to "wrap_content",
                        "android:orientation" to "vertical"
                    )
                ),
                WidgetTemplate(
                    tag = "FrameLayout",
                    title = "帧布局",
                    isContainer = true,
                    defaultAttrs = baseLayoutAttrs + mapOf(
                        "android:layout_width" to "match_parent",
                        "android:layout_height" to "wrap_content"
                    )
                ),
                WidgetTemplate(
                    tag = "androidx.constraintlayout.widget.ConstraintLayout",
                    title = "约束布局",
                    isContainer = true,
                    defaultAttrs = baseLayoutAttrs + mapOf(
                        "android:layout_width" to "match_parent",
                        "android:layout_height" to "wrap_content"
                    )
                ),
                WidgetTemplate(
                    tag = "androidx.recyclerview.widget.RecyclerView",
                    title = "列表视图",
                    isContainer = false,
                    defaultAttrs = baseLayoutAttrs + mapOf(
                        "android:layout_width" to "match_parent",
                        "android:layout_height" to "wrap_content"
                    )
                )
            )
        )

        val text = WidgetCategory(
            id = "text",
            title = "文本 & 输入",
            items = listOf(
                WidgetTemplate(
                    tag = "com.google.android.material.textfield.TextInputLayout",
                    title = "文本输入容器",
                    isContainer = true,
                    defaultAttrs = baseLayoutAttrs + mapOf(
                        "android:layout_width" to "match_parent",
                        "android:layout_height" to "wrap_content",
                        "android:hint" to "用户名"
                    )
                ),
                WidgetTemplate(
                    tag = "com.google.android.material.textfield.TextInputEditText",
                    title = "文本输入",
                    defaultAttrs = baseLayoutAttrs + mapOf(
                        "android:layout_width" to "match_parent",
                        "android:layout_height" to "wrap_content"
                    )
                ),
                WidgetTemplate(
                    tag = "CheckBox",
                    title = "多选框",
                    defaultAttrs = baseLayoutAttrs + mapOf("android:text" to "多选")
                ),
                WidgetTemplate(
                    tag = "RadioButton",
                    title = "单选按钮",
                    defaultAttrs = baseLayoutAttrs + mapOf("android:text" to "选项")
                ),
                WidgetTemplate(
                    tag = "ToggleButton",
                    title = "切换按钮",
                    defaultAttrs = baseLayoutAttrs
                )
            )
        )

        val compound = WidgetCategory(
            id = "compound",
            title = "复合控件",
            items = listOf(
                WidgetTemplate(
                    tag = "com.google.android.material.button.MaterialButton",
                    title = "MD3 按钮",
                    defaultAttrs = baseLayoutAttrs + mapOf("android:text" to "MD3 按钮")
                ),
                WidgetTemplate(
                    tag = "com.google.android.material.card.MaterialCardView",
                    title = "MD3 卡片",
                    isContainer = true,
                    defaultAttrs = baseLayoutAttrs + mapOf(
                        "android:layout_width" to "match_parent",
                        "android:layout_height" to "wrap_content",
                        "android:layout_margin" to "8dp"
                    )
                ),
                WidgetTemplate(
                    tag = "com.google.android.material.chip.Chip",
                    title = "Chip 标签",
                    defaultAttrs = baseLayoutAttrs + mapOf("android:text" to "标签")
                ),
                WidgetTemplate(
                    tag = "ProgressBar",
                    title = "进度条",
                    defaultAttrs = baseLayoutAttrs
                ),
                WidgetTemplate(
                    tag = "SeekBar",
                    title = "滑动条",
                    defaultAttrs = baseLayoutAttrs + mapOf(
                        "android:layout_width" to "match_parent"
                    )
                )
            )
        )

        return listOf(common, layouts, text, compound)
    }

    /**
     * 为某个模板创建一个新的 [ViewNode]，自动分配唯一 id。
     */
    fun createNode(template: WidgetTemplate, idGenerator: () -> String): ViewNode {
        return ViewNode(
            id = idGenerator(),
            tag = template.tag,
            attributes = LinkedHashMap(template.defaultAttrs)
        )
    }
}
