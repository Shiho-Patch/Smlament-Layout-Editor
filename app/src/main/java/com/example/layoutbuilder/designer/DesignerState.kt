package com.example.layoutbuilder.designer

import com.example.layoutbuilder.model.ViewNode
import com.example.layoutbuilder.model.WidgetLibrary
import com.example.layoutbuilder.model.WidgetTemplate
import java.util.concurrent.atomic.AtomicLong

/**
 * 设计器的共享状态。
 *
 * 为避免依赖 ViewModel/生命周期组件，这里使用一个简单的对象单例。
 * 实际生产项目可以改为使用 Jetpack Navigation + NavHostFragment + Shared ViewModel。
 */
object DesignerState {

    private val idCounter = AtomicLong(1L)

    /** 根节点（始终是一个容器） */
    var root: ViewNode = createDefaultRoot()
        private set

    /** 当前选中节点 id；null 表示未选中 */
    var selectedId: String? = null

    /** 监听器：当树/选中状态发生变化时调用 */
    private val listeners = mutableListOf<ChangeListener>()

    interface ChangeListener {
        fun onTreeChanged()
        fun onSelectionChanged(node: ViewNode?)
    }

    fun addListener(l: ChangeListener) { listeners.add(l) }
    fun removeListener(l: ChangeListener) { listeners.remove(l) }

    fun reset() {
        root = createDefaultRoot()
        selectedId = root.id
        notifyTree()
        notifySelection()
    }

    fun nextId(prefix: String = "n"): String = "$prefix${idCounter.getAndIncrement()}"

    fun findSelected(): ViewNode? = selectedId?.let { root.findById(it) }

    fun select(id: String?) {
        selectedId = id
        notifySelection()
    }

    /** 在当前选中节点（或根节点）下插入一个子节点，返回新增节点 id */
    fun addChild(template: WidgetTemplate): String {
        val parent = findSelected()?.takeIf { isContainer(it) } ?: root
        val node = WidgetLibrary.createNode(template) { nextId() }
        node.parent = parent
        parent.children.add(node)
        selectedId = node.id
        notifyTree()
        notifySelection()
        return node.id
    }

    /** 将给定节点移动到根节点同级的最后（即提升层级——简化处理） */
    fun moveToSibling(node: ViewNode) {
        val parent = node.parent ?: return
        val grandParent = parent.parent ?: return
        node.parent = grandParent
        parent.children.remove(node)
        val idx = grandParent.children.indexOf(parent) + 1
        if (idx >= grandParent.children.size) {
            grandParent.children.add(node)
        } else {
            grandParent.children.add(idx, node)
        }
        notifyTree()
    }

    fun deleteSelected() {
        val sel = findSelected() ?: return
        if (sel === root) return
        sel.detachFromParent()
        selectedId = sel.parent?.id ?: root.id
        notifyTree()
        notifySelection()
    }

    fun updateAttribute(key: String, value: String) {
        val sel = findSelected() ?: return
        if (value.isBlank()) sel.attributes.remove(key)
        else sel.attributes[key] = value
        notifyTree()
    }

    fun removeAttribute(key: String) {
        val sel = findSelected() ?: return
        sel.attributes.remove(key)
        notifyTree()
    }

    fun isContainer(node: ViewNode): Boolean =
        WidgetLibrary.byTag[node.tag]?.isContainer == true ||
                node.tag.endsWith("Layout", ignoreCase = true) ||
                node.tag.endsWith("Group", ignoreCase = true) ||
                node.tag == "ScrollView" ||
                node.tag == "HorizontalScrollView"

    private fun notifyTree() { listeners.toList().forEach { it.onTreeChanged() } }
    private fun notifySelection() { listeners.toList().forEach { it.onSelectionChanged(findSelected()) } }

    /** 默认的示例根节点：带一个文本框与按钮的线性布局。 */
    private fun createDefaultRoot(): ViewNode {
        val root = ViewNode(
            id = nextId("root"),
            tag = "LinearLayout",
            attributes = linkedMapOf(
                "android:layout_width" to "match_parent",
                "android:layout_height" to "wrap_content",
                "android:orientation" to "vertical",
                "android:padding" to "16dp"
            )
        )

        val title = ViewNode(
            id = nextId(),
            tag = "TextView",
            attributes = linkedMapOf(
                "android:layout_width" to "wrap_content",
                "android:layout_height" to "wrap_content",
                "android:text" to "欢迎使用可视化布局器",
                "android:textSize" to "20sp",
                "android:textStyle" to "bold"
            ),
            parent = root
        )

        val btn = ViewNode(
            id = nextId(),
            tag = "com.google.android.material.button.MaterialButton",
            attributes = linkedMapOf(
                "android:layout_width" to "wrap_content",
                "android:layout_height" to "wrap_content",
                "android:layout_marginTop" to "12dp",
                "android:text" to "示例按钮"
            ),
            parent = root
        )

        root.children.add(title)
        root.children.add(btn)
        return root
    }
}
