package com.anna.chroniclog.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anna.chroniclog.databinding.ItemLogBinding
import com.anna.chroniclog.model.LogEntry

class LogsAdapter(
    private var logs: List<LogEntry>,
    private val onItemClick: (LogEntry) -> Unit
) : RecyclerView.Adapter<LogsAdapter.LogViewHolder>() {

    inner class LogViewHolder(val binding: ItemLogBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemLogBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logs[position]
        with(holder.binding) {
            tvLogDate.text = log.date
            tvLogSentiment.text = log.sentiment
            root.setOnClickListener { onItemClick(log) }
        }
    }

    override fun getItemCount() = logs.size

    fun updateLogs(newLogs: List<LogEntry>) {
        logs = newLogs
        notifyDataSetChanged()
    }
}