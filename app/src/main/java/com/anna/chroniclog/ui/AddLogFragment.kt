package com.anna.chroniclog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.anna.chroniclog.MainViewModel
import com.anna.chroniclog.adapter.RemediationAdapter
import com.anna.chroniclog.adapter.SymptomAdapter
import com.anna.chroniclog.databinding.FragmentAddLogBinding
import com.anna.chroniclog.model.LogEntry
import com.anna.chroniclog.model.Symptom
import com.anna.chroniclog.model.Remediation

class AddLogFragment : Fragment() {
    private var _binding: FragmentAddLogBinding? = null
    private val binding get() = _binding!!
    private var symptoms = mutableListOf<Symptom>()
    private var remediations = mutableListOf<Remediation>()

    private lateinit var symptomAdapter: SymptomAdapter
    private lateinit var remediationAdapter: RemediationAdapter
    private val viewModel: MainViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setup symptom RecyclerView
        symptomAdapter = SymptomAdapter(symptoms) { position ->
            symptoms.removeAt(position)
            symptomAdapter.notifyItemRemoved(position)
        }
        binding.rvSymptoms.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSymptoms.adapter = symptomAdapter

        // setup remediation RecyclerView
        remediationAdapter = RemediationAdapter(remediations) { position ->
            remediations.removeAt(position)
            remediationAdapter.notifyItemRemoved(position)
        }
        binding.rvRemediations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRemediations.adapter = remediationAdapter

        binding.rgMood.setOnCheckedChangeListener { group, i ->
            val radioButton = binding.rgMood.findViewById<RadioButton>(i)
        }

        binding.btnCancel.setOnClickListener {
            // close add log view, go back to previous screen
            requireActivity().onBackPressedDispatcher.onBackPressed()
            viewModel.clearTempData()
            parentFragmentManager.popBackStack()
        }

        binding.btnAdd.setOnClickListener {
            // leave add log view but save data
            // need to create log with associated date and sentiment for logs fragment
            // need to use symptoms and remediation data for trends
            // save log to firestore
            saveLog()
            viewModel.clearTempData()
            parentFragmentManager.popBackStack()
        }

        // observe symptoms from ViewModel
        viewModel.tempSymptoms.observe(viewLifecycleOwner) { updatedList ->
            symptoms.clear()
            symptoms.addAll(updatedList)
            symptomAdapter.updateSymptoms(updatedList)
        }

        // observe remediations from ViewModel
        viewModel.tempRemediations.observe(viewLifecycleOwner) { updatedList ->
            remediations.clear()
            remediations.addAll(updatedList)
            remediationAdapter.updateRemediations(updatedList)
        }
    }

    fun saveLog() {
        val datePicker = binding.datePicker
        val year = datePicker.year
        val month = datePicker.month + 1
        val day = datePicker.dayOfMonth
        val dateStr = "$month/$day/$year"

        val moodId = binding.rgMood.checkedRadioButtonId
        val mood = when (moodId) {
            binding.mood1.id -> "Terrible"
            binding.mood2.id -> "Bad"
            binding.mood3.id -> "Meh"
            binding.mood4.id -> "Good"
            binding.mood5.id -> "Amazing"
            else -> ""
        }
        val notes = binding.etEditNotes.text.toString().trim()

        if (mood.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Please rate your day",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }

        // save to fire store
        val log = LogEntry(
            date = dateStr,
            symptoms = symptoms,
            remediations = remediations,
            sentiment = mood,
            notes = notes
        )
        viewModel.addLog(log)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}