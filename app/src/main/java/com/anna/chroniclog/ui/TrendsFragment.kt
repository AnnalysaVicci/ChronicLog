package com.anna.chroniclog.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.anna.chroniclog.MainViewModel
import com.anna.chroniclog.databinding.FragmentTrendsBinding
import kotlin.getValue

//import com.github.mikephil.charting.components.XAxis
//import com.github.mikephil.charting.data.*
//import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
//import com.github.mikephil.charting.utils.ColorTemplate

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
    /*
    private fun setupChart(frequencyMap: Map<String, Int>) {
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        // 1. Convert Map to BarEntries
        frequencyMap.entries.forEachIndexed { index, entry ->
            entries.add(BarEntry(index.toFloat(), entry.value.toFloat()))
            labels.add(entry.key)
        }

        // 2. Create the DataSet and style it
        val dataSet = BarDataSet(entries, "Symptom Frequency")
        dataSet.color = Color.MAGENTA
        dataSet.valueTextSize = 12f

        // 3. Bind to the Chart
        val barData = BarData(dataSet)
        binding.barChart.data = barData

        // 4. Map the indices back to Symptom Names on the X-axis
        binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.barChart.xAxis.granularity = 1f

        binding.barChart.invalidate() // Refresh the chart
    } */

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}