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
import com.google.android.material.chip.Chip

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

        val commonConditions = arrayOf(
            "Asthma", "COPD", "Type 1 Diabetes", "Type 2 Diabetes",
            "Hypertension", "Migraine", "Multiple Sclerosis", "Epilepsy",
            "Rheumatoid Arthritis", "Lupus", "Celiac Disease", "Crohn's Disease",
            "Anxiety Disorder", "Depression", "Fibromyalgia", "Chronic Fatigue Syndrome",
            "Endometriosis", "POTS", "Ehlers-Danlos Syndrome", "PMDD", "TMJ-D"
        )

        // autocomplete adapter
        val issuesAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, commonConditions)
        binding.atvIllness.setAdapter(issuesAdapter)

        // observe current illnesses to build Chips
        viewModel.chronicIllnesses.observe(viewLifecycleOwner) { illnesses ->
            binding.cgIllnesses.removeAllViews()
            illnesses.forEach { illness ->
                val chip = Chip(requireContext()).apply {
                    text = illness
                    isCloseIconVisible = true
                    setOnCloseIconClickListener { viewModel.removeChronicIllness(illness) }
                }
                binding.cgIllnesses.addView((chip))
            }
        }

        binding.btnAddIllness.setOnClickListener {
            val text = binding.atvIllness.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.addChronicIllness(text) // adds to Firestore/State
                binding.atvIllness.setText("") // clear for next entry
            }
        }

        // setup sex options adapter
        val sexOptions = listOf("Female", "Male", "Non-binary", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sexOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSex.adapter = adapter

        binding.btnSubmit.setOnClickListener {
            val age = binding.etAge.text.toString()
            val sex = binding.spinnerSex.selectedItem.toString()

            val currentIllnesses = viewModel.chronicIllnesses.value ?: emptyList()

            if (age.isEmpty() || age.toInt() < 13 || age.toInt()>120) {
                binding.etAge.error = "Invalid Age"
                return@setOnClickListener
            }

            viewModel.saveUserProfile(age.toInt(), sex, currentIllnesses)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}