package com.example.teleprompter

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 首页：稿本列表，支持新建 / 导入 / 打开播放 / 长按删除 */
class ScriptListActivity : AppCompatActivity() {
    private lateinit var store: ScriptStore
    private lateinit var listView: ListView
    private val scripts = mutableListOf<Script>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_script_list)
        store = ScriptStore(this)
        listView = findViewById(R.id.listView)

        findViewById<View>(R.id.fabAdd).setOnClickListener {
            val intent = Intent(this, EditorActivity::class.java)
            intent.putExtra("id", store.newId())
            startActivity(intent)
        }
        findViewById<View>(R.id.btnImport).setOnClickListener {
            startActivity(Intent(this, ImportActivity::class.java))
        }
        findViewById<View>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        listView.setOnItemClickListener { _, _, pos, _ -> openPlayer(scripts[pos].id) }
        listView.setOnItemLongClickListener { _, _, pos, _ ->
            val s = scripts[pos]
            AlertDialog.Builder(this)
                .setTitle("删除「${s.title.ifBlank { "(无标题)" }}」?")
                .setMessage("删除后无法恢复")
                .setPositiveButton("删除") { _, _ ->
                    store.delete(s.id)
                    refresh()
                }
                .setNegativeButton("取消", null)
                .show()
            true
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        scripts.clear()
        scripts.addAll(store.getAll())
        listView.adapter = object : ArrayAdapter<Script>(
            this, R.layout.item_script, R.id.tvTitle, scripts
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = convertView ?: layoutInflater.inflate(R.layout.item_script, parent, false)
                val s = scripts[position]
                v.findViewById<TextView>(R.id.tvTitle).text = s.title.ifBlank { "(无标题)" }
                v.findViewById<TextView>(R.id.tvMeta).text =
                    "${s.wordCount} 字 · ${formatDate(s.updatedAt)}"
                return v
            }
        }
    }

    private fun openPlayer(id: String) {
        startActivity(Intent(this, PlayerActivity::class.java).putExtra("id", id))
    }

    private fun formatDate(t: Long): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(t))
    }
}
