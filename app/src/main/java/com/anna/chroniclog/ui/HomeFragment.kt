package com.anna.chroniclog.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.anna.chroniclog.MainViewModel
import com.anna.chroniclog.TrendsViewModel
import com.anna.chroniclog.adapter.MedicationsAdapter
import com.anna.chroniclog.adapter.SymptomAdapter
import com.google.firebase.auth.FirebaseAuth
import com.anna.chroniclog.databinding.FragmentHomeBinding
import com.anna.chroniclog.util.PdfHelper
import com.google.android.material.chip.Chip
import kotlin.getValue

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var currentMedicationsAdapter: MedicationsAdapter
    private lateinit var symptomAdapter: SymptomAdapter
    private val trendsViewModel: TrendsViewModel by activityViewModels ()
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

        trendsViewModel.loadSymptomFrequencies()
        // Provider Share Button
        binding.btnProviderShare.setOnClickListener {
            shareMedicalReport()
        }

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
        viewModel.chronicIllnesses.observe(viewLifecycleOwner) { illnesses ->
            binding.cgHomeIllnesses.removeAllViews()

            if (illnesses.isNullOrEmpty()) {

            } else {
                illnesses.forEach { illness ->
                    val chip = Chip(requireContext()).apply {
                        text = illness
                        isClickable = false
                        isCheckable = false
                        isCloseIconVisible = false
                        setChipBackgroundColorResource(android.R.color.holo_purple)
                    }
                    binding.cgHomeIllnesses.addView(chip)
            }}
        }

        // setup medication RecyclerView
        currentMedicationsAdapter = MedicationsAdapter(emptyList(), false) { medication -> }
        binding.rvCurrentMedications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCurrentMedications.adapter = currentMedicationsAdapter

        // observe current medications
        viewModel.medications.observe(viewLifecycleOwner) { updatedMeds ->
            currentMedicationsAdapter.updateMedications(updatedMeds.filter { it.currentlyTaking})
        }

    }

    private fun shareMedicalReport() {
        val age = viewModel.userAge.value ?: 0
        val sex = viewModel.userSex.value ?: "Not Specified"
        val illnesses = viewModel.chronicIllnesses.value ?: emptyList()
        val meds = viewModel.medications.value ?: emptyList()
        val symptoms = trendsViewModel.symptomFrequency.value ?: emptyMap()
        val remedies = viewModel.remediations.value ?: emptyList()

        // generate file
        val pdfFile = PdfHelper.createMedicalReport(requireContext(), age, sex, illnesses, meds, symptoms, remedies)

        // get URI via FileProvider
        val contentUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            pdfFile
        )

        // create intent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "Medical Health Report - ChronicLog")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Send Report via..."))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}