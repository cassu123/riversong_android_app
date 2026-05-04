package com.riversongai.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.riversongai.R
import com.riversongai.data.model.CreateInventoryItem
import com.riversongai.data.model.InventoryHome
import com.riversongai.data.model.InventoryItem
import com.riversongai.databinding.FragmentInventoryBinding
import com.riversongai.ui.adapter.InventoryItemAdapter
import com.riversongai.ui.viewmodel.InventoryViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class InventoryFragment : Fragment(R.layout.fragment_inventory) {

    private var _b: FragmentInventoryBinding? = null
    private val b get() = _b!!
    private val vm: InventoryViewModel by viewModel()
    private lateinit var adapter: InventoryItemAdapter
    private var allItems: List<InventoryItem> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentInventoryBinding.bind(view)

        adapter = InventoryItemAdapter(onDelete = { vm.deleteItem(it.id) })
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

        observeVm()
        vm.loadHomes()
    }

    private fun observeVm() {
        vm.isLoading.observe(viewLifecycleOwner) { b.swipeRefresh.isRefreshing = it }

        vm.homes.observe(viewLifecycleOwner) { homes -> buildHomeChips(homes) }

        vm.selectedHome.observe(viewLifecycleOwner) { home ->
            b.tvSelectedHome.text = home?.name ?: "No home selected"
            b.btnAddItem.isEnabled = home != null
        }

        vm.items.observe(viewLifecycleOwner) { items ->
            allItems = items
            filterItems(b.etItemSearch.text?.toString() ?: "")
            b.tvItemCount.text = "${items.size} item${if (items.size == 1) "" else "s"}"
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
        val input = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("New Home")
        val editText = android.widget.EditText(requireContext()).apply {
            hint = "Home name"
            setPadding(48, 24, 48, 24)
        }
        input.setView(editText)
            .setPositiveButton("Create") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotBlank()) vm.createHome(name)
            }
            .setNegativeButton("Cancel", null)
            .show()
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
