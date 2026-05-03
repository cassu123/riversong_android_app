package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.riversongai.R
import com.riversongai.data.model.ModelEntry
import com.riversongai.databinding.FragmentSettingsBinding
import com.riversongai.ui.viewmodel.SettingsViewModel
import com.riversongai.utils.ThemeManager
import org.koin.androidx.viewmodel.ext.android.viewModel

import android.media.MediaPlayer
import com.riversongai.data.model.MemoryTtlSettings
import com.riversongai.data.model.VoiceOption
import java.io.File
import java.io.FileOutputStream

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val settingsViewModel: SettingsViewModel by viewModel()

    private var allModels: List<ModelEntry> = emptyList()
    private val providerList = mutableListOf<String>()
    private val modelListForProvider = mutableListOf<String>()
    private var selectedProvider: String = ""
    private var selectedModelId: String = ""

    private var allVoices: List<VoiceOption> = emptyList()
    private val ttsProviderList = mutableListOf<String>()
    private val voiceListForProvider = mutableListOf<VoiceOption>()
    private var selectedTtsProvider: String = ""
    private var selectedVoice: VoiceOption? = null

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLlmSection()
        setupVoiceSection()
        setupMemoryTtlSection()
        
        settingsViewModel.loadVoices()
        settingsViewModel.loadMemoryTtl()
    }

    private fun setupLlmSection() {
        settingsViewModel.modelCatalog.observe(viewLifecycleOwner) { catalog ->
            catalog ?: return@observe
            allModels = catalog.local + catalog.cloud
            providerList.clear()
            providerList.addAll(allModels.map { it.provider }.distinct())

            val providerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, providerList)
            binding.spinnerProvider.setAdapter(providerAdapter)

            binding.spinnerProvider.setOnItemClickListener { _, _, position, _ ->
                selectedProvider = providerList[position]
                updateModelsForProvider(selectedProvider)
            }
        }

        settingsViewModel.llmSettings.observe(viewLifecycleOwner) { settings ->
            settings ?: return@observe
            binding.textViewCurrentModel.text = "Current: ${settings.provider} / ${settings.model}"
            selectedProvider = settings.provider
            selectedModelId = settings.model
            binding.spinnerProvider.setText(settings.provider, false)
            updateModelsForProvider(settings.provider, preselectModel = settings.model)
        }

        settingsViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBarSettings.isVisible = loading
        }

        settingsViewModel.saveResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                settingsViewModel.clearSaveResult()
            }
        }

        settingsViewModel.connectionTestResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                    .setBackgroundTint(requireContext().getColor(android.R.color.holo_green_dark))
                    .show()
                settingsViewModel.clearConnectionTestResult()
            }
        }

        settingsViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                    .setBackgroundTint(requireContext().getColor(android.R.color.holo_red_dark))
                    .show()
                settingsViewModel.clearError()
            }
        }

        binding.buttonSaveModel.setOnClickListener {
            if (selectedProvider.isBlank() || selectedModelId.isBlank()) {
                Toast.makeText(context, getString(R.string.settings_select_model_error), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            settingsViewModel.saveModel(selectedProvider, selectedModelId)
        }

        binding.buttonTestConnection.setOnClickListener {
            settingsViewModel.testConnection()
        }

        binding.switchDarkMode.isChecked = ThemeManager.getSelectedTheme(requireContext()) == ThemeManager.THEME_DARK
        binding.switchDarkMode.setOnCheckedChangeListener { _, checked ->
            ThemeManager.setTheme(requireContext(), if (checked) ThemeManager.THEME_DARK else ThemeManager.THEME_DEFAULT)
        }
    }

    private fun setupVoiceSection() {
        settingsViewModel.voices.observe(viewLifecycleOwner) { voices ->
            allVoices = voices
            ttsProviderList.clear()
            ttsProviderList.addAll(voices.map { it.provider }.distinct())
            
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, ttsProviderList)
            binding.spinnerTtsProvider.setAdapter(adapter)
            
            binding.spinnerTtsProvider.setOnItemClickListener { _, _, position, _ ->
                selectedTtsProvider = ttsProviderList[position]
                updateVoicesForProvider(selectedTtsProvider)
            }
        }

        settingsViewModel.voicePreviewData.observe(viewLifecycleOwner) { data ->
            data?.let {
                playPreview(it)
                settingsViewModel.clearVoicePreviewData()
            }
        }

        binding.buttonTestVoice.setOnClickListener {
            selectedVoice?.let {
                settingsViewModel.testVoice(it.id)
            } ?: Toast.makeText(context, "Select a voice first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateVoicesForProvider(provider: String) {
        voiceListForProvider.clear()
        voiceListForProvider.addAll(allVoices.filter { it.provider == provider })
        
        val names = voiceListForProvider.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
        binding.spinnerVoice.setAdapter(adapter)
        binding.spinnerVoice.setText("", false)
        selectedVoice = null
        
        binding.spinnerVoice.setOnItemClickListener { _, _, position, _ ->
            selectedVoice = voiceListForProvider[position]
        }
    }

    private fun setupMemoryTtlSection() {
        settingsViewModel.memoryTtl.observe(viewLifecycleOwner) { ttl ->
            ttl?.let {
                when (it.ttl) {
                    "7d" -> binding.radioTtl7d.isChecked = true
                    "30d" -> binding.radioTtl30d.isChecked = true
                    "90d" -> binding.radioTtl90d.isChecked = true
                    "365d" -> binding.radioTtl1y.isChecked = true
                    "forever" -> binding.radioTtlForever.isChecked = true
                }
                binding.switchAutoExtend.isChecked = it.autoExtend
            }
        }

        binding.buttonSaveMemoryTtl.setOnClickListener {
            val ttl = when (binding.radioGroupMemoryTtl.checkedRadioButtonId) {
                binding.radioTtl7d.id -> "7d"
                binding.radioTtl30d.id -> "30d"
                binding.radioTtl90d.id -> "90d"
                binding.radioTtl1y.id -> "365d"
                else -> "forever"
            }
            val autoExtend = binding.switchAutoExtend.isChecked
            settingsViewModel.saveMemoryTtl(MemoryTtlSettings(ttl, autoExtend))
        }
    }

    private fun playPreview(data: ByteArray) {
        try {
            val tempFile = File.createTempFile("voice_preview", "mp3", requireContext().cacheDir)
            FileOutputStream(tempFile).use { it.write(data) }
            
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    tempFile.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error playing preview", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateModelsForProvider(provider: String, preselectModel: String? = null) {
        modelListForProvider.clear()
        modelListForProvider.addAll(
            allModels.filter { it.provider == provider }.map { it.modelId }
        )
        val modelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, modelListForProvider)
        binding.spinnerModel.setAdapter(modelAdapter)

        if (preselectModel != null && modelListForProvider.contains(preselectModel)) {
            binding.spinnerModel.setText(preselectModel, false)
            selectedModelId = preselectModel
        }

        binding.spinnerModel.setOnItemClickListener { _, _, position, _ ->
            selectedModelId = modelListForProvider[position]
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
