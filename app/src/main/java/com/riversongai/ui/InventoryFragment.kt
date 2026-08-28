package com.riversongai.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
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
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(item.name)
            .setMessage("Category: ${item.category}\nLocation: ${item.location}\nStatus: ${item.assetStatus}\nEIN: ${item.ein}")
            .setPositiveButton("Edit") { _, _ -> showEditItemDialog(item) }
            .setNeutralButton("Issue") { _, _ -> showIssueDialog(item) }
            .setNegativeButton("Delete") { _, _ -> vm.deleteItem(item.id) }
            .show()
    }

    private fun showEditItemDialog(item: InventoryItem) {
        val ctx = requireContext()
        val density = resources.displayMetrics.density
        val pad = (24 * density).toInt()
        fun fieldParams() = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = (4 * density).toInt() }

        fun input(hint: String, value: String, type: Int = android.text.InputType.TYPE_CLASS_TEXT) =
            com.google.android.material.textfield.TextInputEditText(ctx).apply {
                this.hint = hint
                setText(value)
                inputType = type
                layoutParams = fieldParams()
            }

        val etName = input("Item Name", item.name)
        val etCategory = input("Category", item.category)
        val etLocation = input("Location / Room", item.location)
        val etQuantity = input("Quantity", item.quantity.toString(), android.text.InputType.TYPE_CLASS_NUMBER)
        val etCost = input(
            "Replacement Cost ($)", item.replacementCost?.toString().orEmpty(),
            android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        )
        val etManufacturer = input("Manufacturer", item.manufacturer)
        val etModelNumber = input("Model Number", item.modelNumber)
        val etSerialNumber = input("Serial Number", item.serialNumber)
        val etDescription = input("Description", item.description)

        val statusOptions = listOf("Serviceable", "Unserviceable", "Missing", "In-Use")
        val spinnerStatus = android.widget.Spinner(ctx).apply {
            adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, statusOptions)
            setSelection(statusOptions.indexOf(item.assetStatus).coerceAtLeast(0))
            layoutParams = fieldParams()
        }

        val layout = android.widget.LinearLayout(ctx).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(pad, pad / 2, pad, 0)
            listOf(
                etName, etCategory, etLocation, etQuantity, etCost,
                etManufacturer, etModelNumber, etSerialNumber, etDescription, spinnerStatus
            ).forEach { addView(it) }
        }

        MaterialAlertDialogBuilder(ctx)
            .setTitle("Edit Item")
            .setView(android.widget.ScrollView(ctx).apply { addView(layout) })
            .setPositiveButton("Save") { _, _ ->
                vm.updateItem(
                    item.id, mapOf(
                        "name" to etName.text.toString().trim(),
                        "category" to etCategory.text.toString().trim().ifBlank { "Other" },
                        "location" to etLocation.text.toString().trim(),
                        "quantity" to (etQuantity.text.toString().toIntOrNull() ?: item.quantity),
                        "replacement_cost" to etCost.text.toString().toDoubleOrNull(),
                        "manufacturer" to etManufacturer.text.toString().trim(),
                        "model_number" to etModelNumber.text.toString().trim(),
                        "serial_number" to etSerialNumber.text.toString().trim(),
                        "description" to etDescription.text.toString().trim(),
                        "asset_status" to statusOptions[spinnerStatus.selectedItemPosition]
                    )
                )
            }
            .setNegativeButton("Cancel", null)
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
            fun itemLabel(map: Map<String, String>) =
                map["name"] ?: map["item_name"] ?: map["ein"] ?: "Unnamed item"

            val message = buildString {
                append("Progress: ${audit.scannedCount}/${audit.totalItems}\n")
                append("Status: ${audit.status}")
                if (audit.missing.isNotEmpty()) {
                    append("\n\nNot yet scanned:")
                    audit.missing.take(10).forEach { append("\n• ${itemLabel(it)}") }
                    if (audit.missing.size > 10) append("\n…and ${audit.missing.size - 10} more")
                } else if (audit.totalItems > 0) {
                    append("\n\nAll items scanned!")
                }
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Active Audit")
                .setMessage(message)
                .setPositiveButton("Close", null)
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
        private val tvCat: TextView      = view.findViewById(R.id.tvItemCategory)
        private val tvLoc: TextView      = view.findViewById(R.id.tvItemLocation)
        private val tvCost: TextView     = view.findViewById(R.id.tvItemCost)
        private val tvStatus: TextView   = view.findViewById(R.id.tvItemStatus)
        private val btnDelete: View      = view.findViewById(R.id.btnDeleteItem)

        fun bind(item: InventoryItem) {
            tvName.text = item.name
            tvCat.text = item.category
            tvLoc.text = if (item.location.isNotBlank()) "  ·  ${item.location}" else ""

            tvCost.isVisible = item.replacementCost != null && item.replacementCost > 0
            if (tvCost.isVisible) {
                tvCost.text = "Repl. value: $%.2f".format(item.replacementCost)
            }

            val (label, bg, fg) = when (item.assetStatus) {
                "Serviceable"   -> Triple("SERVICEABLE",   "#1A3A1A", "#3DCC79")
                "Unserviceable" -> Triple("UNSERVICEABLE", "#3A1A1A", "#FFB4AB")
                "Missing"       -> Triple("MISSING",       "#3A2A0A", "#FFB86C")
                "In-Use"        -> Triple("IN USE",        "#1A2A3A", "#96CBFF")
                else            -> Triple(item.assetStatus.uppercase(), "#2A2A2A", "#BFC8CE")
            }
            tvStatus.text = label
            tvStatus.setTextColor(android.graphics.Color.parseColor(fg))
            tvStatus.background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 4f * itemView.resources.displayMetrics.density
                setColor(android.graphics.Color.parseColor(bg))
            }

            itemView.setOnClickListener { onClick(item) }
            btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<InventoryItem>() {
            override fun areItemsTheSame(a: InventoryItem, b: InventoryItem) = a.id == b.id
            override fun areContentsTheSame(a: InventoryItem, b: InventoryItem) = a == b
        }
    }
}
