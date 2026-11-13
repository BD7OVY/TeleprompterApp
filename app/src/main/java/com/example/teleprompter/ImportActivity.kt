package com.example.teleprompter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/** 导入页：用系统文件选择器(SAF)读取 .txt，无需存储权限 */
class ImportActivity : AppCompatActivity() {
    private lateinit var store: ScriptStore
    private val PICK = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import)
        store = ScriptStore(this)
        findViewById<Button>(R.id.btnPick).setOnClickListener { pickFile() }
    }

    private fun pickFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK && resultCode == RESULT_OK && data != null) {
            val uri = data.data ?: return
            val name = queryName(uri)
            val lower = name?.lowercase() ?: ""
            if (!lower.endsWith(".txt") && !lower.endsWith(".text")) {
                Toast.makeText(this, "请选择 .txt 文本文件", Toast.LENGTH_SHORT).show()
                return
            }
            try {
                contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                    val text = reader.readText()
                    val title = (name ?: "导入稿本")
                        .removeSuffix(".txt")
                        .removeSuffix(".text")
                    val script = Script(
                        id = store.newId(),
                        title = title,
                        content = text,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    store.save(script)
                    Toast.makeText(this, "导入成功", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, PlayerActivity::class.java).putExtra("id", script.id))
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "读取失败：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun queryName(uri: Uri): String? {
        var name: String? = null
        val c = contentResolver.query(uri, null, null, null, null)
        c?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = it.getString(idx)
            }
        }
        return name
    }
}
