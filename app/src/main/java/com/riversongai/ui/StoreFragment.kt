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
import com.riversongai.R
import com.riversongai.data.model.CreateProduct
import com.riversongai.data.model.Product
import com.riversongai.databinding.FragmentStoreBinding
import com.riversongai.ui.adapter.ProductAdapter
import com.riversongai.ui.viewmodel.CommerceViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class StoreFragment : Fragment(R.layout.fragment_store) {

    private var _b: FragmentStoreBinding? = null
    private val b get() = _b!!
    private val vm: CommerceViewModel by viewModel()
    private lateinit var adapter: ProductAdapter
    private var allProducts: List<Product> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentStoreBinding.bind(view)

        adapter = ProductAdapter(onDelete = { p ->
            vm.deleteProduct(p.id)
        })

        b.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@StoreFragment.adapter
        }

        b.swipeRefresh.setOnRefreshListener { vm.load() }

        // Search filter
        b.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProducts(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        b.btnAddProduct.setOnClickListener {
            b.cardAddProduct.isVisible = !b.cardAddProduct.isVisible
            if (!b.cardAddProduct.isVisible) clearProductForm()
        }

        b.btnCancelProduct.setOnClickListener {
            b.cardAddProduct.isVisible = false
            clearProductForm()
        }

        b.btnSaveProduct.setOnClickListener { saveProduct() }
        b.btnCreateWorkspace.setOnClickListener { createWorkspace() }

        observeVm()
        vm.load()
    }

    private fun observeVm() {
        vm.isLoading.observe(viewLifecycleOwner) { b.swipeRefresh.isRefreshing = it }

        vm.workspace.observe(viewLifecycleOwner) { ws ->
            val hasWs = ws != null
            b.layoutNoWorkspace.isVisible = !hasWs
            b.layoutStoreContent.isVisible = hasWs
            if (ws != null) {
                b.tvWorkspaceName.text = ws.name
            }
        }

        vm.products.observe(viewLifecycleOwner) { products ->
            allProducts = products
            filterProducts(b.etSearch.text?.toString() ?: "")
            b.tvProductCount.text = "${products.size} product${if (products.size == 1) "" else "s"}"
        }

        vm.error.observe(viewLifecycleOwner) { err ->
            err?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show(); vm.clearError() }
        }

        vm.toast.observe(viewLifecycleOwner) { msg ->
            msg?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); vm.clearToast() }
        }
    }

    private fun filterProducts(query: String) {
        val filtered = if (query.isBlank()) allProducts
        else allProducts.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.sku.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true)
        }
        adapter.submitList(filtered)
    }

    private fun saveProduct() {
        val name = b.etProductName.text?.toString()?.trim() ?: ""
        val sku  = b.etProductSku.text?.toString()?.trim() ?: ""
        if (name.isBlank()) { b.etProductName.error = "Required"; return }
        if (sku.isBlank())  { b.etProductSku.error  = "Required"; return }
        val stock = b.etProductStock.text?.toString()?.toIntOrNull() ?: 0
        val price = b.etProductPrice.text?.toString()?.toDoubleOrNull() ?: 0.0
        vm.createProduct(CreateProduct(sku = sku, name = name, stockQty = stock, unitPrice = price))
        b.cardAddProduct.isVisible = false
        clearProductForm()
    }

    private fun createWorkspace() {
        val name = b.etWorkspaceName.text?.toString()?.trim() ?: ""
        if (name.isBlank()) { b.etWorkspaceName.error = "Required"; return }
        vm.createWorkspace(name)
    }

    private fun clearProductForm() {
        b.etProductName.text?.clear()
        b.etProductSku.text?.clear()
        b.etProductStock.text?.clear()
        b.etProductPrice.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
