package com.anna.chroniclog.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.RotateAnimation
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.anna.chroniclog.MainViewModel
import com.anna.chroniclog.databinding.ItemMedicationBinding
import com.anna.chroniclog.model.Medication
import kotlin.getValue

class MedicationsAdapter(
    private var medications: List<Medication>,
    private val onItemClick: (Medication) -> Unit
) : RecyclerView.Adapter<MedicationsAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ItemMedicationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMedicationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medication = medications[position]

        holder.binding.apply {
            // always visible data
            tvMedName.text = medication.name
            tvMedDosage.text = medication.dosage
            tvMedFreq.text = medication.frequency

            // expandable details data
            tvMedFreq.text = "Frequency: ${medication.frequency}"
            tvStartDate.text = "Started: ${medication.startDate}"
            tvAdherence.text = "Adherance: ${medication.adherence}"
            checkboxCurrentlyTaking.isChecked = medication.currentlyTaking
            tvEndDate.text = if (medication.currentlyTaking) "" else "Ended: ${medication.endDate}"
            tvEndDate.visibility = if (medication.currentlyTaking) View.GONE else View.VISIBLE

            btnDeleteMed.setOnClickListener {
                //onRemove(holder.bindingAdapterPosition)
            }

            // hidden Section logic
            llMedDetails.visibility = if (medication.isExpanded) View.VISIBLE else View.GONE
            ivExpandArrow.rotation = if (medication.isExpanded) 180f else 0f

            // toggle logic
            root.setOnClickListener {
                medication.isExpanded = !medication.isExpanded
                notifyItemChanged(position)
                onItemClick(medication)
            }
        }
    }

    override fun getItemCount() = medications.size

    fun updateMedications(newMedications: List<Medication>) {
        medications = newMedications
        notifyDataSetChanged()
    }
}