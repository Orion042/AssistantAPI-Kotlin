package com.example.assistantapi_kotlin

import android.content.Context
import android.util.Log
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.file.FileUpload
import com.aallam.openai.api.file.Purpose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toPath
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader

suspend fun createRagFile(context: Context, contents: String): Boolean {
    val TAG = "createRagFile"
    val fileName = "rag.json"
    val filePath = context.filesDir.absolutePath + "/" + fileName
    val file = File(filePath)
    lateinit var jsonObj: JSONObject

    try {
        if (file.exists() && file.length() > 0) {
            val bufferedReader = BufferedReader(InputStreamReader(file.inputStream()))
            val jsonStr = bufferedReader.use { it.readText() }
            jsonObj = JSONObject(jsonStr)
        } else {
            jsonObj = JSONObject()

            jsonObj.put("rag_contents", "")

            FileWriter(file).use { writer ->
                writer.write(jsonObj.toString(4))
            }
        }
        jsonObj.put("rag_contents", contents)


        FileWriter(file).use { writer ->
            writer.write(jsonObj.toString(4))
        }

        Log.d(TAG, "JSON書き込み成功")

    } catch (e: Exception) {
        Log.e(TAG, "Error: ${e.message}")
        return false
    }

    return true
}