package com.example.layoutbuilder.model

/**
 * 描述一个控件在树中的节点。
 *
 * 每个节点表示一个 XML 元素（View/ViewGroup），拥有：
 *  - tag：视图类名（如 "TextView"）
 *  - attributes：属性键值对（如 "android:layout_width" to "wrap_content"）
 *  - children：子节点列表（针对 ViewGroup）
 *  - id：唯一 id，用于高亮/选中
 */
data class ViewNode(
    val id: String,
    var tag: String,
    val attributes: MutableMap<String, String> = linkedMapOf(),
    val children: MutableList<ViewNode> = mutableListOf(),
    var parent: ViewNode? = null
) {
    fun prettyName(): String {
        val idPart = attributes["android:id"]?.let { " ($it)" } ?: ""
        val textPart = attributes["android:text"]?.let { "  \"${it.take(12)}\"" } ?: ""
        return "$tag$idPart$textPart"
    }

    /** 查找自身或子节点中包含 id 的节点 */
    fun findById(targetId: String): ViewNode? {
        if (id == targetId) return this
        for (c in children) {
            val r = c.findById(targetId)
            if (r != null) return r
        }
        return null
    }

    /** 从父节点中移除自身 */
    fun detachFromParent() {
        parent?.children?.remove(this)
        parent = null
    }

    /** 深度拷贝 */
    fun deepCopy(newId: (String) -> String = { it + "_copy" }): ViewNode {
        val clone = ViewNode(
            id = newId(id),
            tag = tag,
            attributes = LinkedHashMap(attributes),
            children = mutableListOf()
        )
        for (c in children) {
            val cc = c.deepCopy(newId)
            cc.parent = clone
            clone.children.add(cc)
        }
        return clone
    }
}
