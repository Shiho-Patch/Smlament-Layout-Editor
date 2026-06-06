package com.example.layoutbuilder.parser

import com.example.layoutbuilder.model.ViewNode
import java.util.Locale

/**
 * 将 [ViewNode] 树序列化为 XML 文本。
 *
 * 输出内容与 Android 资源 / layout XML 保持一致：
 *  - 第一行加入 `<?xml?>` 声明
 *  - 根元素声明 `xmlns:android="http://schemas.android.com/apk/res/android"`
 *    与 `xmlns:app="http://schemas.android.com/apk/res-auto"`
 *  - 属性按插入顺序输出（使用 [LinkedHashMap]）
 */
object ViewToXmlBuilder {

    private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
    private const val APP_NS = "http://schemas.android.com/apk/res-auto"
    private const val INDENT = "    "

    fun toXml(root: ViewNode): String {
        val sb = StringBuilder(256)
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        writeNode(sb, root, 0, isRoot = true)
        return sb.toString()
    }

    private fun writeNode(sb: StringBuilder, node: ViewNode, depth: Int, isRoot: Boolean) {
        val indent = INDENT.repeat(depth)
        sb.append(indent).append('<').append(node.tag)

        if (isRoot) {
            sb.append('\n')
                .append(indent).append(INDENT)
                .append("xmlns:android=\"").append(ANDROID_NS).append("\"\n")
                .append(indent).append(INDENT)
                .append("xmlns:app=\"").append(APP_NS).append("\"")
        }

        for ((k, v) in node.attributes) {
            sb.append('\n').append(indent).append(INDENT)
                .append(k).append("=\"").append(escape(v)).append("\"")
        }

        if (node.children.isEmpty()) {
            sb.append(" />\n")
        } else {
            sb.append(">\n")
            for (c in node.children) {
                writeNode(sb, c, depth + 1, isRoot = false)
            }
            sb.append(indent).append("</").append(node.tag).append(">\n")
        }
    }

    private fun escape(raw: String): String =
        raw.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")

    /** 仅作预览用，带缩进（更紧凑的单行样式） */
    fun toCompactString(root: ViewNode): String =
        toXml(root).lineSequence()
            .filter { it.isNotBlank() }
            .joinToString(separator = "\n") { it.trimStart() }
            .lowercase(Locale.ROOT)
}
