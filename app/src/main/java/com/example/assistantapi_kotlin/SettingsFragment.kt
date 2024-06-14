package com.example.assistantapi_kotlin

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.assistant.AssistantId
import com.example.assistantapi_kotlin.databinding.FragmentSettingsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SettingsFragment : Fragment(),
    BottomSheetCreateAssistantFragment.OnBottomSheetCreateAssistantCloseListener {
    private var param1: String? = null
    private var param2: String? = null

    private val TAG = "SettingsFragment"

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private var mainActivity: MainActivity? = null

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
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity = activity as MainActivity?

        binding.assistantInstructionTextView.setMovementMethod(ScrollingMovementMethod())

        initDisplay()

        getAllAssistants()

        binding.assistantGetAssistantsButton.setOnClickListener {
            val bottomSheet = BottomSheetShowAssistantsFragment()

            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }

        binding.assistantCreateNewAssistantButton.setOnClickListener {
            val bottomSheet = BottomSheetCreateAssistantFragment()

            bottomSheet.setTargetFragment(this, 0)

            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }
    }

    @OptIn(BetaOpenAI::class)
    private fun getAllAssistants() {
        assistantsList = listOf()

        CoroutineScope(Dispatchers.IO).launch {
            val assistants = mainActivity?.openAi!!.assistants()

            for (assistant in assistants) {
                var isEqual = false

                val assistantId = assistant.id.id
                Log.d(TAG, "assistant id: $assistantId")

                if(assistantId == readIdFromJson(requireContext(), "assistant_id")) {
                    isEqual = true
                }

                val assistantName = assistant.name
                Log.d(TAG, "assistant name: $assistantName")

                val assistantInstruction = assistant.instructions
                Log.d(TAG, "assistant instruction: $assistantInstruction")

                assistantsList += RecyclerItem(assistantName, assistantId, assistantInstruction!!, isEqual)
            }
        }
    }

    @OptIn(BetaOpenAI::class)
    private fun initDisplay() {
        CoroutineScope(Dispatchers.IO).launch {
            val assistant = mainActivity?.openAi!!.assistant(AssistantId(readIdFromJson(requireContext(), "assistant_id")))

            withContext(Dispatchers.Main) {
                binding.assistantNameTextView.text = assistant!!.name

                binding.assistantIdTextView.text = assistant!!.id.id

                binding.assistantInstructionTextView.text = assistant!!.instructions
            }
        }
    }

    override fun onBottomSheetCreateAssistantClose(assistantName: String, assistantId: String, assistantInstruction: String) {
        Log.d(TAG, "callback")
        updateAssistantDisplayInfo(assistantName, assistantId, assistantInstruction)
    }

    private fun updateAssistantDisplayInfo(assistantName: String, assistantId: String, assistantInstruction: String) {
        binding.assistantNameTextView.text = assistantName

        binding.assistantIdTextView.text = assistantId

        binding.assistantInstructionTextView.text = assistantInstruction
    }

    companion object {

        var assistantsList = listOf<RecyclerItem>()

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}