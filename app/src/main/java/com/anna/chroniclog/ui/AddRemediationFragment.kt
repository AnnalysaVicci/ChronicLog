package com.anna.chroniclog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.anna.chroniclog.MainViewModel
import com.anna.chroniclog.R
import com.anna.chroniclog.databinding.FragmentAddRemediationBinding
import com.anna.chroniclog.model.Remediation
import kotlin.getValue

class AddRemediationFragment : Fragment() {
    private var _binding: FragmentAddRemediationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRemediationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddRemediationBinding.bind(view)

        // categories for Chronic Illness remedies
        val categories = arrayOf(
            "Medication", "Physical Therapy", "Lifestyle/Rest",
            "Dietary", "Supplement", "Mental Health"
        )

        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categories
        )
        binding.atvCategory.setAdapter(categoryAdapter)
        binding.atvCategory.threshold = 1

        // cancel button
        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        // add button
        binding.btnAdd.setOnClickListener {
            saveRemediation()
        }
    }

    private fun saveRemediation() {
        val category = binding.atvCategory.text.toString().trim()
        val description = binding.etDescription.text.toString()

        // determine outcome from RadioGroup
        val outcome = when (binding.rgOutcome.checkedRadioButtonId) {
            R.id.btnHelp -> "Helped"
            R.id.btnHurt -> "Hurt"
            else -> "No change"
        }

        val remediation = Remediation(
            name = category,
            outcome = outcome
        )

        if (category.isNotEmpty()) {
            // save to ViewModel
            viewModel.addTempRemediation(remediation)
            findNavController().popBackStack()
        } else {
            Toast.makeText(context, "Please select a category", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}