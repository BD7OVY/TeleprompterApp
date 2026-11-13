package com.example.teleprompter

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * 稿本数据模型
 * 用内部存储保存：索引(index.json)记录元数据，正文单独存 <id>.txt
 * 不依赖数据库，简单可靠，非技术用户也易懂
 */
data class Script(
    val id: String,
    var title: String,
    var content: String,
    val createdAt: Long,
    var updatedAt: Long
) {
    val wordCount: Int
        get() = content.length
}

/** 稿本仓库：负责增删改查与文件读写 */
class ScriptStore(private val context: Context) {

    private val dir: File = File(context.filesDir, "scripts").apply { mkdirs() }
    private val indexFile = File(dir, "index.json")

    private fun loadIndex(): MutableList<Script> {
        if (!indexFile.exists()) return mutableListOf()
        val arr = JSONArray(indexFile.readText())
        val list = mutableListOf<Script>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val id = o.getString("id")
            val contentFile = File(dir, "$id.txt")
            val content = if (contentFile.exists()) contentFile.readText() else ""
            list.add(
                Script(
                    id = id,
                    title = o.getString("title"),
                    content = content,
                    createdAt = o.getLong("createdAt"),
                    updatedAt = o.getLong("updatedAt")
                )
            )
        }
        return list
    }

    private fun saveIndex(list: List<Script>) {
        val arr = JSONArray()
        for (s in list) {
            val o = JSONObject()
            o.put("id", s.id)
            o.put("title", s.title)
            o.put("createdAt", s.createdAt)
            o.put("updatedAt", s.updatedAt)
            arr.put(o)
            File(dir, "${s.id}.txt").writeText(s.content)
        }
        indexFile.writeText(arr.toString())
    }

    fun getAll(): List<Script> = loadIndex().sortedByDescending { it.updatedAt }

    fun get(id: String): Script? = loadIndex().firstOrNull { it.id == id }

    fun save(script: Script) {
        val list = loadIndex().toMutableList()
        val idx = list.indexOfFirst { it.id == script.id }
        script.updatedAt = System.currentTimeMillis()
        if (idx >= 0) list[idx] = script else list.add(script)
        saveIndex(list)
    }

    fun delete(id: String) {
        val list = loadIndex().toMutableList()
        list.removeIf { it.id == id }
        File(dir, "$id.txt").delete()
        saveIndex(list)
    }

    fun newId(): String = UUID.randomUUID().toString()
}
