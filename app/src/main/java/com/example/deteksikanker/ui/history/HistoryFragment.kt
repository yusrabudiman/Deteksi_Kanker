package com.example.deteksikanker.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deteksikanker.adapter.HistoryAdapter
import com.example.deteksikanker.databinding.FragmentHistoryBinding
import com.example.deteksikanker.ui.resultactivity.ResultActivity

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val historyViewModel: HistoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        val adapter = HistoryAdapter { record ->
            val intent = Intent(requireContext(), ResultActivity::class.java).apply {
                putExtra("image", record.image)
                putExtra("title", record.title)
                putExtra("confidenceScore", record.confidenceScore)
            }
            startActivity(intent)
        }

        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHistory.adapter = adapter

        historyViewModel.allRecords.observe(viewLifecycleOwner) { records ->
            if (records.isNullOrEmpty()) {
                binding.recyclerViewHistory.visibility = View.GONE
                binding.emptyMessage.visibility = View.VISIBLE
            } else {
                binding.recyclerViewHistory.visibility = View.VISIBLE
                binding.emptyMessage.visibility = View.GONE
                adapter.submitList(records)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
