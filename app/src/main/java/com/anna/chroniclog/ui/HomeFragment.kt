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
import com.google.firebase.auth.FirebaseAuth
import com.anna.chroniclog.databinding.FragmentHomeBinding
import kotlin.getValue

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var currentMedicationsAdapter: MedicationsAdapter
    private lateinit var symptomAdapter: SymptomAdapter
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Greet User
        val user = FirebaseAuth.getInstance().currentUser
        binding.tvGreetUser.text = "Welcome, ${user?.displayName ?: user?.email}!"

        // Provider Share Button
        binding.btnProviderShare.setOnClickListener {}

        // nav to edit health info frag
        binding.btnEditHealthInfo.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToEditHealthInformationFragment())
        }

        // observe user's age and sex
        viewModel.userAge.observe(viewLifecycleOwner) { age ->
            binding.tvAge.text = "Age: $age"
        }
        viewModel.userSex.observe(viewLifecycleOwner) { sex ->
            binding.tvSex.text = "Sex: $sex"
        }

        // setup medication RecyclerView
        currentMedicationsAdapter = MedicationsAdapter(emptyList()) { medication -> }
        binding.rvCurrentMedications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCurrentMedications.adapter = currentMedicationsAdapter

        // want to make medication btnDeleteMed view.GONE

        // observe current medications
        viewModel.medications.observe(viewLifecycleOwner) { updatedMeds ->
            currentMedicationsAdapter.updateMedications(updatedMeds.filter { it.currentlyTaking})
        }

        // setup symptom RecyclerView
        symptomAdapter = SymptomAdapter(emptyList()) { symptom ->}
        binding.rvSymptoms.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSymptoms.adapter = symptomAdapter

        // observe chronic symptoms
        //viewModel.medications.observe(viewLifecycleOwner) { chronicSymptoms ->
            //symptomAdapter.updateSymptoms(chronicSymptoms.filter {it.})
        //}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}