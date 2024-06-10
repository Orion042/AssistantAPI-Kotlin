package com.example.assistantapi_kotlin

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.assistant.AssistantRequest
import com.aallam.openai.api.assistant.AssistantTool
import com.aallam.openai.api.file.FileId
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.file.FileUpload
import com.aallam.openai.api.file.Purpose
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.example.assistantapi_kotlin.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.time.Duration.Companion.seconds

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    var openAi: OpenAI? = null

    var chatGptInstruction = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        openAi = OpenAI(
            token = BuildConfig.API_KEY,
            timeout = Timeout(socket = 60.seconds)
        )

        initAssistant()

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_chat, R.id.navigation_settings
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        setupWithNavController(binding.navView, navController)
    }

    @OptIn(BetaOpenAI::class)
    private fun initAssistant() {
        CoroutineScope(Dispatchers.IO).launch {

            if(readIdFromJson(this@MainActivity, "assistant_id") == "") {
                val assistants = openAi?.assistants()

                if (!assistants.isNullOrEmpty()) {
                    Log.d(TAG, "Assistant id is exist")
                    Log.d(TAG, "Assistant id: ${assistants!!.first().id}")
                    Log.d(TAG, "Assistant model: ${assistants!!.first().model}")
                    Log.d(TAG, "Assistant instruction: ${assistants!!.first().instructions}")
                    Log.d(TAG, "Assistant files: ${assistants!!.first().fileIds.joinToString(", ")}")

                    writeIdToJson(this@MainActivity, "assistant_id", assistants!!.first().id.id)
                    writeIdToJson(this@MainActivity, "file_id", assistants!!.first().fileIds[0].id)

                    chatGptInstruction = assistants!!.first().instructions!!
                }
                else {
                    Log.d(TAG, "Assistant is Empty. create new assistant")

                    val tmpContents = """
                        ユーザ情報: Aさん
                        筋トレメニュー: 腕立て、腹筋
                        好きな食べ物: 鳥刺し
                    """.trimIndent()

                    val result = createRagFile(this@MainActivity, tmpContents)

                    val filePath = this@MainActivity.filesDir.absolutePath + "/rag.json"

                    val file = FileUpload(
                        file = FileSource(filePath.toPath(), FileSystem.SYSTEM),
                        purpose = Purpose("assistants")
                    )

                    val knowledgeBase = openAi!!.file(request = file)

                    writeIdToJson(this@MainActivity, "file_id", knowledgeBase.id.id)

                    chatGptInstruction = "あなたは、ユーザに寄り添い会話を盛り上げることができるAIであり、ユーザとのやり取りはできるだけ短く簡潔に返信してください。"

                    val assistant = openAi?.assistant(
                        request = AssistantRequest(
                            name = "Assistant Test",
                            instructions = chatGptInstruction,
                            tools = listOf(AssistantTool.RetrievalTool),
                            model = ModelId("gpt-3.5-turbo"),
                            fileIds = listOf(FileId(readIdFromJson(this@MainActivity, "file_id")))
                        )
                    )

                    writeIdToJson(this@MainActivity, "assistant_id", assistant!!.id.id)
                }
            }
            else {
                Log.d(TAG, "Assistant id: ${readIdFromJson(this@MainActivity, "assistant_id")}")
            }

            if(readIdFromJson(this@MainActivity, "thread_id") == "") {
                val thread = openAi!!.thread()

                writeIdToJson(this@MainActivity, "thread_id", thread.id.id)
            }
        }
    }
}