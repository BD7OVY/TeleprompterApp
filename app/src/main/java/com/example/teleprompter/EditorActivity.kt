package com.example.teleprompter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

/** 编辑页：写标题 + 正文，可保存返回，也可直接“预览播放”进入提词 */
class EditorActivity : AppCompatActivity() {
    private lateinit var store: ScriptStore
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private var scriptId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)
        store = ScriptStore(this)
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)

        scriptId = intent.getStringExtra("id") ?: store.newId()
        val existing = store.get(scriptId)
        if (existing != null) {
            etTitle.setText(existing.title)
            etContent.setText(existing.content)
        }

        findViewById<Button>(R.id.btnSave).setOnClickListener { saveAndExit() }
        findViewById<Button>(R.id.btnPreview).setOnClickListener { saveAndPreview() }
    }

    private fun currentScript(): Script {
        val now = System.currentTimeMillis()
        val ex = store.get(scriptId)
        return Script(
            id = scriptId,
            title = etTitle.text.toString().trim(),
            content = etContent.text.toString(),
            createdAt = ex?.createdAt ?: now,
            updatedAt = now
        )
    }

    private fun saveAndExit() {
        store.save(currentScript())
        finish()
    }

    private fun saveAndPreview() {
        store.save(currentScript())
        startActivity(Intent(this, PlayerActivity::class.java).putExtra("id", scriptId))
    }
}
