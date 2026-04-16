// HealthInformationFragment
package com.anna.chroniclog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.anna.chroniclog.MainViewModel
import com.anna.chroniclog.adapter.MedicationsAdapter
import com.anna.chroniclog.adapter.SymptomAdapter
import com.anna.chroniclog.databinding.FragmentHealthInformationBinding
import com.anna.chroniclog.model.Medication
import com.anna.chroniclog.model.Symptom
import com.anna.chroniclog.model.Remediation


class HealthInformationFragment : Fragment() {
    private var _binding: FragmentHealthInformationBinding? = null
    private val binding get() = _binding!!

    private var symptoms = mutableListOf<Symptom>()
    private lateinit var symptomAdapter: SymptomAdapter
    private lateinit var currentMedicationsAdapter: MedicationsAdapter

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthInformationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // nav to edit health info frag
        binding.btnEditHealthInfo.setOnClickListener {
            findNavController().navigate(HealthInformationFragmentDirections.actionHealthInformationFragmentToEditHealthInformationFragment())
        }

        // setup symptom RecyclerView
        symptomAdapter = SymptomAdapter(symptoms) { position ->
            symptoms.removeAt(position)
            symptomAdapter.notifyItemRemoved(position)
        }
        binding.rvSymptoms.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSymptoms.adapter = symptomAdapter

        // setup medication RecyclerView
        currentMedicationsAdapter = MedicationsAdapter(emptyList()) { medication -> }
        binding.rvCurrentMedications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCurrentMedications.adapter = currentMedicationsAdapter

        viewModel.medications.observe(viewLifecycleOwner) { updatedMeds ->
            //currentMedicationsAdapter.updateMedications(updatedMeds.filter { it.currentlyTaking })
            currentMedicationsAdapter.updateMedications(updatedMeds.filter { it.currentlyTaking})
        }

        viewModel.userAge.observe(viewLifecycleOwner) { age ->
            binding.tvAge.text = "Age: $age"
        }
        viewModel.userSex.observe(viewLifecycleOwner) { sex ->
            binding.tvSex.text = "Sex: $sex"
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}