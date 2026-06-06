package com.example.layoutbuilder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.layoutbuilder.databinding.ActivityMainBinding
import com.example.layoutbuilder.designer.DesignerActivity

/**
 * 应用主入口：Material 3 风格的欢迎页。
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        binding.buttonStart.setOnClickListener {
            DesignerActivity.start(this)
        }
        binding.buttonAbout.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.action_about)
                .setMessage("基于 Material Design 3 的可视化布局编辑器示例项目，使用 Kotlin 编写。")
                .setPositiveButton("确定", null)
                .show()
        }
    }
}
