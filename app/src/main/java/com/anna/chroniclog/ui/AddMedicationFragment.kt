package com.anna.chroniclog.ui

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.anna.chroniclog.databinding.FragmentAddMedicationBinding
import com.anna.chroniclog.model.Medication
import com.anna.chroniclog.MainViewModel
import kotlin.getValue
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.anna.chroniclog.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class AddMedicationFragment : Fragment() {
    // trying to make drug suggestion faster
    private var _binding: FragmentAddMedicationBinding? = null
    private val binding get() = _binding!!
    private var searchJob: Job? = null
    private var selectedStartDate: Calendar = Calendar.getInstance()
    private var selectedEndDate: Calendar? = null
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddMedicationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setup Dosage units and adapter
        val units = listOf("mg", "gram", "mL", "ml")
        //val unitAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, units)
        //unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        //binding.spinnerDosageUnit.adapter = unitAdapter
        val unitAdapter = ArrayAdapter(requireContext(), R.layout.list_item, units)
        (binding.actvUnit as? AutoCompleteTextView)?.setAdapter(unitAdapter)

        // setup Frequency options and adapter
        //val frequencies = listOf("day(s)", "hour(s)", "month(s)", "week(s)", "as needed")
        val frequencies = listOf("1x", "2x", "3x", "4x", "5x", "6x", "7x", "8x", "9x", "10x")
        //val freqAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, frequencies)
        //freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        //binding.spinnerFreq.adapter = freqAdapter
        val frequencyAdapter = ArrayAdapter(requireContext(), R.layout.list_item, frequencies)
        (binding.actvFrequency as? AutoCompleteTextView)?.setAdapter(frequencyAdapter)

        // setup interval options and adapter
        val intervals = listOf("Daily", "Weekly", "Monthly", "As Needed")
        val intervalAdapter = ArrayAdapter(requireContext(), R.layout.list_item, intervals)
        (binding.actvInterval as? AutoCompleteTextView)?.setAdapter(intervalAdapter)

        // setup adapter for med autocomplete
        val drugAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        binding.medNameInput.setAdapter(drugAdapter)
        binding.medNameInput.threshold = 2

        // setup start date
        binding.btnStartDate.setOnClickListener {
            showDatePicker { calendar ->
                selectedStartDate = calendar
                binding.btnStartDate.text = formatDate(calendar)
            }
        }

        // setup end date
        binding.btnEndDate.setOnClickListener {
            showDatePicker { calendar ->
                selectedEndDate = calendar
                binding.btnEndDate.text = formatDate(calendar)
            }
        }

        // handle openfda med auto complete
        binding.medNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""
                if (query.length >= 2) {
                    searchJob?.cancel()
                    searchJob = viewLifecycleOwner.lifecycleScope.launch {
                        delay(300)
                        viewModel.searchDrugs(query)
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        // observe drug search results
        viewModel.drugSuggestions.observe(viewLifecycleOwner) { drugs ->
            drugAdapter.clear()
            drugAdapter.addAll(drugs.map {it.getDisplayName() })
            drugAdapter.notifyDataSetChanged()
            if (binding.medNameInput.text.isEmpty()) {
                binding.medNameInput.showDropDown()
            }
        }

        // end date picker vanishes if curr taking box is checked
        binding.checkboxCurrentlyTaking.setOnCheckedChangeListener { _, isChecked ->
            binding.tvEndDate.visibility = if (isChecked) View.GONE else View.VISIBLE
            //binding.endMedDatePicker.visibility = if (isChecked) View.GONE else View.VISIBLE
            binding.btnEndDate.visibility = if (isChecked) View.GONE else View.VISIBLE
        }

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnAdd.setOnClickListener {
            saveMedication()
        }
    }

    private fun showDatePicker(onDateSelected: (Calendar) -> Unit) {
        val c = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            val result = Calendar.getInstance().apply { set(y, m, d) }
            onDateSelected(result)
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun formatDate(date: Calendar): String {
        val month = date.get(Calendar.MONTH) + 1
        val day = date.get(Calendar.DAY_OF_MONTH)
        val year = date.get(Calendar.YEAR)
        return "$month/$day/$year"
    }

    fun saveMedication() {
        val medname = binding.medNameInput.text.toString().trim()

        val doseAmount = binding.etDosageNum.text.toString()
        //val doseUnit = binding.spinnerDosageUnit.selectedItem.toString()
        val doseUnit = binding.actvUnit.text.toString()
        val dosage = "$doseAmount$doseUnit"

        val frequency = binding.actvFrequency.text.toString()
        val interval = binding.actvInterval.text.toString()
        val freqStr = " $frequency$interval"

        val adherenceId = binding.rgAdherence.checkedRadioButtonId
        val adherence = when (adherenceId) {
            binding.neverDoseBut.id -> "Never miss a dose"
            binding.sometimesDoseBut.id -> "Sometimes miss a dose"
            binding.oftenDoseBut.id -> "Often miss doses"
            else -> ""
        }

        //val startDatePicker = binding.startMedDatePicker
        //val startDateStr = "${startDatePicker.month + 1}/${startDatePicker.dayOfMonth}/${startDatePicker.year}"
        val startDateStr = binding.btnStartDate.text.toString()

        val currentlyTaking = binding.checkboxCurrentlyTaking.isChecked

        //val endDateStr = if (currentlyTaking) "" else {
            //val endDatePicker = binding.endMedDatePicker
            //"${endDatePicker.month + 1}/${endDatePicker.dayOfMonth}/${endDatePicker.year}"
        //}
        val endDateStr = binding.btnEndDate.text.toString()

        if (medname.isEmpty()) {
            android.widget.Toast.makeText(
                requireContext(), "Please enter a medication name", android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }

        // save to fire store
        val med = Medication(
            name = medname,
            dosage = dosage,
            frequency = freqStr,
            adherence = adherence,
            startDate = startDateStr,
            endDate = endDateStr,
            currentlyTaking = currentlyTaking
            )
        viewModel.addMedication(med)

        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}