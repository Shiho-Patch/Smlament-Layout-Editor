package com.example.layoutbuilder.designer.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.layoutbuilder.databinding.FragmentWidgetPaletteBinding
import com.example.layoutbuilder.databinding.ItemWidgetCategoryBinding
import com.example.layoutbuilder.databinding.ItemWidgetTemplateBinding
import com.example.layoutbuilder.designer.DesignerState
import com.example.layoutbuilder.model.WidgetCategory
import com.example.layoutbuilder.model.WidgetLibrary
import com.example.layoutbuilder.model.WidgetTemplate

/**
 * 控件库面板。
 *
 * 以「分类 → 子项（网格）」的两级列表呈现，点击任意控件，
 * 会将模板实例化并插入到当前选中节点（或根节点）的 children 末尾。
 */
class WidgetPaletteFragment : Fragment() {

    private var _binding: FragmentWidgetPaletteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWidgetPaletteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = CategoryAdapter(WidgetLibrary.categories)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class CategoryAdapter(val data: List<WidgetCategory>) :
        RecyclerView.Adapter<CategoryHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
            val b = ItemWidgetCategoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return CategoryHolder(b)
        }

        override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
            holder.bind(data[position])
        }

        override fun getItemCount(): Int = data.size
    }

    private inner class CategoryHolder(val b: ItemWidgetCategoryBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(category: WidgetCategory) {
            b.categoryTitle.text = category.title
            b.childGrid.layoutManager = GridLayoutManager(context, 2)
            b.childGrid.adapter = TemplateAdapter(category.items)
        }
    }

    private inner class TemplateAdapter(val items: List<WidgetTemplate>) :
        RecyclerView.Adapter<TemplateHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateHolder {
            val b = ItemWidgetTemplateBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return TemplateHolder(b)
        }

        override fun onBindViewHolder(holder: TemplateHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size
    }

    private inner class TemplateHolder(val b: ItemWidgetTemplateBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(item: WidgetTemplate) {
            b.title.text = item.title
            b.tag.text = item.tag.substringAfterLast('.')
            b.badge.text = if (item.isContainer) "容器" else "控件"
            b.root.setOnClickListener {
                DesignerState.addChild(item)
            }
        }
    }
}
