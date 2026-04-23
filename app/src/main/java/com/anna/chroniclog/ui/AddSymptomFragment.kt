
package com.anna.chroniclog.ui

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.anna.chroniclog.MainViewModel
import com.anna.chroniclog.databinding.FragmentAddSymptomBinding
import com.anna.chroniclog.model.Symptom
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class AddSymptomFragment : Fragment() {
    private var _binding: FragmentAddSymptomBinding? = null
    private val binding get() = _binding!!
    private var searchJob: kotlinx.coroutines.Job? = null
    private var photoUri: Uri? = null

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            // display img after taking
            binding.imgSymptomPicture.setImageURI(photoUri)
            binding.imgSymptomPicture.visibility = View.VISIBLE
        }
    }
    private val viewModel: MainViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddSymptomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //_binding = FragmentAddSymptomBinding.bind(view)

        // hide symptom image view
        binding.imgSymptomPicture.visibility = View.GONE

        // symptom autocomplete
        setupAutocomplete()

        // setup SeekBar listener
        binding.seekbarSeverity.max = 10

        // setup add picture
        binding.btnAddPicture.setOnClickListener {
            launchCamera()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnAdd.setOnClickListener {
            saveSymptom()
        }
    }

    private fun setupAutocomplete() {
        val reactionAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf()
        )
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

        viewModel.reactionSuggestions.observe(viewLifecycleOwner) { reactions ->
            val names = reactions.map { it.getDisplayName() }
            reactionAdapter.clear()
            reactionAdapter.addAll(names)
            reactionAdapter.filter.filter(binding.atvSymptom.text, null)
            if (names.isNotEmpty() && binding.atvSymptom.hasFocus()) {
                binding.atvSymptom.showDropDown()
            }
        }
    }

    private fun launchCamera() {
        // create file in internal storage
        val imageDir = File(requireContext().filesDir, "captured_media")
        if (!imageDir.exists()) imageDir.mkdirs()

        val imageFile = File(imageDir, "temp_symptom.jpg")

        // get uri via FileProvider
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            imageFile
        )

        // launch camera
        takePicture.launch(photoUri)
    }

    private fun saveSymptom() {
        val symptomName = binding.atvSymptom.text.toString().trim()
        if (symptomName.isEmpty()) {
            binding.atvSymptom.error = "please enter a symptom"
            Toast.makeText(requireContext(), "please enter a symptom", Toast.LENGTH_SHORT).show()
            return
        }

        val symptomId = UUID.randomUUID().toString()
        val currentUri = photoUri

        if (currentUri != null) {
            viewModel.uploadSymptomImage(
                symptomId = symptomId,
                localUri = currentUri,
                onSuccess = { downloadUrl ->
                    buildAndSaveSymptom(symptomId, symptomName, downloadUrl)
                },
                onFailure = {
                    Toast.makeText(requireContext(), "Symptom saved but photo upload failed", Toast.LENGTH_SHORT).show()
                    buildAndSaveSymptom(symptomId, symptomName, "")
                }
            )
        } else {
            buildAndSaveSymptom(symptomId, symptomName, "")
        }
    }

    private fun buildAndSaveSymptom(id: String, name: String, imageUri: String) {
        val symptom = Symptom(
            id = id,
            name = name,
            severity = binding.seekbarSeverity.progress,
            imageUri = imageUri
        )
        viewModel.addTempSymptom(symptom)
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}