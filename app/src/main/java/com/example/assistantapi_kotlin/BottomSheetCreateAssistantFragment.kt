package com.example.assistantapi_kotlin

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowMetrics
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.assistant.AssistantRequest
import com.aallam.openai.api.assistant.AssistantTool
import com.aallam.openai.api.file.FileId
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.file.FileUpload
import com.aallam.openai.api.file.Purpose
import com.aallam.openai.api.model.ModelId
import com.example.assistantapi_kotlin.databinding.FragmentBottomSheetCreateAssistantBinding
import com.example.assistantapi_kotlin.databinding.FragmentBottomSheetShowAssistantsBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toPath

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class BottomSheetCreateAssistantFragment : BottomSheetDialogFragment() {
    private var param1: String? = null
    private var param2: String? = null

    private val TAG = "BottomSheetCreateAssistantFragment"

    private var _binding: FragmentBottomSheetCreateAssistantBinding? = null
    private val binding get() = _binding!!

    private var mainActivity: MainActivity? = null

    private var listener: OnBottomSheetCreateAssistantCloseListener? = null

    interface OnBottomSheetCreateAssistantCloseListener {
        fun onBottomSheetCreateAssistantClose(assistantName: String, assistantId: String, assistantInstruction: String)
    }

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

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = targetFragment as? OnBottomSheetCreateAssistantCloseListener

            if(listener == null) {
                Log.d(TAG, "listener is null")
            }
        } catch (e: ClassCastException) {
            throw ClassCastException("$context Error")
        }
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog as? BottomSheetDialog

        dialog?.let {
            val bottomSheet = it.findViewById(com.google.android.material.R.id.design_bottom_sheet) as? FrameLayout
            bottomSheet?.let { sheet ->
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
                behavior.isHideable = true
                sheet.layoutParams.height = (getWindowSize("height") * 0.8).toInt()
            }
        }

        binding.createNewAssistantButton.setOnClickListener{
            createNewAssistant()
        }

        binding.assistantCreateLayout.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard()
            }
            false
        }
    }

    @OptIn(BetaOpenAI::class)
    private fun createNewAssistant(){
        if(binding.editAssistantName.text.toString().isEmpty() || binding.editAssistantInstruction.text.toString().isEmpty()) {
            Toast.makeText(requireContext(), "アシスタント名、インストラクション内容を入力してください", Toast.LENGTH_SHORT).show()

            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val assistant = mainActivity?.openAi!!.assistant(
                request = AssistantRequest(
                    name = binding.editAssistantName.text.toString(),
                    instructions = binding.editAssistantInstruction.text.toString(),
                    tools = listOf(AssistantTool.RetrievalTool),
                    model = ModelId("gpt-3.5-turbo"),
                    fileIds = listOf(FileId(readIdFromJson(requireContext(), "file_id")))
                )
            )

            writeIdToJson(requireContext(), "assistant_id", assistant!!.id.id)

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "アシスタントを作成しました", Toast.LENGTH_SHORT).show()

                listener?.onBottomSheetCreateAssistantClose(binding.editAssistantName.text.toString(), assistant!!.id.id, binding.editAssistantInstruction.text.toString())

                dismiss()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBottomSheetCreateAssistantBinding.inflate(inflater, container, false)

        mainActivity = activity as? MainActivity

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.assistantCreateCancelTextview.setOnClickListener {
            dismiss()
        }
    }

    private fun getWindowSize(type: String): Int{
        val windowMetrics: WindowMetrics = requireActivity().windowManager.currentWindowMetrics

        return when(type) {
            "width" -> windowMetrics.bounds.width()
            "height" -> windowMetrics.bounds.height()
            else -> 0
        }
    }

    private fun hideKeyboard(){
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.assistantCreateLayout.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BottomSheetCreateAssistantFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}