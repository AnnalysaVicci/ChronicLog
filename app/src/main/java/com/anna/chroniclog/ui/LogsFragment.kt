// LogsFragment - screen where all daily logs are shown
package com.anna.chroniclog.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.anna.chroniclog.MainViewModel
import com.anna.chroniclog.adapter.LogsAdapter
import com.anna.chroniclog.databinding.FragmentLogsBinding
import com.anna.chroniclog.model.LogEntry

class LogsFragment : Fragment() {

    private var _binding: FragmentLogsBinding? = null
    private val binding get() = _binding!!
    private lateinit var logsAdapter: LogsAdapter
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setup logs adapter
        logsAdapter = LogsAdapter(emptyList()) { log ->
            val action = LogsFragmentDirections.actionLogsFragmentToLogFragment(log.id)
            findNavController().navigate(action)
        }
        binding.rvLogs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLogs.adapter = logsAdapter

        // observe ViewModel
        viewModel.logs.observe(viewLifecycleOwner) {
            logs -> logsAdapter.updateLogs(logs) }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}