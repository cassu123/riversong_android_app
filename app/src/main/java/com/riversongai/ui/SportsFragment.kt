package com.riversongai.ui

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.riversongai.R
import com.riversongai.data.model.SportsTeam
import com.riversongai.databinding.FragmentFeedsSportsBinding
import com.riversongai.ui.adapter.SportsMatchAdapter
import com.riversongai.ui.viewmodel.SportsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SportsFragment : Fragment(R.layout.fragment_feeds_sports) {

    private var _binding: FragmentFeedsSportsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SportsViewModel by viewModel()
    private lateinit var matchAdapter: SportsMatchAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFeedsSportsBinding.bind(view)

        matchAdapter = SportsMatchAdapter()
        binding.recyclerViewSports.apply {
            adapter = matchAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.buttonSearchTeam.setOnClickListener { performSearch() }
        binding.editTextSearchTeam.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else false
        }

        binding.tabLayoutSports.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                updateList()
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
        })
    }

    private fun performSearch() {
        val query = binding.editTextSearchTeam.text.toString()
        if (query.isNotBlank()) {
            viewModel.searchTeams(query)
        }
    }

    private fun observeViewModel() {
        viewModel.followedTeams.observe(viewLifecycleOwner) { teams ->
            populateTeamChips(teams)
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            if (results.isNotEmpty()) {
                showSearchResultsDialog(results)
            }
        }

        viewModel.results.observe(viewLifecycleOwner) { updateList() }
        viewModel.fixtures.observe(viewLifecycleOwner) { updateList() }
    }

    private fun populateTeamChips(teams: List<SportsTeam>) {
        binding.chipGroupTeams.removeAllViews()
        
        val allChip = Chip(requireContext()).apply {
            text = "All"
            isCheckable = true
            isChecked = viewModel.selectedTeam.value == null
            setOnClickListener { viewModel.selectTeam(null) }
        }
        binding.chipGroupTeams.addView(allChip)

        teams.forEach { team ->
            val chip = Chip(requireContext()).apply {
                text = team.name
                isCheckable = true
                isChecked = viewModel.selectedTeam.value?.id == team.id
                setOnClickListener { viewModel.selectTeam(team) }
                setOnLongClickListener {
                    showUnfollowDialog(team)
                    true
                }
            }
            binding.chipGroupTeams.addView(chip)
        }
    }

    private fun updateList() {
        val isResults = binding.tabLayoutSports.selectedTabPosition == 0
        val list = if (isResults) viewModel.results.value else viewModel.fixtures.value
        matchAdapter.submitList(list)
        binding.textViewSportsEmpty.visibility = if (list.isNullOrEmpty()) View.VISIBLE else View.GONE
    }

    private fun showSearchResultsDialog(results: List<SportsTeam>) {
        val names = results.map { "${it.name} (${it.leagueName})" }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Search Results")
            .setItems(names) { _, which ->
                viewModel.followTeam(results[which])
                binding.editTextSearchTeam.text?.clear()
            }
            .show()
    }

    private fun showUnfollowDialog(team: SportsTeam) {
        AlertDialog.Builder(requireContext())
            .setTitle("Unfollow ${team.name}?")
            .setPositiveButton("Unfollow") { _, _ -> viewModel.unfollowTeam(team.id) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
