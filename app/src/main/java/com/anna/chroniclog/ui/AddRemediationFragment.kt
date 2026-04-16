package com.anna.chroniclog.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.anna.chroniclog.MainActivity
import com.anna.chroniclog.MainViewModel
import com.anna.chroniclog.R
import com.anna.chroniclog.databinding.FragmentAddRemediationBinding
import com.anna.chroniclog.model.Remediation
import com.google.firebase.auth.FirebaseAuth
import kotlin.getValue

class AddRemediationFragment : Fragment() {
    private var _binding: FragmentAddRemediationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        binding.autoCompleteCategory.setAdapter(categoryAdapter)

        // cancel button
        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // add button
        binding.btnAdd.setOnClickListener {
            saveRemediation()
            parentFragmentManager.popBackStack()
        }
    }

    private fun saveRemediation() {
        val category = binding.autoCompleteCategory.text.toString()
        val description = binding.etDescription.text.toString()

        // determine outcome from RadioGroup
        val outcome = when (binding.rgOutcome.checkedRadioButtonId) {
            R.id.btnHelp -> "Helped"
            R.id.btnHurt -> "Hurt"
            else -> "No change"
        }

        if (category.isNotEmpty()) {
            // save to ViewModel
            viewModel.addTempRemediation(Remediation(category, description, outcome))
            parentFragmentManager.popBackStack()
        } else {
            Toast.makeText(context, "Please select a category", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}