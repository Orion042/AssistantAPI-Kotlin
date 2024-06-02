package com.example.assistantapi_kotlin

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader

fun writeIdToJson(context: Context, type: String, id: String): Boolean {
    val TAG = "writeIdToJson"
    val fileName = "chatGptIds.json"
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

            jsonObj.put("assistant_id", "")
            jsonObj.put("thread_id", "")
            jsonObj.put("file_id", "")

            FileWriter(file).use { writer ->
                writer.write(jsonObj.toString(4))
            }
        }

        when (type) {
            "assistant_id" -> jsonObj.put("assistant_id", id)
            "thread_id" -> jsonObj.put("thread_id", id)
            "file_id" -> jsonObj.put("file_id", id)
            else -> return false
        }

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