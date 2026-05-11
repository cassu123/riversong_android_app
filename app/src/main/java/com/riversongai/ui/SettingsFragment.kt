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
import com.riversongai.data.model.*
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
    private var selectedProvider: String = ""
    private var selectedModelId: String = ""
    private var modelFilter = "ALL"

    private var selectedFallbackProvider: String = ""
    private var selectedFallbackModelId: String = ""

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
        setupCloudFallbackSection()
        setupN8nSection()
        setupVoiceSection()
        setupMemoryTtlSection()
        setupAdminControls()
        settingsViewModel.loadSettings()
    }

    private fun setupLlmSection() {
        binding.chipGroupModelFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            modelFilter = when (checkedIds.firstOrNull()) {
                R.id.chipFilterLocal -> "LOCAL"
                R.id.chipFilterCloud -> "CLOUD"
                else -> "ALL"
            }
            updateProviderList()
        }

        settingsViewModel.modelCatalog.observe(viewLifecycleOwner) { catalog ->
            catalog ?: return@observe
            allModels = catalog.local + catalog.cloud
            updateProviderList()
        }

        settingsViewModel.llmSettings.observe(viewLifecycleOwner) { settings ->
            settings ?: return@observe
            val currentModelEntry = allModels.find { it.modelId == settings.model }
            binding.textViewCurrentModel.text = "Current: ${currentModelEntry?.displayName ?: settings.model}"
            selectedProvider = settings.provider
            selectedModelId = settings.model
            binding.spinnerProvider.setText(settings.provider, false)
            updateModelsForProvider(settings.provider, preselectModel = settings.model)
        }

        binding.buttonSaveModel.setOnClickListener {
            if (selectedProvider.isBlank() || selectedModelId.isBlank()) {
                Toast.makeText(context, "Select a model", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            settingsViewModel.saveModel(selectedProvider, selectedModelId)
        }
        binding.buttonTestConnection.setOnClickListener { settingsViewModel.testConnection() }
    }

    private fun updateProviderList() {
        val filtered = when (modelFilter) {
            "LOCAL" -> allModels.filter { it.isCloud == false }
            "CLOUD" -> allModels.filter { it.isCloud == true }
            else -> allModels
        }
        providerList.clear()
        providerList.addAll(filtered.map { it.provider }.distinct())
        binding.spinnerProvider.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, providerList))
        
        if (selectedProvider.isNotEmpty() && !providerList.contains(selectedProvider)) {
            binding.spinnerProvider.setText("", false)
            binding.spinnerModel.setText("", false)
            selectedProvider = ""
            selectedModelId = ""
        }
    }

    private fun updateModelsForProvider(provider: String, preselectModel: String? = null) {
        val filteredModels = allModels.filter { it.provider == provider }
        val adapter = object : ArrayAdapter<ModelEntry>(requireContext(), R.layout.item_spinner_model, filteredModels) {
            override fun getView(pos: Int, conv: View?, parent: ViewGroup): View {
                val v = conv ?: LayoutInflater.from(context).inflate(R.layout.item_spinner_model, parent, false)
                val m = getItem(pos)
                v.findViewById<TextView>(R.id.text1).text = m?.displayName
                val sub = buildString {
                    if (m?.vramGb != null) append("${m.vramGb}GB VRAM")
                    else if (m?.sizeMb != null) append("${m.sizeMb}MB")
                    if (m?.costInputUsd != null) append(" • \$${m.costInputUsd}/1k")
                }
                v.findViewById<TextView>(R.id.text2).apply { text = sub; isVisible = sub.isNotEmpty() }
                return v
            }
            override fun getDropDownView(p: Int, c: View?, parent: ViewGroup): View = getView(p, c, parent)
        }
        binding.spinnerModel.setAdapter(adapter)
        if (preselectModel != null) {
            filteredModels.find { it.modelId == preselectModel }?.let { binding.spinnerModel.setText(it.displayName, false); selectedModelId = it.modelId }
        }
        binding.spinnerModel.setOnItemClickListener { _, _, pos, _ -> selectedModelId = adapter.getItem(pos)?.modelId ?: "" }
    }

    private fun setupCloudFallbackSection() {
        binding.switchCloudFallback.setOnCheckedChangeListener { _, isChecked ->
            val v = if (isChecked) View.VISIBLE else View.GONE
            binding.layoutFallbackProvider.visibility = v
            binding.layoutFallbackModel.visibility = v
            binding.buttonSaveCloudFallback.visibility = v
        }
        settingsViewModel.modelCatalog.observe(viewLifecycleOwner) { catalog ->
            catalog ?: return@observe
            val cloudProviders = catalog.cloud.map { it.provider }.distinct()
            binding.spinnerFallbackProvider.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cloudProviders))
        }
        settingsViewModel.llmSettings.observe(viewLifecycleOwner) { s ->
            s ?: return@observe
            binding.switchCloudFallback.isChecked = s.cloudFallbackEnabled
            s.cloudFallbackProvider?.let { binding.spinnerFallbackProvider.setText(it, false); updateFallbackModels(it, s.cloudFallbackModel) }
        }
    }

    private fun updateFallbackModels(provider: String, preselect: String?) {
        val filtered = allModels.filter { it.provider == provider && it.isCloud == true }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, filtered.map { it.displayName })
        binding.spinnerFallbackModel.setAdapter(adapter)
        preselect?.let { id -> filtered.find { it.modelId == id }?.let { binding.spinnerFallbackModel.setText(it.displayName, false); selectedFallbackModelId = it.modelId } }
    }

    private fun setupAdminControls() {
        val isAdmin = requireContext().getSharedPreferences("rs_prefs", Context.MODE_PRIVATE).getBoolean("is_admin", false)
        if (!isAdmin) return
        binding.cardAdminControls.isVisible = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                settingsViewModel.getApiService().getFeatureVisibility().body()?.let { populateVisibilityToggles(it) }
                loadModelVisibility()
            } catch (e: Exception) {}
        }
    }

    private fun loadModelVisibility() {
        viewLifecycleOwner.lifecycleScope.launch {
            settingsViewModel.getApiService().getModelVisibility().body()?.let { data ->
                populateModelToggles(binding.layoutModelVisibility, data.allLlms, { it.modelId ?: "" }, data.hiddenLlms.toMutableSet(), true)
                populateModelToggles(binding.layoutVoiceVisibility, data.allVoices, { it.voiceId ?: "" }, data.hiddenVoices.toMutableSet(), false)
            }
        }
    }

    private fun populateModelToggles(container: LinearLayout, items: List<ModelVisibilityItem>, idOf: (ModelVisibilityItem) -> String, hidden: MutableSet<String>, isLlm: Boolean) {
        container.removeAllViews()
        items.forEach { item ->
            val id = idOf(item)
            val row = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL; gravity = android.view.Gravity.CENTER_VERTICAL; setPadding(0, 6.dp, 0, 6.dp) }
            val label = TextView(requireContext()).apply { layoutParams = LinearLayout.LayoutParams(0, -2, 1f); text = item.displayName; tag = id; setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium) }
            val switch = MaterialSwitch(requireContext()).apply { isChecked = !hidden.contains(id); setOnCheckedChangeListener { _, ok -> if (ok) hidden.remove(id) else hidden.add(id); saveModelVisibility() } }
            row.addView(label); row.addView(switch); container.addView(row)
        }
    }

    private fun saveModelVisibility() {
        viewLifecycleOwner.lifecycleScope.launch {
            val update = ModelVisibilityUpdate(collectHiddenIds(binding.layoutModelVisibility), collectHiddenIds(binding.layoutVoiceVisibility))
            if (settingsViewModel.getApiService().setModelVisibility(update).isSuccessful) Snackbar.make(binding.root, "Visibility updated", 2000).show()
        }
    }

    private fun collectHiddenIds(container: LinearLayout): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until container.childCount) {
            val row = container.getChildAt(i) as? LinearLayout ?: continue
            if (!(row.getChildAt(1) as MaterialSwitch).isChecked) (row.getChildAt(0).tag as? String)?.let { list.add(it) }
        }
        return list
    }

    private fun populateVisibilityToggles(map: Map<String, Any?>) {
        binding.layoutFeatureVisibility.removeAllViews()
        map.keys.sorted().forEach { feature ->
            val row = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL; gravity = android.view.Gravity.CENTER_VERTICAL; setPadding(0, 8.dp, 0, 8.dp) }
            val label = TextView(requireContext()).apply { layoutParams = LinearLayout.LayoutParams(0, -2, 1f); text = feature.uppercase(); setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium) }
            val switch = MaterialSwitch(requireContext()).apply { isChecked = map[feature] == true; setOnCheckedChangeListener { _, ok -> updateFeatureVisibility(feature, ok) } }
            row.addView(label); row.addView(switch); binding.layoutFeatureVisibility.addView(row)
        }
    }

    private fun updateFeatureVisibility(feature: String, ok: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            val current = mutableMapOf<String, Any?>()
            for (i in 0 until binding.layoutFeatureVisibility.childCount) {
                val row = binding.layoutFeatureVisibility.getChildAt(i) as LinearLayout
                current[(row.getChildAt(0) as TextView).text.toString().lowercase()] = (row.getChildAt(1) as MaterialSwitch).isChecked
            }
            settingsViewModel.getApiService().setFeatureVisibility(current)
        }
    }

    private fun setupN8nSection() {
        val isAdmin = requireContext().getSharedPreferences("rs_prefs", 0).getBoolean("is_admin", false)
        binding.labelOrchestration.isVisible = isAdmin; binding.cardOrchestration.isVisible = isAdmin
        if (!isAdmin) return
        binding.switchN8nEnabled.setOnCheckedChangeListener { _, ok ->
            val v = if (ok) View.VISIBLE else View.GONE
            binding.layoutN8nUrl.visibility = v; binding.layoutN8nApiKey.visibility = v; binding.layoutN8nWebhookSecret.visibility = v; binding.textViewN8nHelper.visibility = v; binding.buttonSaveN8n.visibility = v
        }
        settingsViewModel.n8nSettings.observe(viewLifecycleOwner) { s ->
            s ?: return@observe
            binding.switchN8nEnabled.isChecked = s.enabled
            binding.editTextN8nUrl.setText(s.url); binding.editTextN8nApiKey.setText(s.apiKey); binding.editTextN8nWebhookSecret.setText(s.webhookSecret)
        }
        binding.buttonSaveN8n.setOnClickListener {
            val s = N8nSettings(binding.switchN8nEnabled.isChecked, binding.editTextN8nUrl.text.toString(), binding.editTextN8nApiKey.text.toString(), binding.editTextN8nWebhookSecret.text.toString())
            settingsViewModel.saveN8nSettings(s)
        }
    }

    private fun setupVoiceSection() {
        settingsViewModel.voices.observe(viewLifecycleOwner) { rebuildVoiceCards(it, settingsViewModel.selectedVoice.value) }
        settingsViewModel.selectedVoice.observe(viewLifecycleOwner) { rebuildVoiceCards(settingsViewModel.voices.value.orEmpty(), it) }
        settingsViewModel.voicePreviewData.observe(viewLifecycleOwner) { it?.let { playPreview(it); settingsViewModel.clearVoicePreviewData() } }
    }

    private fun rebuildVoiceCards(voices: List<VoiceOption>, active: VoiceOption?) {
        binding.voiceContainer.removeAllViews()
        voices.groupBy { it.provider }.forEach { (prov, list) ->
            binding.voiceContainer.addView(TextView(requireContext()).apply { text = prov.uppercase(); setTextColor(resolveThemeColor(com.google.android.material.R.attr.colorPrimary)); textSize = 10f; setPadding(0, 8.dp, 0, 4.dp) })
            list.forEach { v ->
                val ok = active?.id == v.id
                val card = MaterialCardView(requireContext()).apply { radius = 8.dp.toFloat(); setCardBackgroundColor(resolveThemeColor(com.google.android.material.R.attr.colorSurfaceVariant)); strokeWidth = if (ok) 2.dp else 0; strokeColor = resolveThemeColor(com.google.android.material.R.attr.colorPrimary); setOnClickListener { settingsViewModel.selectVoice(v) } }
                val row = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL; gravity = 16; setPadding(12.dp, 12.dp, 12.dp, 12.dp) }
                val col = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, -2, 1f) }
                col.addView(TextView(requireContext()).apply { text = v.name; textSize = 14f; if (ok) setTypeface(null, 1) })
                col.addView(TextView(requireContext()).apply { text = v.provider; textSize = 11f; setTextColor(resolveThemeColor(com.google.android.material.R.attr.colorOnSurfaceVariant)) })
                row.addView(col)
                row.addView(MaterialButton(requireContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply { text = if (previewingVoiceId == v.id) "◉" else "▶"; setOnClickListener { previewingVoiceId = v.id; settingsViewModel.testVoice(v.id) } })
                card.addView(row); binding.voiceContainer.addView(card)
            }
        }
    }

    private fun setupMemoryTtlSection() {
        settingsViewModel.memoryTtl.observe(viewLifecycleOwner) { s ->
            s ?: return@observe
            binding.switchSummariesEnabled.isChecked = s.summariesEnabled
            binding.switchAutoExtend.isChecked = s.autoExtend
            when (s.ttl) { "7d" -> binding.radioTtl7d.isChecked = true; "30d" -> binding.radioTtl30d.isChecked = true; "90d" -> binding.radioTtl90d.isChecked = true; "365d" -> binding.radioTtl1y.isChecked = true; else -> binding.radioTtlForever.isChecked = true }
        }
        binding.buttonSaveMemoryTtl.setOnClickListener {
            val ttl = when (binding.radioGroupMemoryTtl.checkedRadioButtonId) { binding.radioTtl7d.id -> "7d"; binding.radioTtl30d.id -> "30d"; binding.radioTtl90d.id -> "90d"; binding.radioTtl1y.id -> "365d"; else -> "forever" }
            settingsViewModel.saveMemoryTtl(MemoryTtlSettings(binding.switchSummariesEnabled.isChecked, ttl, binding.switchAutoExtend.isChecked))
        }
    }

    private fun playPreview(data: ByteArray) {
        try {
            val tmp = File.createTempFile("voice", "mp3", requireContext().cacheDir)
            FileOutputStream(tmp).use { it.write(data) }
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply { setDataSource(tmp.absolutePath); prepare(); start(); setOnCompletionListener { tmp.delete(); previewingVoiceId = null; rebuildVoiceCards(settingsViewModel.voices.value.orEmpty(), settingsViewModel.selectedVoice.value) } }
        } catch (e: Exception) { previewingVoiceId = null }
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density + 0.5f).toInt()
    private fun resolveThemeColor(@AttrRes attr: Int): Int { val tv = TypedValue(); requireContext().theme.resolveAttribute(attr, tv, true); return tv.data }
    override fun onDestroyView() { super.onDestroyView(); mediaPlayer?.release(); _binding = null }
}
