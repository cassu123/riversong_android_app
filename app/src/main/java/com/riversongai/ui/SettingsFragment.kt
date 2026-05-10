package com.riversongai.ui

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.materialswitch.MaterialSwitch
import com.riversongai.R
import com.riversongai.data.model.MemoryTtlSettings
import com.riversongai.data.model.ModelEntry
import com.riversongai.data.model.VoiceOption
import com.riversongai.databinding.FragmentSettingsBinding
import com.riversongai.ui.viewmodel.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val settingsViewModel: SettingsViewModel by viewModel()

    private var allModels: List<ModelEntry> = emptyList()
    private val providerList = mutableListOf<String>()
    private val modelListForProvider = mutableListOf<String>()
    private var selectedProvider: String = ""
    private var selectedModelId: String = ""

    private var previewingVoiceId: String? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLlmSection()
        setupVoiceSection()
        setupMemoryTtlSection()
        setupAdminControls()
        settingsViewModel.loadVoices()
        settingsViewModel.loadMemoryTtl()
    }

    private fun setupAdminControls() {
        val isAdmin = requireContext()
            .getSharedPreferences("rs_prefs", Context.MODE_PRIVATE)
            .getBoolean("is_admin", false)

        if (!isAdmin) return
        
        binding.cardAdminControls.isVisible = true
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resp = settingsViewModel.getApiService().getFeatureVisibility()
                if (resp.isSuccessful) {
                    populateVisibilityToggles(resp.body().orEmpty())
                }
            } catch (e: Exception) { /* non-fatal */ }
        }
    }

    private fun populateVisibilityToggles(visibilityMap: Map<String, Boolean>) {
        binding.layoutFeatureVisibility.removeAllViews()
        val sortedFeatures = visibilityMap.keys.sorted()
        
        sortedFeatures.forEach { feature ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, 8.dp, 0, 8.dp)
            }
            
            val label = TextView(requireContext()).apply {
                val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                layoutParams = params
                text = feature.replaceFirstChar { it.uppercase() }
                textAppearance = com.google.android.material.R.style.TextAppearance_Material3_BodyMedium
            }
            
            val switch = MaterialSwitch(requireContext()).apply {
                isChecked = visibilityMap[feature] == true
                setOnCheckedChangeListener { _, isChecked ->
                    updateFeatureVisibility(feature, isChecked)
                }
            }
            
            row.addView(label)
            row.addView(switch)
            binding.layoutFeatureVisibility.addView(row)
        }
    }

    private fun updateFeatureVisibility(feature: String, isVisible: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val current = mutableMapOf<String, Boolean>()
                for (i in 0 until binding.layoutFeatureVisibility.childCount) {
                    val row = binding.layoutFeatureVisibility.getChildAt(i) as LinearLayout
                    val label = row.getChildAt(0) as TextView
                    val sw = row.getChildAt(1) as MaterialSwitch
                    current[label.text.toString().lowercase()] = sw.isChecked
                }
                
                val resp = settingsViewModel.getApiService().setFeatureVisibility(current)
                if (resp.isSuccessful) {
                    Snackbar.make(binding.root, "Feature visibility updated", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Error updating visibility", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    // ── AI MODEL ─────────────────────────────────────────────────────────────

    private fun setupLlmSection() {
        settingsViewModel.modelCatalog.observe(viewLifecycleOwner) { catalog ->
            catalog ?: return@observe
            allModels = catalog.local + catalog.cloud
            providerList.clear()
            providerList.addAll(allModels.map { it.provider }.distinct())
            binding.spinnerProvider.setAdapter(
                ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, providerList)
            )
            binding.spinnerProvider.setOnItemClickListener { _, _, pos, _ ->
                selectedProvider = providerList[pos]
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

        settingsViewModel.isLoading.observe(viewLifecycleOwner) { binding.progressBarSettings.isVisible = it }

        settingsViewModel.saveResult.observe(viewLifecycleOwner) { result ->
            result?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); settingsViewModel.clearSaveResult() }
        }

        settingsViewModel.connectionTestResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                    .setBackgroundTint(requireContext().getColor(android.R.color.holo_green_dark)).show()
                settingsViewModel.clearConnectionTestResult()
            }
        }

        settingsViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                    .setBackgroundTint(requireContext().getColor(android.R.color.holo_red_dark)).show()
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

        binding.buttonTestConnection.setOnClickListener { settingsViewModel.testConnection() }
    }

    private fun updateModelsForProvider(provider: String, preselectModel: String? = null) {
        modelListForProvider.clear()
        modelListForProvider.addAll(allModels.filter { it.provider == provider }.map { it.modelId })
        binding.spinnerModel.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, modelListForProvider)
        )
        if (preselectModel != null && modelListForProvider.contains(preselectModel)) {
            binding.spinnerModel.setText(preselectModel, false)
            selectedModelId = preselectModel
        }
        binding.spinnerModel.setOnItemClickListener { _, _, pos, _ ->
            selectedModelId = modelListForProvider[pos]
        }
    }

    // ── VOICE — card list with ▶ Preview button ───────────────────────────────

    private fun setupVoiceSection() {
        settingsViewModel.voices.observe(viewLifecycleOwner) { voices ->
            rebuildVoiceCards(voices, settingsViewModel.selectedVoice.value)
        }

        settingsViewModel.selectedVoice.observe(viewLifecycleOwner) { selected ->
            rebuildVoiceCards(settingsViewModel.voices.value ?: emptyList(), selected)
        }

        settingsViewModel.voicePreviewData.observe(viewLifecycleOwner) { data ->
            data?.let { playPreview(it); settingsViewModel.clearVoicePreviewData() }
        }
    }

    private fun rebuildVoiceCards(voices: List<VoiceOption>, activeVoice: VoiceOption?) {
        val container = binding.voiceContainer
        container.removeAllViews()

        if (voices.isEmpty()) {
            container.addView(TextView(requireContext()).apply {
                text = "No voices available"
                setTextColor(resolveThemeColor(com.google.android.material.R.attr.colorOnSurfaceVariant))
                textSize = 13f
            })
            return
        }

        val primary          = resolveThemeColor(com.google.android.material.R.attr.colorPrimary)
        val onSurface        = resolveThemeColor(com.google.android.material.R.attr.colorOnSurface)
        val onSurfaceVariant = resolveThemeColor(com.google.android.material.R.attr.colorOnSurfaceVariant)
        val outlineVariant   = resolveThemeColor(com.google.android.material.R.attr.colorOutlineVariant)
        val surfaceVariant   = resolveThemeColor(com.google.android.material.R.attr.colorSurfaceVariant)

        voices.groupBy { it.provider }.forEach { (provider, provVoices) ->

            // Provider group header
            container.addView(TextView(requireContext()).apply {
                text = provider.uppercase()
                setTextColor(primary)
                textSize = 10f
                letterSpacing = 0.10f
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.topMargin = 8.dp; lp.bottomMargin = 4.dp
                layoutParams = lp
            })

            provVoices.forEach { voice ->
                val isActive = activeVoice?.id == voice.id

                val card = MaterialCardView(requireContext()).apply {
                    radius = 8f * resources.displayMetrics.density
                    setCardBackgroundColor(surfaceVariant)
                    strokeWidth = if (isActive) 2.dp else 0
                    strokeColor = primary
                    cardElevation = 0f
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    lp.bottomMargin = 8.dp
                    layoutParams = lp
                    isClickable = true
                    isFocusable = true
                    setOnClickListener { settingsViewModel.selectVoice(voice) }
                }

                val row = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.CENTER_VERTICAL
                    setPadding(12.dp, 12.dp, 12.dp, 12.dp)
                }

                // Name + provider label
                val nameCol = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                nameCol.addView(TextView(requireContext()).apply {
                    text = voice.name
                    setTextColor(if (isActive) primary else onSurface)
                    textSize = 14f
                    if (isActive) setTypeface(null, android.graphics.Typeface.BOLD)
                })
                nameCol.addView(TextView(requireContext()).apply {
                    text = voice.provider
                    setTextColor(onSurfaceVariant)
                    textSize = 11f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also { it.topMargin = 2.dp }
                })
                row.addView(nameCol)

                // Preview button
                val previewBtn = MaterialButton(requireContext(),
                    null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
                    text = if (previewingVoiceId == voice.id) "◉ Playing" else "▶ Preview"
                    textSize = 11f
                    isEnabled = previewingVoiceId == null || previewingVoiceId == voice.id
                    setPadding(10.dp, 0, 10.dp, 0)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also { it.marginStart = 8.dp }
                    setOnClickListener {
                        if (previewingVoiceId == null) {
                            previewingVoiceId = voice.id
                            rebuildVoiceCards(
                                settingsViewModel.voices.value ?: emptyList(),
                                settingsViewModel.selectedVoice.value
                            )
                            settingsViewModel.testVoice(voice.id)
                        }
                    }
                }
                row.addView(previewBtn)
                card.addView(row)
                container.addView(card)
            }
        }
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density + 0.5f).toInt()

    private fun resolveThemeColor(@AttrRes attr: Int): Int {
        val tv = TypedValue()
        requireContext().theme.resolveAttribute(attr, tv, true)
        return tv.data
    }

    // ── MEMORY TTL ────────────────────────────────────────────────────────────

    private fun setupMemoryTtlSection() {
        settingsViewModel.memoryTtl.observe(viewLifecycleOwner) { ttl ->
            ttl?.let {
                when (it.ttl) {
                    "7d"      -> binding.radioTtl7d.isChecked = true
                    "30d"     -> binding.radioTtl30d.isChecked = true
                    "90d"     -> binding.radioTtl90d.isChecked = true
                    "365d"    -> binding.radioTtl1y.isChecked = true
                    "forever" -> binding.radioTtlForever.isChecked = true
                }
                binding.switchAutoExtend.isChecked = it.autoExtend
            }
        }
        binding.buttonSaveMemoryTtl.setOnClickListener {
            val ttl = when (binding.radioGroupMemoryTtl.checkedRadioButtonId) {
                binding.radioTtl7d.id      -> "7d"
                binding.radioTtl30d.id     -> "30d"
                binding.radioTtl90d.id     -> "90d"
                binding.radioTtl1y.id      -> "365d"
                else                       -> "forever"
            }
            settingsViewModel.saveMemoryTtl(MemoryTtlSettings(ttl, binding.switchAutoExtend.isChecked))
        }
    }

    // ── Audio playback ─────────────────────────────────────────────────────────

    private fun playPreview(data: ByteArray) {
        try {
            val tmp = File.createTempFile("voice_preview", "mp3", requireContext().cacheDir)
            FileOutputStream(tmp).use { it.write(data) }
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(tmp.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    tmp.delete()
                    previewingVoiceId = null
                    rebuildVoiceCards(
                        settingsViewModel.voices.value ?: emptyList(),
                        settingsViewModel.selectedVoice.value
                    )
                }
            }
        } catch (e: Exception) {
            previewingVoiceId = null
            Toast.makeText(context, "Error playing preview", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        mediaPlayer = null
        _binding = null
    }
}
