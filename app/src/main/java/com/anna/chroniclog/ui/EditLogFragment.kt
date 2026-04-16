package com.anna.chroniclog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.anna.chroniclog.R
import com.anna.chroniclog.adapter.RemediationAdapter
import com.anna.chroniclog.adapter.SymptomAdapter
import com.google.firebase.auth.FirebaseAuth
import com.anna.chroniclog.databinding.FragmentEditLogBinding
import com.anna.chroniclog.model.Symptom
import com.anna.chroniclog.model.Remediation

class EditLogFragment : Fragment() {
    private var _binding: FragmentEditLogBinding? = null
    private val binding get() = _binding!!

    private var symptoms = mutableListOf<Symptom>()
    private var remediations = mutableListOf<Remediation>()

    private lateinit var symptomAdapter: SymptomAdapter
    private lateinit var remediationAdapter: RemediationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditLogBinding.inflate(inflater, container, false)
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

        // set binding.datePicker time to be current time
        //binding.datePicker.setOnDateChangedListener { picker, i, i1, i2 ->  }

        binding.btnAddSymptom.setOnClickListener {
            //showAddSymptomDialog()
        }

        binding.rgMood.setOnCheckedChangeListener { group, i ->
            val radioButton = binding.rgMood.findViewById<RadioButton>(i)
        }

        binding.btnCancel.setOnClickListener {
            // close add log view, go back to previous screen
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSave.setOnClickListener {
            //update log with new info
        }

        fun saveLog() {

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

            /*
            if (mood.isEmpty()) {
                android.widget.TextView.Toast.makeText(
                    requireContext(),
                    "Please rate your day",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return
            } */

            // save to fire store
            //val log = LogEntry(date = dateStr, sentiment = mood, notes = notes, symptoms = symptoms, remediations = remediations)
            //viewModel.saveLog(log)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

