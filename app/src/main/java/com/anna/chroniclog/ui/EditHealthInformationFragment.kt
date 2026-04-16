// HealthInformationFragment
package com.anna.chroniclog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.anna.chroniclog.MainViewModel
import com.anna.chroniclog.databinding.FragmentEditHealthInformationBinding


class EditHealthInformationFragment : Fragment() {
    private var _binding: FragmentEditHealthInformationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditHealthInformationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sexOptions = listOf("Female", "Male", "Non-binary", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sexOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSex.adapter = adapter

        // pre-fill age and sex info
        //viewModel.userAge.observe(viewLifecycleOwner) { age ->
            //if (age != null) binding.etAge.setText(age.toString())
        //}
        viewModel.userAge.value?.let { age ->
            binding.etAge.setText(age.toString())
        }
        /* viewModel.userSex.observe(viewLifecycleOwner) { sex ->
            if (!sex.isNullOrEmpty()) {
                val index = sexOptions.indexOf(sex)
                if (index >= 0) binding.spinnerSex.setSelection(index)
            }
        } */
        viewModel.userSex.value?.let { sex ->
            val index = sexOptions.indexOf(sex)
            if (index >= 0) binding.spinnerSex.setSelection(index)
        }

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSave.setOnClickListener {
            saveHealthInfo()
            findNavController().popBackStack()
        }
    }

    private fun saveHealthInfo() {
        val ageStr = binding.etAge.text.toString().trim()
        val sex = binding.spinnerSex.selectedItem.toString()

        if (ageStr.isEmpty()) {
            binding.etAge.error = "Please enter your age"
            return
        }
        val age = ageStr.toIntOrNull()
        if (age == null || age <= 0 || age > 120) {
            binding.etAge.error = "Please enter a valid age"
            return
        }

        viewModel.saveUserProfile(age, sex)
        Toast.makeText(requireContext(), "Health info saved", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}