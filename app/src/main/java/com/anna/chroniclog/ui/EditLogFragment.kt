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

        // setup symptom RecyclerView
        /* symptomAdapter = SymptomAdapter(symptoms) { position ->
            symptoms.removeAt(position)
            symptomAdapter.notifyItemRemoved(position)
        } */
        symptomAdapter = SymptomAdapter(symptoms) { symptom ->
            viewModel.removeTempSymptom(symptom.id)
        }
        binding.rvSymptoms.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSymptoms.adapter = symptomAdapter

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


        // observe the logs from the ViewModel
        viewModel.logs.observe(viewLifecycleOwner) { logList ->
            // find the specific log that matches the ID passed in args
            val currentLog = logList.find { it.id == logId }

            currentLog?.let { log ->
                binding.tvDate.text = "Edit Log from ${log.date}"
                // load sentiment
            }
        }


        viewModel.symptoms.observe(viewLifecycleOwner) { allSymptoms ->
            // filter all symptoms to find only those belonging to THIS log
            val filteredSymptoms = allSymptoms.filter { it.logId == logId }
            symptomAdapter.updateSymptoms(filteredSymptoms)
        }

        // Add Symptom Button
        binding.btnAddSymptom.setOnClickListener {
            findNavController().navigate(AddLogFragmentDirections.actionAddLogFragmentToAddSymptomFragment())
        }

        viewModel.remediations.observe(viewLifecycleOwner) { allRemediations ->
            val filteredRemediations = allRemediations.filter { it.logId == logId }
            remediationAdapter.updateRemediations(filteredRemediations)
        }

        // Add Remediation Button
        binding.btnAddRemediation.setOnClickListener {
            findNavController().navigate(AddLogFragmentDirections.actionAddLogFragmentToAddRemediationFragment())
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

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

