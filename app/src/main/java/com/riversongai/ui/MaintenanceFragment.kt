package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayoutMediator
import com.riversongai.R
import com.riversongai.data.model.CreateServiceLog
import com.riversongai.data.model.CreateVehicle
import com.riversongai.data.model.ServiceLog
import com.riversongai.data.model.Vehicle
import com.riversongai.databinding.FragmentMaintenanceBinding
import com.riversongai.ui.viewmodel.MaintenanceViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MaintenanceFragment : Fragment(R.layout.fragment_maintenance) {

    private var _b: FragmentMaintenanceBinding? = null
    private val b get() = _b!!
    private val vm: MaintenanceViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentMaintenanceBinding.bind(view)

        // ViewPager tabs: History | + Log Service
        b.pagerMaintenance.adapter = MaintenancePagerAdapter(this)
        TabLayoutMediator(b.tabsMaintenance, b.pagerMaintenance) { tab, pos ->
            tab.text = if (pos == 0) "Service History" else "Log Service"
        }.attach()

        b.btnCancelVehicle.setOnClickListener {
            b.cardAddVehicle.isVisible = false
            clearVehicleForm()
        }
        b.btnSaveVehicle.setOnClickListener { saveVehicle() }

        observeVm()
        vm.loadVehicles()
    }

    private fun observeVm() {
        vm.vehicles.observe(viewLifecycleOwner) { buildVehicleChips(it) }
        vm.error.observe(viewLifecycleOwner) { err ->
            err?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show(); vm.clearError() }
        }
        vm.toast.observe(viewLifecycleOwner) { msg ->
            msg?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); vm.clearToast() }
        }
    }

    private fun buildVehicleChips(vehicles: List<Vehicle>) {
        b.layoutVehicleChips.removeAllViews()

        val addChip = Chip(requireContext()).apply {
            text = "+ Add Vehicle"
            isCheckable = false
            setOnClickListener { b.cardAddVehicle.isVisible = !b.cardAddVehicle.isVisible }
        }
        b.layoutVehicleChips.addView(addChip)

        vehicles.forEach { v ->
            val label = if (v.nickname.isNotBlank()) v.nickname else "${v.year} ${v.make} ${v.model}"
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isChecked = vm.selectedVehicle.value?.id == v.id
                setOnClickListener { vm.selectVehicle(v) }
                setOnLongClickListener { 
                    com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete Vehicle?")
                        .setMessage("This will permanently delete $label and all its service logs.")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete") { _, _ -> vm.deleteVehicle(v.id) }
                        .show()
                    true 
                }
            }
            b.layoutVehicleChips.addView(chip)
        }
    }

    private fun saveVehicle() {
        val year  = b.etVehicleYear.text?.toString()?.trim()?.toIntOrNull()
        val make  = b.etVehicleMake.text?.toString()?.trim() ?: ""
        val model = b.etVehicleModel.text?.toString()?.trim() ?: ""
        if (year == null) { b.etVehicleYear.error  = "Required"; return }
        if (make.isBlank())  { b.etVehicleMake.error  = "Required"; return }
        if (model.isBlank()) { b.etVehicleModel.error = "Required"; return }
        vm.createVehicle(CreateVehicle(
            make = make, model = model, year = year,
            nickname = b.etVehicleNickname.text?.toString()?.trim() ?: ""
        ))
        b.cardAddVehicle.isVisible = false
        clearVehicleForm()
    }

    private fun clearVehicleForm() {
        b.etVehicleYear.text?.clear(); b.etVehicleMake.text?.clear()
        b.etVehicleModel.text?.clear(); b.etVehicleNickname.text?.clear()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }

    // ── Pager adapter ───────────────────────────────────────────────────────
    inner class MaintenancePagerAdapter(frag: Fragment) : FragmentStateAdapter(frag) {
        override fun getItemCount() = 2
        override fun createFragment(position: Int) =
            if (position == 0) ServiceHistoryFragment(vm) else LogServiceFragment(vm)
    }
}

