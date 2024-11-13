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
import com.example.deteksikanker.R
import com.example.deteksikanker.data.response.ArticlesItem
import com.example.deteksikanker.databinding.ItemArticleBinding

class ArticleAdapter(private val onArticleClick: (String) -> Unit) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    private val articles = mutableListOf<ArticlesItem>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newArticles: List<ArticlesItem>) {
        articles.clear()
        articles.addAll(newArticles)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(articles[position])
    }

    override fun getItemCount(): Int = articles.size

    inner class ArticleViewHolder(private val binding: ItemArticleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(article: ArticlesItem) {
            binding.titleEvent.text = article.title
            binding.descriptionEvent.text = article.description

            val imageUrl = article.urlToImage
            if (!imageUrl.isNullOrEmpty()) {
                binding.imageLoadingIndicator.visibility = View.VISIBLE
                binding.imageEvent.visibility = View.VISIBLE

                Glide.with(binding.imageEvent.context)
                    .load(imageUrl)
                    .listener(object : RequestListener<Drawable> {
                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean,
                        ): Boolean {
                            binding.imageLoadingIndicator.visibility = View.GONE
                            binding.imageEvent.visibility = View.VISIBLE
                            return false
                        }

                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.imageLoadingIndicator.visibility = View.GONE
                            binding.imageEvent.visibility = View.VISIBLE
                            return false
                        }
                    })
                    .into(binding.imageEvent)
            } else {
                binding.imageLoadingIndicator.visibility = View.GONE
                binding.imageEvent.setImageResource(R.drawable.ic_place_holder)
                binding.imageEvent.visibility = View.VISIBLE
            }

            // Set click listener to open article URL
            binding.root.setOnClickListener {
                val articleUrl = article.url
                articleUrl?.let { onArticleClick(it) }
            }
        }
    }
}
