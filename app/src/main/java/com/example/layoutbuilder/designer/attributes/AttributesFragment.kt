package com.example.layoutbuilder.designer.attributes

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.layoutbuilder.databinding.FragmentAttributesBinding
import com.example.layoutbuilder.databinding.ItemAttributeBinding
import androidx.fragment.app.Fragment
import com.example.layoutbuilder.designer.DesignerState
import com.example.layoutbuilder.model.ViewNode
import com.example.layoutbuilder.model.WidgetLibrary

/**
 * 属性编辑面板。
 *
 * 顶部显示当前选中节点信息；
 * 下方显示一个「常用属性列表」，展示当前节点已有属性，
 * 还可以通过「添加属性」快捷选择 [WidgetLibrary.commonAttributeKeys] 中的项。
 */
class AttributesFragment : Fragment() {

    private var _binding: FragmentAttributesBinding? = null
    private val binding get() = _binding!!

    private val stateListener = object : DesignerState.ChangeListener {
        override fun onTreeChanged() { refresh() }
        override fun onSelectionChanged(node: ViewNode?) { refresh() }
    }

    private lateinit var adapter: AttributeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttributesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DesignerState.addListener(stateListener)
        adapter = AttributeAdapter(::onEdit, ::onRemove)
        binding.recycler.layoutManager = LinearLayoutManager(context)
        binding.recycler.adapter = adapter

        binding.buttonAddAttribute.setOnClickListener { showAddAttributeMenu() }
        refresh()
    }

    override fun onDestroyView() {
        DesignerState.removeListener(stateListener)
        super.onDestroyView()
        _binding = null
    }

    private fun refresh() {
        val sel = DesignerState.findSelected()
        binding.selectedInfo.text = if (sel == null) {
            "（未选中任何视图）"
        } else {
            "已选中：${sel.tag}\n属性数量：${sel.attributes.size}"
        }
        adapter.submit(sel?.attributes?.toList().orEmpty())
    }

    private fun onEdit(key: String, value: String) {
        DesignerState.updateAttribute(key, value)
    }

    private fun onRemove(key: String) {
        DesignerState.removeAttribute(key)
    }

    private fun showAddAttributeMenu() {
        val ctx = requireContext()
        val keys = WidgetLibrary.commonAttributeKeys.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(ctx)
            .setTitle("选择要添加的属性")
            .setItems(keys) { _, which ->
                val key = keys[which]
                val sel = DesignerState.findSelected()
                if (sel != null && !sel.attributes.containsKey(key)) {
                    sel.attributes[key] = ""
                    DesignerState.updateAttribute(key, "")
                }
            }
            .show()
    }
}

private class AttributeAdapter(
    private val onEdit: (String, String) -> Unit,
    private val onRemove: (String) -> Unit
) : RecyclerView.Adapter<AttributeHolder>() {

    private val data = mutableListOf<Pair<String, String>>()

    fun submit(list: List<Pair<String, String>>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttributeHolder {
        val b = ItemAttributeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AttributeHolder(b, onEdit, onRemove)
    }

    override fun onBindViewHolder(holder: AttributeHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
}

private class AttributeHolder(
    private val b: ItemAttributeBinding,
    private val onEdit: (String, String) -> Unit,
    private val onRemove: (String) -> Unit
) : RecyclerView.ViewHolder(b.root) {

    private var currentKey: String? = null

    init {
        b.editValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                val key = currentKey ?: return
                onEdit(key, s?.toString().orEmpty())
            }
        })
        b.buttonDelete.setOnClickListener {
            val key = currentKey ?: return@setOnClickListener
            onRemove(key)
        }
    }

    fun bind(item: Pair<String, String>) {
        currentKey = item.first
        b.labelKey.text = item.first
        if (b.editValue.text.toString() != item.second) {
            b.editValue.setTextKeepState(item.second)
        }
    }
}
