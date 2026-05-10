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
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayoutMediator
import com.riversongai.R
import com.riversongai.data.model.*
import com.riversongai.databinding.*
import com.riversongai.ui.viewmodel.CommerceViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class StoreFragment : Fragment(R.layout.fragment_store) {

    private var _b: FragmentStoreBinding? = null
    private val b get() = _b!!
    private val vm: CommerceViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentStoreBinding.bind(view)

        b.viewPagerStore.adapter = StorePagerAdapter(this)
        TabLayoutMediator(b.tabLayoutStore, b.viewPagerStore) { tab, pos ->
            tab.text = when (pos) {
                0 -> "Products"
                1 -> "Suppliers"
                2 -> "Customers"
                3 -> "Sales"
                else -> "Members"
            }
        }.attach()

        b.btnCreateWorkspace.setOnClickListener {
            val name = b.etWorkspaceName.text?.toString()?.trim() ?: ""
            if (name.isNotEmpty()) vm.createWorkspace(name)
        }

        observeVm()
        vm.loadWorkspaces()
    }

    private fun observeVm() {
        vm.isLoading.observe(viewLifecycleOwner) { /* handled by sub-fragments */ }
        
        vm.workspaces.observe(viewLifecycleOwner) { list -> buildWorkspaceChips(list) }
        
        vm.selectedWorkspace.observe(viewLifecycleOwner) { ws ->
            val hasWs = ws != null
            b.layoutNoWorkspace.isVisible = !hasWs
            b.layoutStoreContent.isVisible = hasWs
            if (ws != null) b.tvWorkspaceName.text = ws.name
        }

        vm.error.observe(viewLifecycleOwner) { it?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show(); vm.clearError() } }
        vm.toast.observe(viewLifecycleOwner) { it?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); vm.clearToast() } }
    }

    private fun buildWorkspaceChips(list: List<CommerceWorkspace>) {
        b.chipGroupWorkspaces.removeAllViews()
        list.forEach { ws ->
            val chip = Chip(requireContext()).apply {
                text = ws.name
                isCheckable = true
                isChecked = vm.selectedWorkspace.value?.id == ws.id
                setOnClickListener { vm.selectWorkspace(ws) }
            }
            b.chipGroupWorkspaces.addView(chip)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }

    inner class StorePagerAdapter(frag: Fragment) : FragmentStateAdapter(frag) {
        override fun getItemCount() = 5
        override fun createFragment(position: Int) = when (position) {
            0 -> StoreProductsFragment(vm)
            1 -> StoreSuppliersFragment(vm)
            2 -> StoreCustomersFragment(vm)
            3 -> StoreSalesFragment(vm)
            else -> StoreMembersFragment(vm)
        }
    }
}

// ── Sub-Fragments ────────────────────────────────────────────────────────────

class StoreProductsFragment(private val vm: CommerceViewModel) : Fragment(R.layout.layout_store_products) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = LayoutStoreProductsBinding.bind(view)
        val adapter = ProductAdapter()
        b.rvProducts.adapter = adapter
        b.rvProducts.layoutManager = LinearLayoutManager(requireContext())
        vm.products.observe(viewLifecycleOwner) { adapter.submitList(it) }
    }
}

class StoreSuppliersFragment(private val vm: CommerceViewModel) : Fragment(R.layout.layout_store_suppliers) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = LayoutStoreSuppliersBinding.bind(view)
        val adapter = SupplierAdapter()
        b.rvSuppliers.adapter = adapter
        b.rvSuppliers.layoutManager = LinearLayoutManager(requireContext())
        vm.suppliers.observe(viewLifecycleOwner) { adapter.submitList(it) }
    }
}

class StoreCustomersFragment(private val vm: CommerceViewModel) : Fragment(R.layout.layout_store_customers) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = LayoutStoreCustomersBinding.bind(view)
        val adapter = CustomerAdapter()
        b.rvCustomers.adapter = adapter
        b.rvCustomers.layoutManager = LinearLayoutManager(requireContext())
        vm.customers.observe(viewLifecycleOwner) { adapter.submitList(it) }
    }
}

class StoreSalesFragment(private val vm: CommerceViewModel) : Fragment(R.layout.layout_store_sales) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = LayoutStoreSalesBinding.bind(view)
        val adapter = SaleAdapter()
        b.rvSales.adapter = adapter
        b.rvSales.layoutManager = LinearLayoutManager(requireContext())
        vm.sales.observe(viewLifecycleOwner) { adapter.submitList(it) }
    }
}

