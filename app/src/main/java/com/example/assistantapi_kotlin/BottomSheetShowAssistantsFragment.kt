package com.example.assistantapi_kotlin

import android.app.Dialog
import android.graphics.Insets
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowMetrics
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.assistantapi_kotlin.databinding.FragmentBottomSheetShowAssistantsBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class BottomSheetShowAssistantsFragment : BottomSheetDialogFragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentBottomSheetShowAssistantsBinding? = null
    private val binding get() = _binding!!

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.FullScreenBottomSheetDialog)
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

        val adapter = RecyclerAdapter(SettingsFragment.assistantsList)

        binding.assistantListRecyclerview.adapter = adapter

        binding.assistantListRecyclerview.layoutManager = LinearLayoutManager(requireContext())

        binding.assistantListRecyclerview.setHasFixedSize(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBottomSheetShowAssistantsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.assistantListCancelTextview.setOnClickListener {
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

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BottomSheetShowAssistantsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}