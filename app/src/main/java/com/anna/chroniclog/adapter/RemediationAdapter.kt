package com.anna.chroniclog.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.anna.chroniclog.databinding.ItemRemediationBinding
import com.anna.chroniclog.model.Remediation

class RemediationAdapter(
    private var remediations: List<Remediation>,
    private val showDeleteButton: Boolean = true,
    private val onRemove: (Remediation) -> Unit
) : RecyclerView.Adapter<RemediationAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemRemediationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RemediationAdapter.ViewHolder {
        val binding = ItemRemediationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val remediation = remediations[position]

        holder.binding.apply {
            tvRemediationDesc.text = remediation.name
            tvHelpOrHurt.text = remediation.outcome

            btnDeleteRemediation.visibility = if (showDeleteButton) View.VISIBLE else View.GONE
            btnDeleteRemediation.setOnClickListener {
                onRemove(remediation) // passing the object
            }
        }
    }

    override fun getItemCount() = remediations.size

    fun updateRemediations(newRemediations: List<Remediation>) {
        remediations = newRemediations
        notifyDataSetChanged()
    }
}