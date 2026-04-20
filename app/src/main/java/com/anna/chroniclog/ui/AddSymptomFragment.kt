// AddLogFragment - screen where user can add a symptom
package com.anna.chroniclog.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.anna.chroniclog.MainViewModel
import com.anna.chroniclog.databinding.FragmentAddSymptomBinding
import com.anna.chroniclog.model.Symptom
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddSymptomFragment : Fragment() {
    private var _binding: FragmentAddSymptomBinding? = null
    private val binding get() = _binding!!

    //private var photoUri: Uri? = null
    private var searchJob: kotlinx.coroutines.Job? = null
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddSymptomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddSymptomBinding.bind(view)

        val reactionAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        binding.atvSymptom.setAdapter(reactionAdapter)
        binding.atvSymptom.threshold = 3

        binding.atvSymptom.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""
                if (query.length >= 2) {
                    searchJob?.cancel()
                    searchJob = viewLifecycleOwner.lifecycleScope.launch {
                        delay(500)
                        viewModel.searchReactions(query)
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        // observe results
        viewModel.reactionSuggestions.observe(viewLifecycleOwner) { reactions ->
            val names = reactions.map { it.getDisplayName() }
            reactionAdapter.clear()
            reactionAdapter.addAll(names)

            // AutoCompleteTextView needs to know the data changed to filter the list
            reactionAdapter.filter.filter(binding.atvSymptom.text, null)

            if (names.isNotEmpty() && binding.atvSymptom.hasFocus()) {
                binding.atvSymptom.showDropDown()
            }
        }

        // setup SeekBar listener
        binding.seekbarSeverity.max = 10

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnAdd.setOnClickListener {
            saveSymptom()
            parentFragmentManager.popBackStack()
        }
    }

    private fun saveSymptom() {
        val symptomName = binding.atvSymptom.text.toString().trim()
        if (symptomName.isEmpty()) {
            binding.atvSymptom.error = "Please enter or select a symptom"
            return
        }
        val symptom = Symptom(
            name = symptomName,
            severity = binding.seekbarSeverity.progress
        )
        // symptoms before the final Log is saved
        viewModel.addTempSymptom(symptom)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}