package com.example.layoutbuilder.designer.canvas

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.Shape
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.example.layoutbuilder.databinding.FragmentDesignerCanvasBinding
import com.example.layoutbuilder.designer.DesignerState
import com.example.layoutbuilder.model.ViewNode
import com.example.layoutbuilder.parser.XmlToViewBuilder

/**
 * 实时预览画布。
 *
 * - 按照 [DesignerState.root] 渲染一棵 View 树；
 * - 点击任意子视图可切换「选中」状态；
 * - 选中的视图会有高亮描边。
 */
class DesignerCanvasFragment : Fragment() {

    private var _binding: FragmentDesignerCanvasBinding? = null
    private val binding get() = _binding!!

    private val stateListener = object : DesignerState.ChangeListener {
        override fun onTreeChanged() { rebuildTree() }
        override fun onSelectionChanged(node: ViewNode?) { refreshHighlights() }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDesignerCanvasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DesignerState.addListener(stateListener)
        binding.btnSelectParent.setOnClickListener {
            val sel = DesignerState.findSelected() ?: return@setOnClickListener
            DesignerState.select(sel.parent?.id ?: DesignerState.root.id)
        }
        binding.btnAddAsChild.setOnClickListener {
            // 跳转到控件库 Tab
            val pager = activity?.findViewById<androidx.viewpager2.widget.ViewPager2>(com.example.layoutbuilder.R.id.pager)
            pager?.currentItem = 0
        }
        rebuildTree()
    }

    override fun onDestroyView() {
        DesignerState.removeListener(stateListener)
        super.onDestroyView()
        _binding = null
    }

    private fun rebuildTree() {
        val ctx = binding.previewContainer.context
        binding.previewContainer.removeAllViews()

        // 渲染根视图
        val rendered = XmlToViewBuilder.build(ctx, DesignerState.root)
        // 覆盖根视图的宽度为 match_parent，使容器撑满
        rendered.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        binding.previewContainer.addView(rendered)

        // 为整棵树的每个节点安装点击监听
        bindClickRecursively(rendered)
        refreshHighlights()
    }

    private fun bindClickRecursively(v: View) {
        val id = v.getTag(XmlToViewBuilder.VIEW_NODE_ID_TAG) as? String ?: return
        v.setOnClickListener {
            DesignerState.select(id)
        }
        if (v is ViewGroup) {
            for (c in v.children) bindClickRecursively(c)
        }
    }

    private fun refreshHighlights() {
        val selectedId = DesignerState.selectedId
        applyHighlightRecursively(binding.previewContainer, selectedId)
    }

    private fun applyHighlightRecursively(v: View, selectedId: String?) {
        val id = v.getTag(XmlToViewBuilder.VIEW_NODE_ID_TAG) as? String
        if (id != null && id == selectedId) {
            v.background = SelectedBorderDrawable()
        } else if (v.background is SelectedBorderDrawable) {
            v.background = null
        }
        if (v is ViewGroup) {
            for (c in v.children) applyHighlightRecursively(c, selectedId)
        }
    }

    /** 简单的描边绘制，不影响内部的背景。 */
    private class SelectedBorderDrawable : ShapeDrawable(object : Shape() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.parseColor("#6750A4")
            strokeWidth = 6f
        }
        private val rect = Rect()
        override fun draw(canvas: Canvas, p: Paint) {
            canvas.getClipBounds(rect)
            canvas.drawRect(rect, paint)
        }
    })
}
