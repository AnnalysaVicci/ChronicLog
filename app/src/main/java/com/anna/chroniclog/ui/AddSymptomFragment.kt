package com.anna.chroniclog.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.anna.chroniclog.MainViewModel
import com.anna.chroniclog.R
import com.anna.chroniclog.databinding.FragmentAddSymptomBinding
import com.anna.chroniclog.model.Symptom

class AddSymptomFragment : Fragment(R.layout.fragment_add_symptom) {
    private var _binding: FragmentAddSymptomBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddSymptomBinding.bind(view)

        // list of chronic illness symptoms
        val commonSymptoms = arrayOf(
            "Abdominal Pain", "Back Pain", "Brain Fog", "Chest Pain",
            "Dizziness", "Fatigue", "Headache", "Insomnia",
            "Joint Pain", "Muscle Weakness", "Nausea", "Shortness of Breath"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            commonSymptoms
        )

        // Link the adapter to the AutoCompleteTextView
        binding.atvSymptom.setAdapter(adapter)

        // Set threshold to 1 so suggestions appear after typing one letter
        binding.atvSymptom.threshold = 1

        // setup SeekBar listener
        binding.seekbarSeverity.max = 10

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnAdd.setOnClickListener {
            saveSymptom()
            parentFragmentManager.popBackStack()
        }
    }

    private fun saveSymptom() {
        val symptomName = binding.atvSymptom.text.toString().trim()
        if (symptomName.isEmpty()) {
            binding.atvSymptom.error = "Please enter or select a symptom"
            return
        }
        val symptom = Symptom(
            name = symptomName,
            severity = binding.seekbarSeverity.progress
        )
        // symptoms before the final Log is saved
        viewModel.addTempSymptom(symptom)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}