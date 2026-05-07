// AddLogFragment - screen where user can add a log
package com.anna.chroniclog.ui

import android.app.DatePickerDialog
import android.icu.util.Calendar
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
import kotlin.math.log

class AddLogFragment : Fragment() {
    private var _binding: FragmentAddLogBinding? = null
    private val binding get() = _binding!!
    private var symptoms = mutableListOf<Symptom>()
    private var remediations = mutableListOf<Remediation>()
    private lateinit var symptomAdapter: SymptomAdapter
    private lateinit var remediationAdapter: RemediationAdapter
    private var selectedLogDate: Calendar = Calendar.getInstance()
    private val viewModel: MainViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // set initial date button text to today's date
        binding.btnDate.text = formatDate(selectedLogDate)

        // setup log date
        binding.btnDate.setOnClickListener {
            showDatePicker { calendar ->
                selectedLogDate = calendar
                binding.btnDate.text = formatDate(calendar)
            }
        }

        // setup symptom adapter and rv
        symptomAdapter = SymptomAdapter(symptoms) { symptom ->
            viewModel.removeTempSymptom(symptom.id)
        }
        binding.rvSymptoms.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSymptoms.adapter = symptomAdapter
        binding.rvSymptoms.isNestedScrollingEnabled = false

        binding.btnAddSymptom.setOnClickListener {
            findNavController().navigate(AddLogFragmentDirections.actionAddLogFragmentToAddSymptomFragment())
        }

        // setup remediation adapter and RecyclerView
        remediationAdapter = RemediationAdapter(remediations) { remediation ->
            viewModel.removeTempRemediation(remediation.id)
        }
        binding.rvRemediations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRemediations.adapter = remediationAdapter
        binding.rvRemediations.isNestedScrollingEnabled = false

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
            //symptoms.clear()
            //symptoms.addAll(updatedList)
            symptomAdapter.updateSymptoms(updatedList)
        }

        // observe remediations from ViewModel
        viewModel.tempRemediations.observe(viewLifecycleOwner) { updatedList ->
            //remediations.clear()
            //remediations.addAll(updatedList)
            remediationAdapter.updateRemediations(updatedList)
        }
    }

    private fun showDatePicker(onDateSelected: (Calendar) -> Unit) {
        val c = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                val result = Calendar.getInstance().apply { set(y, m, d) }
                onDateSelected(result)
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        )
        // Prevent picking future dates
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun formatDate(date: Calendar): String {
        val month = date.get(Calendar.MONTH) + 1
        val day = date.get(Calendar.DAY_OF_MONTH)
        val year = date.get(Calendar.YEAR)
        return "$month/$day/$year"
    }

    fun saveLog() {
        // new log data
        //val datePicker = binding.datePicker
        //val calendar = java.util.Calendar.getInstance()
        //calendar.set(datePicker.year, datePicker.month, datePicker.dayOfMonth, 0, 0, 0)
        //val logTimestamp = calendar.timeInMillis
        //val year = datePicker.year
        //val month = datePicker.month + 1
        //val day = datePicker.dayOfMonth
        //val dateStr = "$month/$day/$year"

        val dateStr = formatDate(selectedLogDate)
        val logTimestamp = selectedLogDate.timeInMillis

        // check if log already exists for this day
        val existingLogs = viewModel.logs.value ?: emptyList()
        val isDuplicate = existingLogs.any { it.date == dateStr }

        if (isDuplicate) {
            Toast.makeText(requireContext(), "A log already exists for $dateStr", Toast.LENGTH_SHORT).show()
        }

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

        val notes = binding.etEditNotes.text.toString().trim()

        // Create log entry using temp data from ViewModel
        //val currentSymptoms = viewModel.tempSymptoms.value ?: emptyList()
        //val currentRemediations = viewModel.tempRemediations.value ?: emptyList()

        val newLog = LogEntry(
            date = dateStr,
            timestamp = logTimestamp,
            symptoms = symptoms,
            //symptoms = currentSymptoms.map { it.copy(timestamp = logTimestamp) },
            remediations = remediations,
            //remediations = currentRemediations.map { it.copy(timestamp = logTimestamp) },
            sentiment = mood,
            notes = notes
        )

        //viewModel.addLog(newLog)
        //viewModel.clearTempData()
        //findNavController().popBackStack()

        // map symptoms and remediations to include this specific log's ID
        val finalSymptoms = (viewModel.tempSymptoms.value ?: emptyList()).map {
            it.copy(
                name = it.name.trim().uppercase(),
                logId = newLog.id,
                timestamp = logTimestamp
            )
        }
        val finalRemediations = (viewModel.tempRemediations.value ?: emptyList()).map {
            it.copy(
                logId = newLog.id,
                timestamp = logTimestamp
            )
        }

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

        // adds to _log LiveData
        viewModel.addLog(completeLog) // saves to firestore
        viewModel.clearTempData()

        // nav to logs
        // I think I can just do popbackstack() now
        findNavController().navigate(AddLogFragmentDirections.actionAddLogFragmentToLogsFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}