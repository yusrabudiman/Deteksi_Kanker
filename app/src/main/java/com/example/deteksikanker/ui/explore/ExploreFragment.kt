package com.example.deteksikanker.ui.explore

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deteksikanker.adapter.ArticleAdapter
import com.example.deteksikanker.databinding.FragmentDashboardBinding

class ExploreFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var articleAdapter: ArticleAdapter
    private val exploreViewModel: ExploreViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()

        exploreViewModel.articles.observe(viewLifecycleOwner) { articles ->
            articleAdapter.submitList(articles)
            binding.progressBar.visibility = View.GONE
        }

        exploreViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
        }

        binding.progressBar.visibility = View.VISIBLE
        exploreViewModel.fetchCancerArticles()

        return root
    }

    private fun setupRecyclerView() {
        articleAdapter = ArticleAdapter { articleUrl ->
            openArticleInCustomTab(articleUrl)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = articleAdapter
        }
    }

    private fun openArticleInCustomTab(url: String) {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(requireContext(), Uri.parse(url))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
