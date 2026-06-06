package com.example.layoutbuilder.designer.code

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.layoutbuilder.databinding.FragmentXmlCodeBinding
import com.example.layoutbuilder.designer.DesignerState
import com.example.layoutbuilder.model.ViewNode
import com.example.layoutbuilder.parser.ViewToXmlBuilder

/**
 * XML 代码预览页。
 *
 * 展示 [DesignerState.root] 对应的 XML 布局文本。
 */
class XmlCodeFragment : Fragment() {

    private var _binding: FragmentXmlCodeBinding? = null
    private val binding get() = _binding!!

    private val listener = object : DesignerState.ChangeListener {
        override fun onTreeChanged() { refresh() }
        override fun onSelectionChanged(node: ViewNode?) = Unit
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentXmlCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DesignerState.addListener(listener)
        refresh()
    }

    override fun onDestroyView() {
        DesignerState.removeListener(listener)
        super.onDestroyView()
        _binding = null
    }

    private fun refresh() {
        val xml = ViewToXmlBuilder.toXml(DesignerState.root)
        binding.xmlText.text = xml
    }
}
