package com.anna.chroniclog.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anna.chroniclog.databinding.ItemSymptomImageBinding
import com.bumptech.glide.Glide

class SymptomImageAdapter(private var imageUrls: List<String>) :
    RecyclerView.Adapter<SymptomImageAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSymptomImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SymptomImageAdapter.ViewHolder {
        val binding = ItemSymptomImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val url = imageUrls[position]
        val targetImageView = holder.binding.ivSymptomDetail

        Glide.with(targetImageView.context)
            .load(url)
            .placeholder(android.R.drawable.progress_indeterminate_horizontal)
            .error(android.R.drawable.stat_notify_error)
            .centerCrop()
            .into(targetImageView)
    }

    override fun getItemCount() = imageUrls.size

    fun updateImages(newUrls: List<String>) {
        imageUrls = newUrls
        notifyDataSetChanged()
    }
}