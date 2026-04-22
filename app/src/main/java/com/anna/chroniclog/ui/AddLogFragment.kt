// AddLogFragment - screen where user can add a log
package com.anna.chroniclog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
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
        /*symptomAdapter = SymptomAdapter(symptoms) { position ->
            symptoms.removeAt(position)
            symptomAdapter.notifyItemRemoved(position)
        } */
        symptomAdapter = SymptomAdapter(symptoms) { symptom ->
            viewModel.removeTempSymptom(symptom.id)
        }
        binding.rvSymptoms.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSymptoms.adapter = symptomAdapter

        binding.btnAddSymptom.setOnClickListener {
            findNavController().navigate(AddLogFragmentDirections.actionAddLogFragmentToAddSymptomFragment())
        }

        // setup remediation RecyclerView
        /*
        remediationAdapter = RemediationAdapter(remediations) { position ->
            remediations.removeAt(position)
            remediationAdapter.notifyItemRemoved(position)
        } */
        remediationAdapter = RemediationAdapter(remediations) { remediation ->
            viewModel.removeTempRemediation(remediation.id)
        }
        binding.rvRemediations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRemediations.adapter = remediationAdapter

        binding.btnAddRemediation.setOnClickListener {
            findNavController().navigate(AddLogFragmentDirections.actionAddLogFragmentToAddRemediationFragment())
        }

        binding.rgMood.setOnCheckedChangeListener { group, checkedId ->
            // loop through all buttons to reset their alpha/transparency
            for (i in 0 until group.childCount) {
                val rb = group.getChildAt(i) as RadioButton
                rb.alpha = if (rb.id == checkedId) 1.0f else 0.5f
            }
        }

        binding.btnCancel.setOnClickListener {
            // close add log view, go back to previous screen
            viewModel.clearTempData()
            findNavController().popBackStack()
        }

        binding.btnAdd.setOnClickListener {
            saveLog()
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
        // new log data
        val datePicker = binding.datePicker
        val year = datePicker.year
        val month = datePicker.month + 1
        val day = datePicker.dayOfMonth
        val dateStr = "$month/$day/$year"

        val moodId = binding.rgMood.checkedRadioButtonId
        val mood = when (moodId) {
            binding.mood1.id -> "☹\uFE0F" //"Terrible"
            binding.mood2.id -> "\uD83D\uDE41"  //"Bad"
            binding.mood3.id -> "\uD83D\uDE10" //"Meh"
            binding.mood4.id -> "\uD83D\uDE42" //""Good"
            binding.mood5.id -> "\uD83D\uDE00" //"Amazing"
            else -> ""
        }
        val notes = binding.etEditNotes.text.toString().trim()

        if (mood.isEmpty()) {
            Toast.makeText(requireContext(), "Please rate your day", Toast.LENGTH_SHORT).show()
            return
        }

        // save to fire store
        val newLog = LogEntry(
            date = dateStr,
            symptoms = symptoms,
            remediations = remediations,
            sentiment = mood,
            notes = notes
        )

        // map symptoms and remediations to include this specific log's ID
        val finalSymptoms = symptoms.map { it.copy(logId = newLog.id) }
        val finalRemediations = remediations.map { it.copy(logId = newLog.id) }

        // create the complete log object
        val completeLog = newLog.copy(
            symptoms = finalSymptoms,
            remediations = finalRemediations
        )

        // adds to _symptoms LiveData
        finalSymptoms.forEach { symptom ->
            viewModel.addSymptom(symptom)
        }
        // adds to _symptoms LiveData
        finalRemediations.forEach { remediation ->
            viewModel.addRemediation(remediation)
        }

        viewModel.addLog(completeLog) // saves to firestore
        viewModel.clearTempData()
        // nav to logs
        findNavController().navigate(AddLogFragmentDirections.actionAddLogFragmentToLogsFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}