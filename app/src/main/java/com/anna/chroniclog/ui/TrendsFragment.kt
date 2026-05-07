package com.anna.chroniclog.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.anna.chroniclog.TrendsViewModel
import com.anna.chroniclog.databinding.FragmentTrendsBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch
import kotlin.getValue

class TrendsFragment : Fragment() {
    private var _binding: FragmentTrendsBinding? = null
    private val binding get() = _binding!!
    private val trendsViewModel: TrendsViewModel by activityViewModels()

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

        // tell the Trends analyst to go get the data
        trendsViewModel.loadSymptomFrequencies()

        // collect the StateFlow data
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                trendsViewModel.symptomFrequency.collect { frequencyMap ->
                    if (frequencyMap.isNotEmpty()) {
                        setupChart(frequencyMap)
                        binding.cardFrequencyChart.visibility = View.VISIBLE
                        binding.tvTrendNotif.visibility = View.GONE
                    } else {
                        binding.cardFrequencyChart.visibility = View.GONE
                        binding.tvTrendNotif.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun setupChart(frequencyMap: Map<String, Int>) {

        val top3 = frequencyMap.entries
            .sortedByDescending { it.value }
            .take(3)

        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        top3.forEachIndexed { index, entry ->
            entries.add(BarEntry(index.toFloat(), entry.value.toFloat()))
            labels.add(entry.key.lowercase().replaceFirstChar { it.uppercase() })
        }

        val dataSet = BarDataSet(entries, "Symptom Frequency").apply {
            color = Color.parseColor("#6A0DAD")
            valueTextSize = 14f
            valueTextColor = Color.BLACK
        }

        binding.barChart.apply {
            data = BarData(dataSet).apply {
                barWidth = 0.5f  // narrower bars with more breathing room
            }
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                textSize = 13f
                labelRotationAngle = 0f  // keep labels horizontal since there are only 3
            }
            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f
            axisLeft.granularity = 1f
            description.isEnabled = false
            legend.isEnabled = false
            setFitBars(true)  // ensures bars fit nicely in the chart width
            animateY(400)
            invalidate()
        }

        /*
        // convert map to BarEntries
        frequencyMap.entries.forEachIndexed { index, entry ->
            entries.add(BarEntry(index.toFloat(), entry.value.toFloat()))
            labels.add(entry.key)
        }

        // create the DataSet and style it
        val dataSet = BarDataSet(entries, "Symptom Frequency")
        dataSet.color = Color.MAGENTA
        dataSet.valueTextSize = 12f

        // bind to the Chart
        val barData = BarData(dataSet)
        binding.barChart.data = barData

        // map the indices back to Symptom Names on the X-axis
        binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.barChart.xAxis.granularity = 1f

        binding.barChart.invalidate() // Refresh the chart */
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}