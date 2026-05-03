package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.riversongai.data.model.FeedPreferences
import com.riversongai.databinding.FragmentFeedSettingsBinding
import com.riversongai.ui.viewmodel.FeedsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class FeedSettingsFragment : Fragment() {

    private var _binding: FragmentFeedSettingsBinding? = null
    private val binding get() = _binding!!

    private val feedsViewModel: FeedsViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFeedSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        feedsViewModel.loadPreferences()

        feedsViewModel.preferences.observe(viewLifecycleOwner) { prefs ->
            prefs ?: return@observe
            binding.editTextLat.setText(prefs.weatherLat?.toString() ?: "")
            binding.editTextLon.setText(prefs.weatherLon?.toString() ?: "")
            binding.editTextTickers.setText(prefs.stockTickers.joinToString(","))
        }

        feedsViewModel.saveResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                feedsViewModel.clearSaveResult()
            }
        }

        feedsViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                feedsViewModel.clearError()
            }
        }

        binding.buttonSaveFeedSettings.setOnClickListener {
            val lat = binding.editTextLat.text.toString().toDoubleOrNull()
            val lon = binding.editTextLon.text.toString().toDoubleOrNull()
            val tickersRaw = binding.editTextTickers.text.toString()
            val tickers = tickersRaw.split(",").map { it.trim().uppercase() }.filter { it.isNotBlank() }

            val current = feedsViewModel.preferences.value ?: FeedPreferences()
            val updated = current.copy(
                weatherLat = lat,
                weatherLon = lon,
                stockTickers = tickers
            )
            feedsViewModel.savePreferences(updated)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
