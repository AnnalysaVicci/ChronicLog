package com.anna.chroniclog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.anna.chroniclog.MainViewModel
import com.anna.chroniclog.databinding.DialogOnboardingBinding

class OnboardingDialogFragment : DialogFragment() {

    private var _binding: DialogOnboardingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false // force them to fill it in

        val sexOptions = listOf("Female", "Male", "Non-binary", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sexOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSex.adapter = adapter

        binding.btnSubmit.setOnClickListener {
            val age = binding.etAge.text.toString()
            val sex = binding.spinnerSex.selectedItem.toString()

            if (age.isEmpty() || age.toInt() < 13 || age.toInt()>120 ) {
                binding.etAge.error = "Please enter a valid age"
                return@setOnClickListener
            }

            viewModel.saveUserProfile(age.toInt(), sex)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}