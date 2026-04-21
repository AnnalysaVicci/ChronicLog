// HealthInformationFragment
package com.anna.chroniclog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.anna.chroniclog.MainViewModel
import com.anna.chroniclog.databinding.FragmentEditHealthInformationBinding
import com.google.android.material.chip.Chip


class EditHealthInformationFragment : Fragment() {
    private var _binding: FragmentEditHealthInformationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditHealthInformationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val commonConditions = arrayOf(
            "Asthma", "COPD", "Type 1 Diabetes", "Type 2 Diabetes",
            "Hypertension", "Migraine", "Multiple Sclerosis", "Epilepsy",
            "Rheumatoid Arthritis", "Lupus", "Celiac Disease", "Crohn's Disease",
            "Anxiety Disorder", "Depression", "Fibromyalgia", "Chronic Fatigue Syndrome",
            "Endometriosis", "POTS", "Ehlers-Danlos Syndrome"
        )

        // autocomplete adapter
        val issuesAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, commonConditions)
        binding.atvIllness.setAdapter(issuesAdapter)

        // observe current illnesses to build Chips
        viewModel.chronicIllnesses.observe(viewLifecycleOwner) { illnesses ->
            binding.cgIllnesses.removeAllViews()
            illnesses.forEach { illness ->
                addIllnessChip(illness)
            }
        }
        binding.btnAddIllness.setOnClickListener {
            val text = binding.atvIllness.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.addChronicIllness(text) // adds to Firestore/State
                binding.atvIllness.setText("") // clear for next entry
            }
        }


        val sexOptions = listOf("Female", "Male", "Non-binary", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sexOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSex.adapter = adapter

        viewModel.userAge.value?.let { age ->
            binding.etAge.setText(age.toString())
        }

        viewModel.userSex.value?.let { sex ->
            val index = sexOptions.indexOf(sex)
            if (index >= 0) binding.spinnerSex.setSelection(index)
        }

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSave.setOnClickListener {
            saveHealthInfo()
            findNavController().popBackStack()
        }
    }

    private fun addIllnessChip(text: String) {
        val chip = Chip(requireContext()).apply {
            this.text = text
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                viewModel.removeChronicIllness(text)
            }
        }
        binding.cgIllnesses.addView(chip)
    }

    private fun saveHealthInfo() {
        val ageStr = binding.etAge.text.toString().trim()
        val sex = binding.spinnerSex.selectedItem.toString()

        if (ageStr.isEmpty()) {
            binding.etAge.error = "Please enter your age"
            return
        }
        val age = ageStr.toIntOrNull()
        if (age == null || age <= 0 || age > 120) {
            binding.etAge.error = "Please enter a valid age"
            return
        }

        viewModel.saveUserProfile(age, sex)
        Toast.makeText(requireContext(), "Health info saved", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}