class StoreMembersFragment(private val vm: CommerceViewModel) : Fragment(R.layout.layout_store_members) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = LayoutStoreMembersBinding.bind(view)
        val adapter = MemberAdapter()
        b.rvMembers.adapter = adapter
        b.rvMembers.layoutManager = LinearLayoutManager(requireContext())
        vm.members.observe(viewLifecycleOwner) { adapter.submitList(it) }
    }
}

// ── Adapters ────────────────────────────────────────────────────────────────

class ProductAdapter : ListAdapter<Product, ProductAdapter.VH>(DIFF) {
    inner class VH(v: View) : RecyclerView.ViewHolder(v) { val tv = v.findViewById<TextView>(android.R.id.text1) }
    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(LayoutInflater.from(p.context).inflate(android.R.layout.simple_list_item_1, p, false))
    override fun onBindViewHolder(h: VH, pos: Int) { val item = getItem(pos); h.tv.text = "${item.name} (${item.stockQty} in stock)" }
    companion object { val DIFF = object : DiffUtil.ItemCallback<Product>() { override fun areItemsTheSame(a: Product, b: Product) = a.id == b.id; override fun areContentsTheSame(a: Product, b: Product) = a == b } }
}

class SupplierAdapter : ListAdapter<Supplier, SupplierAdapter.VH>(DIFF) {
    inner class VH(v: View) : RecyclerView.ViewHolder(v) { val tv = v.findViewById<TextView>(android.R.id.text1) }
    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(LayoutInflater.from(p.context).inflate(android.R.layout.simple_list_item_1, p, false))
    override fun onBindViewHolder(h: VH, pos: Int) { val item = getItem(pos); h.tv.text = item.name }
    companion object { val DIFF = object : DiffUtil.ItemCallback<Supplier>() { override fun areItemsTheSame(a: Supplier, b: Supplier) = a.id == b.id; override fun areContentsTheSame(a: Supplier, b: Supplier) = a == b } }
}

class CustomerAdapter : ListAdapter<Customer, CustomerAdapter.VH>(DIFF) {
    inner class VH(v: View) : RecyclerView.ViewHolder(v) { val tv = v.findViewById<TextView>(android.R.id.text1) }
    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(LayoutInflater.from(p.context).inflate(android.R.layout.simple_list_item_1, p, false))
    override fun onBindViewHolder(h: VH, pos: Int) { val item = getItem(pos); h.tv.text = "${item.name} (${item.email})" }
    companion object { val DIFF = object : DiffUtil.ItemCallback<Customer>() { override fun areItemsTheSame(a: Customer, b: Customer) = a.id == b.id; override fun areContentsTheSame(a: Customer, b: Customer) = a == b } }
}

class SaleAdapter : ListAdapter<Sale, SaleAdapter.VH>(DIFF) {
    inner class VH(v: View) : RecyclerView.ViewHolder(v) { val tv = v.findViewById<TextView>(android.R.id.text1) }
    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(LayoutInflater.from(p.context).inflate(android.R.layout.simple_list_item_1, p, false))
    override fun onBindViewHolder(h: VH, pos: Int) { val item = getItem(pos); h.tv.text = "Sale #${item.id.takeLast(4)}: $${item.totalAmount}" }
    companion object { val DIFF = object : DiffUtil.ItemCallback<Sale>() { override fun areItemsTheSame(a: Sale, b: Sale) = a.id == b.id; override fun areContentsTheSame(a: Sale, b: Sale) = a == b } }
}

class MemberAdapter : ListAdapter<WorkspaceMember, MemberAdapter.VH>(DIFF) {
    inner class VH(v: View) : RecyclerView.ViewHolder(v) { val tv = v.findViewById<TextView>(android.R.id.text1) }
    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(LayoutInflater.from(p.context).inflate(android.R.layout.simple_list_item_1, p, false))
    override fun onBindViewHolder(h: VH, pos: Int) { val item = getItem(pos); h.tv.text = "${item.name} (${item.role})" }
    companion object { val DIFF = object : DiffUtil.ItemCallback<WorkspaceMember>() { override fun areItemsTheSame(a: WorkspaceMember, b: WorkspaceMember) = a.id == b.id; override fun areContentsTheSame(a: WorkspaceMember, b: WorkspaceMember) = a == b } }
}
