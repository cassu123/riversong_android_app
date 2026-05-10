package com.riversongai.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.riversongai.R
import com.riversongai.data.model.*
import com.riversongai.databinding.*
import com.riversongai.ui.viewmodel.InventoryViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class InventoryFragment : Fragment(R.layout.fragment_inventory) {

    private var _b: FragmentInventoryBinding? = null
    private val b get() = _b!!
    private val vm: InventoryViewModel by viewModel()
    private lateinit var adapter: InventoryItemAdapter
    private var allItems: List<InventoryItem> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentInventoryBinding.bind(view)

        adapter = InventoryItemAdapter(
            onDelete = { vm.deleteItem(it.id) },
            onClick = { showItemDetails(it) }
        )
        b.rvItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@InventoryFragment.adapter
        }

        b.swipeRefresh.setOnRefreshListener { vm.loadHomes() }

        b.etItemSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterItems(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        b.btnAddItem.setOnClickListener {
            b.cardAddItem.isVisible = !b.cardAddItem.isVisible
            if (!b.cardAddItem.isVisible) clearItemForm()
        }
        b.btnCancelItem.setOnClickListener { b.cardAddItem.isVisible = false; clearItemForm() }
        b.btnSaveItem.setOnClickListener { saveItem() }
        
        b.btnAudit.setOnClickListener { showAuditManagement() }

        observeVm()
        vm.loadHomes()
    }

    private fun observeVm() {
        vm.isLoading.observe(viewLifecycleOwner) { b.swipeRefresh.isRefreshing = it }

        vm.homes.observe(viewLifecycleOwner) { homes -> buildHomeChips(homes) }

        vm.selectedHome.observe(viewLifecycleOwner) { home ->
            b.tvSelectedHome.text = home?.name ?: "No home selected"
            b.btnAddItem.isEnabled = home != null
            b.btnAudit.isVisible = home != null
        }

        vm.items.observe(viewLifecycleOwner) { items ->
            allItems = items
            filterItems(b.etItemSearch.text?.toString() ?: "")
            b.tvItemCount.text = "${items.size} item${if (items.size == 1) "" else "s"}"
        }

        vm.activeAudit.observe(viewLifecycleOwner) { audit ->
            b.btnAudit.text = if (audit != null) "Active Audit (${audit.scannedCount}/${audit.totalItems})" else "Start Audit"
        }

        vm.error.observe(viewLifecycleOwner) { err ->
            err?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show(); vm.clearError() }
        }

        vm.toast.observe(viewLifecycleOwner) { msg ->
            msg?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); vm.clearToast() }
        }
    }

    private fun buildHomeChips(homes: List<InventoryHome>) {
        b.layoutHomes.removeAllViews()

        // + Add Home chip
        val addChip = Chip(requireContext()).apply {
            text = "+ Add Home"
            isCheckable = false
            setOnClickListener { showAddHomeDialog() }
        }
        b.layoutHomes.addView(addChip)

        homes.forEach { home ->
            val chip = Chip(requireContext()).apply {
                text = "${home.name} (${home.itemCount})"
                isCheckable = true
                isChecked = vm.selectedHome.value?.id == home.id
                setOnClickListener { vm.selectHome(home) }
            }
            b.layoutHomes.addView(chip)
        }
    }

    private fun showAddHomeDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val layout = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }
        val etName = com.google.android.material.textfield.TextInputEditText(requireContext()).apply { hint = "Home Name" }
        val etDesc = com.google.android.material.textfield.TextInputEditText(requireContext()).apply { hint = "Description" }
        val btnSave = com.google.android.material.button.MaterialButton(requireContext()).apply {
            text = "Create"
            setOnClickListener {
                val name = etName.text.toString().trim()
                if (name.isNotBlank()) vm.createHome(name)
                dialog.dismiss()
            }
        }
        layout.addView(etName); layout.addView(etDesc); layout.addView(btnSave)
        dialog.setContentView(layout); dialog.show()
    }

    private fun filterItems(query: String) {
        val filtered = if (query.isBlank()) allItems
        else allItems.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true) ||
            it.location.contains(query, ignoreCase = true)
        }
        adapter.submitList(filtered)
    }

    private fun saveItem() {
        val name = b.etNewItemName.text?.toString()?.trim() ?: ""
        if (name.isBlank()) { b.etNewItemName.error = "Required"; return }
        val body = CreateInventoryItem(
            name = name,
            category = b.etNewItemCategory.text?.toString()?.trim()?.ifBlank { "Other" } ?: "Other",
            location = b.etNewItemLocation.text?.toString()?.trim() ?: "",
            replacementCost = b.etNewItemCost.text?.toString()?.toDoubleOrNull(),
        )
        vm.createItem(body)
        b.cardAddItem.isVisible = false
        clearItemForm()
    }

    private fun showItemDetails(item: InventoryItem) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = LayoutDeviceDetailBottomSheetBinding.inflate(layoutInflater) // Placeholder layout, need a specific one or reuse
        // For now, let's just use a simple dynamic layout or an alert
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(item.name)
            .setMessage("Category: ${item.category}\nLocation: ${item.location}\nStatus: ${item.assetStatus}\nEIN: ${item.ein}")
            .setPositiveButton("Edit") { _, _ -> /* TODO: show edit form */ }
            .setNeutralButton("Issue") { _, _ -> showIssueDialog(item) }
            .setNegativeButton("Delete") { _, _ -> vm.deleteItem(item.id) }
            .show()
    }

    private fun showIssueDialog(item: InventoryItem) {
        val etEmail = android.widget.EditText(requireContext()).apply { hint = "Collaborator Email" }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Issue Item")
            .setView(etEmail)
            .setPositiveButton("Issue") { _, _ -> vm.issueItem(item.id, etEmail.text.toString()) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAuditManagement() {
        val audit = vm.activeAudit.value
        if (audit == null) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Start Audit")
                .setMessage("Begin scanning items in ${vm.selectedHome.value?.name}?")
                .setPositiveButton("Start") { _, _ -> vm.startAudit() }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Active Audit")
                .setMessage("Progress: ${audit.scannedCount}/${audit.totalItems}\nStatus: ${audit.status}")
                .setPositiveButton("Continue Scanning") { _, _ -> /* TODO: open scanner */ }
                .setNegativeButton("Close", null)
                .show()
        }
    }

    private fun clearItemForm() {
        b.etNewItemName.text?.clear()
        b.etNewItemCategory.text?.clear()
        b.etNewItemLocation.text?.clear()
        b.etNewItemCost.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}

// ── Item adapter ─────────────────────────────────────────────────────────────

class InventoryItemAdapter(
    private val onDelete: (InventoryItem) -> Unit,
    private val onClick: (InventoryItem) -> Unit
) : ListAdapter<InventoryItem, InventoryItemAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_inventory_item, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView     = view.findViewById(R.id.tvItemName)
        private val tvMeta: TextView     = view.findViewById(R.id.tvItemMeta)
        private val tvStatus: TextView   = view.findViewById(R.id.tvItemStatus)

        fun bind(item: InventoryItem) {
            tvName.text = item.name
            tvMeta.text = "${item.category} • ${item.location}"
            tvStatus.text = item.assetStatus
            itemView.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<InventoryItem>() {
            override fun areItemsTheSame(a: InventoryItem, b: InventoryItem) = a.id == b.id
            override fun areContentsTheSame(a: InventoryItem, b: InventoryItem) = a == b
        }
    }
}
