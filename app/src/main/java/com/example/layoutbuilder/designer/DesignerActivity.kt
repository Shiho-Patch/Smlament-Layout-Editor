package com.example.layoutbuilder.designer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.layoutbuilder.R
import com.example.layoutbuilder.databinding.ActivityDesignerBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.example.layoutbuilder.designer.widget.WidgetPaletteFragment
import com.example.layoutbuilder.designer.canvas.DesignerCanvasFragment
import com.example.layoutbuilder.designer.attributes.AttributesFragment
import com.example.layoutbuilder.designer.code.XmlCodeFragment

/**
 * 布局编辑主界面。
 *
 * 使用 Material 3 风格的 TabLayout + ViewPager2，承载 4 个 fragment：
 *  - 控件库 / 画布 / 属性 / XML 代码
 */
class DesignerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDesignerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDesignerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_designer)

        val adapter = PagerAdapter(this)
        binding.pager.adapter = adapter
        TabLayoutMediator(binding.tabs, binding.pager) { tab, position ->
            tab.text = tabTitle(position)
        }.attach()
    }

    private fun tabTitle(position: Int): CharSequence = getString(
        when (position) {
            0 -> R.string.tab_widgets
            1 -> R.string.tab_canvas
            2 -> R.string.tab_attributes
            3 -> R.string.tab_code
            else -> R.string.tab_widgets
        }
    )

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.designer, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            R.id.action_delete -> {
                DesignerState.deleteSelected()
                true
            }
            R.id.action_clear -> {
                DesignerState.reset()
                true
            }
            R.id.action_copy_xml -> {
                copyXmlToClipboard()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun copyXmlToClipboard() {
        val xml = com.example.layoutbuilder.parser.ViewToXmlBuilder.toXml(DesignerState.root)
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("layout-xml", xml))
        Toast.makeText(this, R.string.msg_copied, Toast.LENGTH_SHORT).show()
    }

    private inner class PagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 4
        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> WidgetPaletteFragment()
            1 -> DesignerCanvasFragment()
            2 -> AttributesFragment()
            3 -> XmlCodeFragment()
            else -> error("Unknown tab")
        }
    }

    companion object {
        fun start(ctx: Context) { ctx.startActivity(Intent(ctx, DesignerActivity::class.java)) }
    }
}
