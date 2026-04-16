package com.anna.chroniclog.ui

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



class AddMedicationFragment : Fragment() {
    private var _binding: FragmentAddMedicationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddMedicationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // handle openfda med auto complete
        val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        binding.medNameInput.setAdapter(adapter)
        binding.medNameInput.threshold = 3

        binding.medNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""
                if (query.length >= 2) {
                    viewModel.searchDrugs(query)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })



        // end date picker vanishes if curr taking box is checked
        binding.checkboxCurrentlyTaking.setOnCheckedChangeListener { _, isChecked ->
            binding.tvEndDate.visibility = if (isChecked) View.GONE else View.VISIBLE
            binding.endMedDatePicker.visibility = if (isChecked) View.GONE else View.VISIBLE
        }

        binding.btnCancel.setOnClickListener {
            // close add med view, go back to previous screen
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnAdd.setOnClickListener {
            saveMedication()
        }
    }

    fun saveMedication() {
        val medname = binding.medNameInput.text.toString().trim()
        val dosage = binding.etDosage.text.toString().trim()
        val frequency = binding.etFreq.text.toString().trim()

        val adherenceId = binding.rgAdherence.checkedRadioButtonId
        val adherence = when (adherenceId) {
            binding.neverDoseBut.id -> "Never miss a dose"
            binding.sometimesDoseBut.id -> "Sometimes miss a dose"
            binding.oftenDoseBut.id -> "Often miss doses"
            else -> ""
        }

        val startDatePicker = binding.startMedDatePicker
        val startDateStr = "${startDatePicker.month + 1}/${startDatePicker.dayOfMonth}/${startDatePicker.year}"

        val currentlyTaking = binding.checkboxCurrentlyTaking.isChecked

        val endDateStr = if (currentlyTaking) "" else {
            val endDatePicker = binding.endMedDatePicker
            "${endDatePicker.month + 1}/${endDatePicker.dayOfMonth}/${endDatePicker.year}"
        }

        if (medname.isEmpty()) {
            android.widget.Toast.makeText(
                requireContext(), "Please enter a medication name", android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        requireActivity().onBackPressedDispatcher.onBackPressed()

        // save to fire store
        val med = Medication(
            name = medname,
            dosage = dosage,
            frequency = frequency,
            adherence = adherence,
            startDate = startDateStr,
            endDate = endDateStr,
            currentlyTaking = currentlyTaking
            )
        viewModel.addMedication(med)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}