// ── Service History tab ─────────────────────────────────────────────────────

class ServiceHistoryFragment(private val vm: MaintenanceViewModel) : Fragment() {

    private lateinit var adapter: ServiceLogAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rv = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(requireContext())
            setPadding(0, 8.dp, 0, 80.dp)
            clipToPadding = false
        }
        adapter = ServiceLogAdapter()
        rv.adapter = adapter
        return rv
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.serviceLogs.observe(viewLifecycleOwner) { adapter.submitList(it) }
    }

    private val Int.dp get() = (this * resources.displayMetrics.density + 0.5f).toInt()
}

// ── Log Service tab ─────────────────────────────────────────────────────────

class LogServiceFragment(private val vm: MaintenanceViewModel) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val ctx = requireContext()
        val dm  = ctx.resources.displayMetrics
        fun Int.dp() = (this * dm.density + 0.5f).toInt()

        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16.dp(), 16.dp(), 16.dp(), 80.dp())
        }

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        val etDate = com.google.android.material.textfield.TextInputEditText(ctx).apply { setText(today) }
        val etOdo  = com.google.android.material.textfield.TextInputEditText(ctx).apply { inputType = android.text.InputType.TYPE_CLASS_NUMBER }
        val etType = com.google.android.material.textfield.TextInputEditText(ctx).apply { setText("General Service") }

        val wrapDate = com.google.android.material.textfield.TextInputLayout(ctx, null, com.google.android.material.R.attr.textInputOutlinedStyle).apply { hint = "Service Date (YYYY-MM-DD)"; addView(etDate) }
        val wrapOdo  = com.google.android.material.textfield.TextInputLayout(ctx, null, com.google.android.material.R.attr.textInputOutlinedStyle).apply { hint = "Odometer (miles)"; addView(etOdo) }
        val wrapType = com.google.android.material.textfield.TextInputLayout(ctx, null, com.google.android.material.R.attr.textInputOutlinedStyle).apply { hint = "Service Type"; addView(etType) }

        listOf(wrapDate, wrapOdo, wrapType).forEach { wrap ->
            wrap.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.bottomMargin = 8.dp() }
            layout.addView(wrap)
        }

        val btnLog = com.google.android.material.button.MaterialButton(ctx).apply {
            text = ">> Commit Log Entry"
            setOnClickListener {
                val date = etDate.text?.toString()?.trim() ?: today
                val odo  = etOdo.text?.toString()?.toIntOrNull()
                val type = etType.text?.toString()?.trim()?.ifBlank { "General Service" } ?: "General Service"
                vm.logService(CreateServiceLog(serviceDate = date, odometer = odo, serviceType = type))
            }
        }
        layout.addView(btnLog)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.selectedVehicle.observe(viewLifecycleOwner) {
            view.alpha = if (it != null) 1f else 0.5f
        }
    }
}

// ── Service Log RecyclerView adapter ───────────────────────────────────────

class ServiceLogAdapter : ListAdapter<ServiceLog, ServiceLogAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_service_log, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvDate: TextView    = view.findViewById(R.id.tvLogDate)
        private val tvType: TextView    = view.findViewById(R.id.tvLogType)
        private val tvOdo: TextView     = view.findViewById(R.id.tvLogOdometer)
        private val tvPro: TextView     = view.findViewById(R.id.tvLogProTag)

        fun bind(log: ServiceLog) {
            tvDate.text = log.serviceDate
            tvType.text = log.serviceType.ifBlank { "Service" }
            tvOdo.text  = if (log.odometer != null) "${log.odometer.toFormattedMiles()} miles" else ""
            tvPro.text  = if (log.isProService) "PRO" else "DIY"
        }

        private fun Int.toFormattedMiles() = "%,d".format(this)
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ServiceLog>() {
            override fun areItemsTheSame(a: ServiceLog, b: ServiceLog) = a.id == b.id
            override fun areContentsTheSame(a: ServiceLog, b: ServiceLog) = a == b
        }
    }
}
