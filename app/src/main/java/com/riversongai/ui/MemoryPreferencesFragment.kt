package com.riversongai.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.riversongai.R
import com.riversongai.databinding.FragmentMemoryPreferencesBinding
import com.riversongai.ui.adapter.PreferenceAdapter
import com.riversongai.ui.viewmodel.MemoryViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MemoryPreferencesFragment : Fragment(R.layout.fragment_memory_preferences) {

    private var _binding: FragmentMemoryPreferencesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MemoryViewModel by sharedViewModel()
    private lateinit var preferenceAdapter: PreferenceAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMemoryPreferencesBinding.bind(view)

        preferenceAdapter = PreferenceAdapter { pref ->
            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete this preference?")
                .setMessage("Are you sure you want to delete this preference?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete") { _, _ -> viewModel.deletePreference(pref.id) }
                .show()
        }
        binding.recyclerViewPreferences.apply {
            adapter = preferenceAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.preferences.observe(viewLifecycleOwner) { prefs ->
            preferenceAdapter.submitList(prefs)
            binding.textViewEmpty.visibility = if (prefs.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
