package com.example.deteksikanker.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.deteksikanker.data.local.database.HistoryCancerRecord
import com.example.deteksikanker.databinding.ItemHistoryBinding

class HistoryAdapter(private val onItemClick: (HistoryCancerRecord) -> Unit) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private var records: List<HistoryCancerRecord> = listOf()

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n", "DefaultLocale")
        fun bind(record: HistoryCancerRecord) {
            binding.titleEvent.text = record.title
            binding.descriptionEvent.text = "Confidence: ${String.format("%.2f", record.confidenceScore * 100)}%"

            loadImage(record.image)
            itemView.setOnClickListener { onItemClick(record) }
        }

        private fun loadImage(imageUrl: String) {
            binding.imageLoadingIndicator.visibility = View.VISIBLE

            Glide.with(itemView.context)
                .load(imageUrl)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.imageLoadingIndicator.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.imageLoadingIndicator.visibility = View.GONE
                        return false
                    }
                })
                .into(binding.imageEvent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(records[position])
    }

    override fun getItemCount(): Int = records.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newRecords: List<HistoryCancerRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }
}
