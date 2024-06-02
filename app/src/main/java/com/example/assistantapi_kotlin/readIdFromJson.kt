package com.example.assistantapi_kotlin

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.InputStreamReader

fun readIdFromJson(context: Context, type: String): String {
    val TAG = "readAndWriteIdToJson"
    val fileName = "chatGptIds.json"
    val filePath = context.filesDir.absolutePath + "/" + fileName
    val file = File(filePath)
    lateinit var jsonObj: JSONObject

    try {
        if (file.exists() && file.length() > 0) {
            val bufferedReader = BufferedReader(InputStreamReader(file.inputStream()))
            val jsonStr = bufferedReader.use { it.readText() }
            jsonObj = JSONObject(jsonStr)

            return jsonObj.getString(type)
        }
        return ""
    } catch (e: Exception) {
        Log.e(TAG, "Error: ${e.message}")
        return ""
    }
}