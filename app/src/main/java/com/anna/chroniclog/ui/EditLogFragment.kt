package com.anna.chroniclog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.anna.chroniclog.MainViewModel
import com.anna.chroniclog.adapter.RemediationAdapter
import com.anna.chroniclog.adapter.SymptomAdapter
import com.anna.chroniclog.databinding.FragmentEditLogBinding
import com.anna.chroniclog.model.Symptom
import com.anna.chroniclog.model.Remediation
import kotlin.getValue

class EditLogFragment : Fragment() {
    private var _binding: FragmentEditLogBinding? = null
    private val binding get() = _binding!!

    private var symptoms = mutableListOf<Symptom>()
    private var remediations = mutableListOf<Remediation>()

    private lateinit var symptomAdapter: SymptomAdapter
    private lateinit var remediationAdapter: RemediationAdapter

    private val removedSymptomIds = mutableListOf<String>()
    private val removedRemediationIds = mutableListOf<String>()

    private val args: LogFragmentArgs by navArgs()
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logId = args.logId

        // setup adapters
        symptomAdapter = SymptomAdapter(symptoms) { symptom ->
            removedSymptomIds.add(symptom.id)
            viewModel.removeTempSymptom(symptom.id)
        }
        binding.rvSymptoms.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSymptoms.adapter = symptomAdapter

        remediationAdapter = RemediationAdapter(remediations) { remediation ->
            removedRemediationIds.add(remediation.id)
            viewModel.removeTempRemediation(remediation.id)
        }
        binding.rvRemediations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRemediations.adapter = remediationAdapter

        // initialize temp data from current log
        if (viewModel.tempSymptoms.value.isNullOrEmpty() &&
            viewModel.tempRemediations.value.isNullOrEmpty()) {
            val currentLog = viewModel.logs.value?.find { it.id == logId }
            currentLog?.let { log ->
                val logSymptoms = viewModel.symptoms.value?.filter { it.logId == logId } ?: emptyList()
                val logRemediations = viewModel.remediations.value?.filter { it.logId == logId } ?: emptyList()
                logSymptoms.forEach { viewModel.addTempSymptom(it) }
                logRemediations.forEach { viewModel.addTempRemediation(it) }
            }
        }


        // observe the logs from the ViewModel
        viewModel.logs.observe(viewLifecycleOwner) { logList ->
            // find the specific log that matches the ID passed in args
            val currentLog = logList.find { it.id == logId }

            currentLog?.let { log ->

                binding.tvDate.text = "Edit Log from ${log.date}"
                binding.etEditNotes.setText(log.notes)

                val rbId = when (log.sentiment) {
                    "☹\uFE0F" -> binding.mood1.id
                    "\uD83D\uDE41" -> binding.mood2.id
                    "\uD83D\uDE10" -> binding.mood3.id
                    "\uD83D\uDE42" -> binding.mood4.id
                    "\uD83D\uDE00" -> binding.mood5.id
                    else -> -1
                }
                if (rbId != -1) binding.rgMood.check(rbId)
            }
        }

        /*
        viewModel.symptoms.observe(viewLifecycleOwner) { allSymptoms ->
            // filter all symptoms to find only those belonging to THIS log
            val filteredSymptoms = allSymptoms.filter { it.logId == logId }
            symptomAdapter.updateSymptoms(filteredSymptoms)
        } */
        // observe temp data for RecyclerView
        viewModel.tempSymptoms.observe(viewLifecycleOwner) { tempSymptoms ->
            symptomAdapter.updateSymptoms(tempSymptoms)
        }

        viewModel.tempRemediations.observe(viewLifecycleOwner) { tempRemediations ->
            remediationAdapter.updateRemediations(tempRemediations)
        }

        // Add Symptom Button
        binding.btnAddSymptom.setOnClickListener {
            findNavController().navigate(EditLogFragmentDirections.actionEditLogFragmentToAddSymptomFragment())
        }

        // Add Remediation Button
        binding.btnAddRemediation.setOnClickListener {
            findNavController().navigate(EditLogFragmentDirections.actionEditLogFragmentToAddRemediationFragment())
        }

        // Rate Your Day Radio Group
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

        binding.btnSave.setOnClickListener {
            updateLog()
        }
    }

    fun updateLog() {
        val logId = args.logId
        val notes = binding.etEditNotes.text.toString().trim()

        val moodId = binding.rgMood.checkedRadioButtonId
        val mood = when (moodId) {
            binding.mood1.id -> "☹\uFE0F" //"Terrible"
            binding.mood2.id -> "\uD83D\uDE41"  //"Bad"
            binding.mood3.id -> "\uD83D\uDE10" //"Meh"
            binding.mood4.id -> "\uD83D\uDE42" //""Good"
            binding.mood5.id -> "\uD83D\uDE00" //"Amazing"
            else -> ""
        }

        if (mood.isEmpty()) {
            Toast.makeText(requireContext(), "Please rate your day", Toast.LENGTH_SHORT).show()
            return
        }

        //val updatedSymptoms = viewModel.tempSymptoms.value ?: emptyList()
        //val updatedRemediations = viewModel.tempRemediations.value ?: emptyList()
        val updatedSymptoms = (viewModel.tempSymptoms.value ?: emptyList()).map { symptom ->
            if (symptom.logId.isEmpty()) symptom.copy(logId = logId) else symptom
        }
        val updatedRemediations = (viewModel.tempRemediations.value ?: emptyList()).map { remediation ->
            if (remediation.logId.isEmpty()) remediation.copy(logId = logId) else remediation
        }

        val originalLog = viewModel.logs.value?.find { it.id == logId }

        val updatedLog = originalLog?.copy(
            sentiment = mood,
            notes = notes,
            symptoms = updatedSymptoms,
            remediations = updatedRemediations
        )

        if (updatedLog != null) {
            updatedSymptoms.forEach { symptom ->
                if (viewModel.symptoms.value?.none { it.id == symptom.id } == true) {
                    viewModel.addSymptom(symptom)
                }
            }
            updatedRemediations.forEach { remediation ->
                if (viewModel.remediations.value?.none { it.id == remediation.id } == true) {
                    viewModel.addRemediation(remediation)
                }
            }

            viewModel.updateLog(updatedLog, removedSymptomIds, removedRemediationIds)
            viewModel.clearTempData()
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

