package com.anna.chroniclog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.anna.chroniclog.MainViewModel
import com.anna.chroniclog.databinding.FragmentTrendsBinding
import kotlin.getValue

class TrendsFragment : Fragment() {
    private var _binding: FragmentTrendsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //setup trends

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}