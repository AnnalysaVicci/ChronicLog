// LogsFragment - screen where all daily logs are shown
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
import com.anna.chroniclog.databinding.FragmentMedicationsBinding
import com.anna.chroniclog.model.Medication
import kotlin.getValue

class MedicationsFragment : Fragment() {
    private var _binding: FragmentMedicationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var currentMedicationsAdapter: MedicationsAdapter
    private lateinit var allMedicationsAdapter: MedicationsAdapter
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setup medications RecyclerView
        currentMedicationsAdapter = MedicationsAdapter(emptyList()) { medication -> }
        binding.rvCurrentMedications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCurrentMedications.adapter = currentMedicationsAdapter

        allMedicationsAdapter = MedicationsAdapter(emptyList()) { medication -> }
        binding.rvAllMedications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAllMedications.adapter = allMedicationsAdapter

        viewModel.medications.observe(viewLifecycleOwner) { medications ->
            currentMedicationsAdapter.updateMedications(medications.filter { it.currentlyTaking })
            allMedicationsAdapter.updateMedications(medications.filter { !it.currentlyTaking })
        }

        // observe ViewModel
        viewModel.medications.observe(viewLifecycleOwner) { medications ->
            currentMedicationsAdapter.updateMedications(medications.filter { it.currentlyTaking })
            allMedicationsAdapter.updateMedications(medications.filter { !it.currentlyTaking })
        }

        binding.btnAddMedication.setOnClickListener {
            findNavController().navigate(MedicationsFragmentDirections.actionMedicationsFragmentToAddMedicationFragment())
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}