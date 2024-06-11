package com.example.assistantapi_kotlin

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.assistant.AssistantId
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.core.Role
import com.aallam.openai.api.core.Status
import com.aallam.openai.api.message.MessageContent
import com.aallam.openai.api.message.MessageRequest
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.api.run.RunRequest
import com.aallam.openai.api.thread.ThreadId
import com.example.assistantapi_kotlin.databinding.FragmentChatBinding
import com.example.assistantapi_kotlin.databinding.FragmentSettingsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ChatFragment : Fragment() {
    private val TAG = "ChatFragment"

    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private var mainActivity: MainActivity? = null

    private var messageId = 0

    private lateinit var mMessageRecycler: RecyclerView
    private lateinit var mMessageAdapter: MessageListAdapter

    private var messageList: ArrayList<Message> = arrayListOf()

    private val chatGpt = User("Chat-GPT", "Chat-GPT")
    private val user = User("User", "User")

    private val handler: Handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        mMessageRecycler = binding.recyclerChat

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("messageList", messageList)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity = activity as MainActivity?

        if(savedInstanceState != null) {
            messageList = savedInstanceState.getParcelableArrayList("messageList") ?: arrayListOf()
            displayMessageList()
        }
        else {
            initMessage()
        }

        binding.chatgptChatSendImageview.setOnClickListener {
            if(binding.chatgptChatMessageEdittext.text.toString() != "") {

                hideKeyboard()

                messageList.add(Message(messageId.toString(), binding.chatgptChatMessageEdittext.text.toString(), user, getTime()))

                binding.chatgptChatMessageEdittext.setText("")

                messageList.add(Message(messageId.toString(), "...", chatGpt, getTime()))

                displayMessageList()

                startChat(binding.chatgptChatMessageEdittext.text.toString())
            }
        }

        binding.recyclerChat.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard()
            }
            false
        }
    }

    @OptIn(BetaOpenAI::class)
    private fun startChat(userMessage: String) {
        CoroutineScope(Dispatchers.IO).launch {
            mainActivity?.openAi!!.message(
                threadId = ThreadId(readIdFromJson(requireContext(), "thread_id")), request = MessageRequest(
                    role = Role.User,
                    content = userMessage
                )
            )

            val run = mainActivity?.openAi!!.createRun(
                ThreadId(readIdFromJson(requireContext(), "thread_id")), request = RunRequest(
                    assistantId = AssistantId(readIdFromJson(requireContext(), "assistant_id")),
                    instructions = mainActivity?.chatGptInstruction
                )
            )

            do {
                delay(200)
                val retrievedRun = mainActivity?.openAi!!.getRun(ThreadId(readIdFromJson(requireContext(), "thread_id")), run.id)
            } while(retrievedRun.status != Status.Completed)

            val runStep = mainActivity?.openAi!!.runSteps(run.threadId, run.id)
            Log.d(TAG, "runStep: $runStep")

            val assistantMessages = mainActivity?.openAi!!.messages(ThreadId(readIdFromJson(requireContext(), "thread_id")))

            val chatGptResponseText = assistantMessages.first().content.first() as? MessageContent.Text

            Log.d(TAG, chatGptResponseText?.text?.value.toString())

            removeLastMessage()

            messageList.add(Message(messageId.toString(), chatGptResponseText?.text?.value.toString(), chatGpt, getTime()))

            displayMessageList()
        }
    }

    private fun displayMessageList() {
        ++messageId

        handler.post {
            mMessageAdapter = MessageListAdapter(requireContext(), messageList)

            mMessageRecycler.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = mMessageAdapter
            }
        }
    }

    private fun initMessage() {
        messageList.add(Message(messageId.toString(), "何かお困りごとはありますか？", chatGpt, getTime()))

        displayMessageList()
    }

    private fun removeLastMessage() {
        messageList.removeLast()
    }

    private fun getTime(): String {
        val currentDateTime = LocalDateTime.now()

        val hour = currentDateTime.hour
        val minute = currentDateTime.minute

        return "$hour:$minute"
    }

    private fun hideKeyboard(){
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.chatgptChatMessageEdittext.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}