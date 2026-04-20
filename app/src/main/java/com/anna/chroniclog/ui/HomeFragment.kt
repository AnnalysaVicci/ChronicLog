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
import com.google.firebase.auth.FirebaseAuth
import com.anna.chroniclog.databinding.FragmentHomeBinding
import kotlin.getValue

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var currentMedicationsAdapter: MedicationsAdapter
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // check if onboarding is needed
        //viewModel.checkIfProfileExists().observe(viewLifecycleOwner) { exists ->
            //if (!exists) { OnboardingDialogFragment().show(parentFragmentManager, "onboarding") } }
        // if user exists without a username/age/sex, make them make one

        // Greet User
        val user = FirebaseAuth.getInstance().currentUser
        binding.tvGreetUser.text = "Welcome, ${user?.displayName ?: user?.email}!"

        binding.btnProviderShare.setOnClickListener {  }

        // nav to edit health info frag
        binding.btnEditHealthInfo.setOnClickListener {
            findNavController().navigate(HealthInformationFragmentDirections.actionHealthInformationFragmentToEditHealthInformationFragment())
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

        viewModel.medications.observe(viewLifecycleOwner) { updatedMeds ->
            currentMedicationsAdapter.updateMedications(updatedMeds.filter { it.currentlyTaking})
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}