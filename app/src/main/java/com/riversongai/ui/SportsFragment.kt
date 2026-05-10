package com.riversongai.ui

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayout
import com.riversongai.R
import com.riversongai.data.model.SportsMatch
import com.riversongai.data.model.SportsTeam
import com.riversongai.databinding.FragmentFeedsSportsBinding
import com.riversongai.ui.adapter.SportsMatchAdapter
import com.riversongai.ui.adapter.StandingsAdapter
import com.riversongai.ui.viewmodel.SportsViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class SportsFragment : Fragment(R.layout.fragment_feeds_sports) {

    private var _binding: FragmentFeedsSportsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SportsViewModel by activityViewModel()
    
    private lateinit var matchAdapter: SportsMatchAdapter
    private lateinit var standingsAdapter: StandingsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFeedsSportsBinding.bind(view)

        matchAdapter = SportsMatchAdapter()
        standingsAdapter = StandingsAdapter()
        
        binding.recyclerViewSports.apply {
            adapter = matchAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.buttonSearchTeam.setOnClickListener { showSearchDialog() }
        binding.editTextSearchTeam.setOnEditorActionListener { _, _, _ -> showSearchDialog(); true }

        binding.tabLayoutSports.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) { updateList() }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.chipAllTeams.setOnClickListener { viewModel.selectTeam(null) }
    }

    private fun observeViewModel() {
        viewModel.followedTeams.observe(viewLifecycleOwner) { teams ->
            populateTeamChips(teams)
        }

        viewModel.selectedTeam.observe(viewLifecycleOwner) { selected ->
            updateSelectedChip(selected)
            updateList()
        }

        viewModel.results.observe(viewLifecycleOwner) { if (binding.tabLayoutSports.selectedTabPosition == 0) updateList() }
        viewModel.fixtures.observe(viewLifecycleOwner) { if (binding.tabLayoutSports.selectedTabPosition == 1) updateList() }
        viewModel.standings.observe(viewLifecycleOwner) { if (binding.tabLayoutSports.selectedTabPosition == 2) updateList() }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { /* show loading if needed */ }
    }

    private fun updateList() {
        val tabPos = binding.tabLayoutSports.selectedTabPosition
        when (tabPos) {
            0 -> {
                binding.recyclerViewSports.adapter = matchAdapter
                matchAdapter.submitList(viewModel.results.value)
                binding.textViewSportsEmpty.isVisible = viewModel.results.value.isNullOrEmpty()
                binding.textViewSportsEmpty.text = "No recent results"
            }
            1 -> {
                binding.recyclerViewSports.adapter = matchAdapter
                matchAdapter.submitList(viewModel.fixtures.value)
                binding.textViewSportsEmpty.isVisible = viewModel.fixtures.value.isNullOrEmpty()
                binding.textViewSportsEmpty.text = "No upcoming fixtures"
            }
            2 -> {
                binding.recyclerViewSports.adapter = standingsAdapter
                val leagueId = viewModel.selectedTeam.value?.leagueId 
                    ?: viewModel.followedTeams.value?.firstOrNull()?.leagueId
                
                if (leagueId != null) {
                    viewModel.loadStandings(leagueId)
                    standingsAdapter.submitList(viewModel.standings.value)
                    binding.textViewSportsEmpty.isVisible = viewModel.standings.value.isNullOrEmpty()
                } else {
                    standingsAdapter.submitList(emptyList())
                    binding.textViewSportsEmpty.isVisible = true
                }
                binding.textViewSportsEmpty.text = "Search for a team above to get started"
            }
        }
    }

    private fun populateTeamChips(teams: List<SportsTeam>) {
        // Keep "All" chip
        val allChip = binding.chipAllTeams
        binding.chipGroupTeams.removeAllViews()
        binding.chipGroupTeams.addView(allChip)

        teams.forEach { team ->
            val chip = Chip(ContextThemeWrapper(requireContext(), com.google.android.material.R.style.Widget_Material3_Chip_Filter)).apply {
                text = team.name
                isCheckable = true
                setOnClickListener { viewModel.selectTeam(team) }
            }
            binding.chipGroupTeams.addView(chip)
        }
    }

    private fun updateSelectedChip(selected: SportsTeam?) {
        for (i in 0 until binding.chipGroupTeams.childCount) {
            val chip = binding.chipGroupTeams.getChildAt(i) as? Chip ?: continue
            chip.isChecked = (selected == null && chip.id == R.id.chipAllTeams) || (selected?.name == chip.text.toString())
        }
    }

    private fun showSearchDialog() {
        val query = binding.editTextSearchTeam.text.toString()
        if (query.isBlank()) return
        viewModel.searchTeams(query)
        
        // Simplified search result handling for now
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Add Team")
            .setItems(arrayOf("Searching...")) { _, _ -> }
            .show()

        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            if (results.isEmpty()) {
                dialog.setTitle("No teams found")
            } else {
                dialog.setTitle("Select Team to Follow")
                val names = results.map { "${it.name} (${it.leagueName})" }.toTypedArray()
                // Re-setup listener
                val listView = dialog.listView
                // This is a bit hacky for a quick fix, normally would use a custom adapter
                // but let's just close and re-show for simplicity in this turn
                dialog.dismiss()
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Select Team to Follow")
                    .setItems(names) { _, which ->
                        viewModel.followTeam(results[which])
                    }
                    .show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
