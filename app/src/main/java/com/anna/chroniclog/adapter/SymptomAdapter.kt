package com.anna.chroniclog.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anna.chroniclog.databinding.ItemSymptomBinding
import com.anna.chroniclog.model.Symptom

class SymptomAdapter(
    private var symptoms: List<Symptom>,
    private val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<SymptomAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSymptomBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SymptomAdapter.ViewHolder {
        val binding = ItemSymptomBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val symptom = symptoms[position]

        holder.binding.apply {
            tvSymptomName.text = symptom.name
            //btnRemoveSymptom.setOnClickListener {onRemove(holder.bindingAdapterPosition)}
        }
    }

    override fun getItemCount() = symptoms.size

    fun updateSymptoms(newSymptoms: List<Symptom>) {
        symptoms = newSymptoms
        notifyDataSetChanged()
    }
}