// LogFragment - screen that shows details of a log that was clicked in LogsFragment screen
package com.anna.chroniclog.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.anna.chroniclog.adapter.RemediationAdapter
import com.anna.chroniclog.adapter.SymptomAdapter
import com.anna.chroniclog.databinding.FragmentLogBinding
import com.anna.chroniclog.model.Remediation
import com.anna.chroniclog.model.Symptom

class LogFragment : Fragment() {

    private var _binding: FragmentLogBinding? = null
    private val binding get() = _binding!!

    private val symptoms = mutableListOf<Symptom>()
    private val remediations = mutableListOf<Remediation>()

    private lateinit var symptomAdapter: SymptomAdapter
    private lateinit var remediationAdapter: RemediationAdapter

    private val args: LogFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logId = args.logId

        // TODO Week 3: fetch log from Firestore using logId
        // For now use placeholder data
        binding.tvDate.text = "April 12, 2026"
        binding.tvSentiment.text = "😊"
        binding.tvNotes.text = "Felt okay today, headache in the afternoon."

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

        // navigate to EditLogFragment
        binding.btnEditLog.setOnClickListener {
            val action = LogFragmentDirections
                .actionLogFragmentToEditLogFragment(logId)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}