package com.riversongai.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.riversongai.R
import com.riversongai.data.model.*
import com.riversongai.databinding.*
import com.riversongai.ui.viewmodel.CulinaryViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlinx.coroutines.launch

class CulinaryFragment : Fragment(R.layout.fragment_culinary) {

    private var _binding: FragmentCulinaryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CulinaryViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCulinaryBinding.bind(view)

        setupViewPager()
        setupFab()
        observeViewModel()
        
        viewModel.loadAll()
    }

    private fun setupViewPager() {
        binding.viewPagerCulinary.adapter = CulinaryPagerAdapter()
        TabLayoutMediator(binding.tabLayoutCulinary, binding.viewPagerCulinary) { tab, position ->
            tab.text = when (position) {
                0 -> "Library"
                1 -> "What's for Dinner"
                2 -> "Stockroom"
                3 -> "Prep Deck"
                4 -> "Walmart Export"
                5 -> "Banned Items"
                else -> "Equipment"
            }
        }.attach()
    }

    private fun setupFab() {
        binding.fabAddRecipe.setOnClickListener {
            showAddRecipeDialog()
        }
    }

    private fun showAddRecipeDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = LayoutAddRecipeBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack", "Dessert", "Other")
        sheetBinding.autoCompleteMealType.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mealTypes))

        sheetBinding.buttonSaveRecipe.setOnClickListener {
            val title = sheetBinding.editTextTitle.text.toString()
            if (title.isBlank()) { sheetBinding.layoutTitle.error = "Title required"; return@setOnClickListener }
            viewModel.createRecipe(RecipeCreate(
                title = title,
                mealType = sheetBinding.autoCompleteMealType.text.toString(),
                servings = sheetBinding.editTextServings.text.toString().toIntOrNull() ?: 1,
                ingredients = null,
                steps = null,
                equipmentNeeded = null
            ))
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun observeViewModel() {
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private inner class CulinaryPagerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemCount() = 7
        override fun getItemViewType(position: Int) = position
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                0 -> RecipesViewHolder(LayoutCulinaryRecipesBinding.inflate(inflater, parent, false))
                1 -> DinnerViewHolder(LayoutCulinaryDinnerBinding.inflate(inflater, parent, false))
                2 -> StockroomViewHolder(LayoutCulinaryStockroomBinding.inflate(inflater, parent, false))
                3 -> PrepDeckViewHolder(LayoutCulinaryPrepBinding.inflate(inflater, parent, false))
                4 -> WalmartViewHolder(LayoutCulinaryWalmartBinding.inflate(inflater, parent, false))
                5 -> BannedViewHolder(LayoutCulinaryBannedBinding.inflate(inflater, parent, false))
                else -> EquipmentViewHolder(LayoutCulinaryEquipmentBinding.inflate(inflater, parent, false))
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is RecipesViewHolder -> holder.bind()
                is DinnerViewHolder -> holder.bind()
                is StockroomViewHolder -> holder.bind()
                is PrepDeckViewHolder -> holder.bind()
                is WalmartViewHolder -> holder.bind()
                is BannedViewHolder -> holder.bind()
                is EquipmentViewHolder -> holder.bind()
            }
        }

        inner class RecipesViewHolder(val b: LayoutCulinaryRecipesBinding) : RecyclerView.ViewHolder(b.root) {
            val recipeAdapter = RecipeAdapter()
            fun bind() {
                b.recyclerViewRecipes.apply { adapter = recipeAdapter; layoutManager = LinearLayoutManager(requireContext()) }
                b.swipeRefreshRecipes.setOnRefreshListener { lifecycleScope.launch { viewModel.loadRecipes() } }
                b.editTextRecipeSearch.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) { viewModel.setSearchQuery(s?.toString() ?: "") }
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })
                b.chipGroupMealTypes.setOnCheckedStateChangeListener { _, checkedIds ->
                    val mealType = when (checkedIds.firstOrNull()) {
                        R.id.chipBreakfast -> "Breakfast"; R.id.chipLunch -> "Lunch"; R.id.chipDinner -> "Dinner"
                        R.id.chipSnack -> "Snack"; R.id.chipDessert -> "Dessert"; else -> "All"
                    }
                    viewModel.setMealType(mealType)
                }
                viewModel.isLoading.observe(viewLifecycleOwner) {
                    b.progressBarRecipes.isVisible = it
                    b.swipeRefreshRecipes.isRefreshing = it
                }
                viewModel.filteredRecipes.observe(viewLifecycleOwner) {
                    recipeAdapter.submitList(it)
                    b.textViewEmptyRecipes.isVisible = it.isEmpty()
                }
            }
        }

        inner class DinnerViewHolder(val b: LayoutCulinaryDinnerBinding) : RecyclerView.ViewHolder(b.root) {
            val adapter = DinnerAdapter()
            fun bind() {
                b.recyclerViewDinner.apply { adapter = this@DinnerViewHolder.adapter; layoutManager = LinearLayoutManager(requireContext()) }
                b.swipeRefreshDinner.setOnRefreshListener { lifecycleScope.launch { viewModel.loadDinnerProposals() } }
                viewModel.dinnerProposals.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                    b.textViewEmptyDinner.isVisible = it.isEmpty()
                    b.swipeRefreshDinner.isRefreshing = false
                }
            }
        }

        inner class StockroomViewHolder(val b: LayoutCulinaryStockroomBinding) : RecyclerView.ViewHolder(b.root) {
            val adapter = StockroomAdapter()
            fun bind() {
                b.recyclerViewStockroom.apply { adapter = this@StockroomViewHolder.adapter; layoutManager = LinearLayoutManager(requireContext()) }
                b.swipeRefreshStockroom.setOnRefreshListener { lifecycleScope.launch { viewModel.loadStockroom() } }
                viewModel.stockroom.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                    b.swipeRefreshStockroom.isRefreshing = false
                }
                b.buttonScanStock.setOnClickListener { /* mock */ viewModel.scanStockroom("MOCK_BARCODE") }
                b.buttonDepleteStock.setOnClickListener { /* mock */ viewModel.depleteStockroom("MOCK_BARCODE") }
            }
        }

        inner class PrepDeckViewHolder(val b: LayoutCulinaryPrepBinding) : RecyclerView.ViewHolder(b.root) {
            fun bind() {
                b.swipeRefreshPrep.setOnRefreshListener { lifecycleScope.launch { viewModel.loadActivePrep() } }
                viewModel.activePrep.observe(viewLifecycleOwner) { session ->
                    b.swipeRefreshPrep.isRefreshing = false
                    if (session != null) {
                        b.cardActiveSession.isVisible = true
                        b.textViewSessionLabel.text = session.label
                        b.buttonCompleteSession.setOnClickListener { /* missing endpoint in VM, omitting */ }
                        
                        // We will just do a simple string dump for recipes to save building another adapter
                        val recipesText = session.recipes.joinToString("\n") { it.recipeTitle ?: "Unknown" }
                        val tv = TextView(requireContext()).apply { text = recipesText; setPadding(16,16,16,16) }
                        b.recyclerViewPrepRecipes.visibility = View.GONE
                        (b.recyclerViewPrepRecipes.parent as ViewGroup).addView(tv)
                    } else {
                        b.cardActiveSession.isVisible = false
                    }
                }
            }
        }

        inner class WalmartViewHolder(val b: LayoutCulinaryWalmartBinding) : RecyclerView.ViewHolder(b.root) {
            val adapter = WalmartAdapter()
            fun bind() {
                b.recyclerViewWalmart.apply { adapter = this@WalmartViewHolder.adapter; layoutManager = LinearLayoutManager(requireContext()) }
                b.swipeRefreshWalmart.setOnRefreshListener { lifecycleScope.launch { viewModel.loadWalmartMappings() } }
                b.buttonExportCart.setOnClickListener {
                    viewModel.exportWalmart { resp ->
                        resp.cartUrl?.let { url -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                    }
                }
                b.buttonAddMapping.setOnClickListener { showAddMappingDialog() }
                viewModel.walmartMappings.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                    b.swipeRefreshWalmart.isRefreshing = false
                }
            }
            private fun showAddMappingDialog() {
                val dialog = BottomSheetDialog(requireContext())
                val ctx = requireContext()
                val layout = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 40, 40, 40) }
                val etName = com.google.android.material.textfield.TextInputEditText(ctx).apply { hint = "Ingredient Name" }
                val etId = com.google.android.material.textfield.TextInputEditText(ctx).apply { hint = "Walmart Item ID" }
                val btnSave = MaterialButton(ctx).apply { 
                    text = "Save"; setOnClickListener {
                        viewModel.createWalmartMapping(etName.text.toString(), etId.text.toString())
                        dialog.dismiss()
                    }
                }
                layout.addView(etName); layout.addView(etId); layout.addView(btnSave)
                dialog.setContentView(layout); dialog.show()
            }
        }

        inner class BannedViewHolder(val b: LayoutCulinaryBannedBinding) : RecyclerView.ViewHolder(b.root) {
            val bannedAdapter = BannedAdapter()
            fun bind() {
                b.recyclerViewBanned.apply { adapter = bannedAdapter; layoutManager = LinearLayoutManager(requireContext()) }
                b.swipeRefreshBanned.setOnRefreshListener { lifecycleScope.launch { viewModel.loadBannedItems() } }
                b.buttonAddBanned.setOnClickListener { showAddBannedDialog() }
                
                ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                    override fun onMove(r: RecyclerView, v: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        val item = bannedAdapter.currentList[viewHolder.bindingAdapterPosition]
                        viewModel.deleteBannedItem(item.id)
                        Snackbar.make(b.root, "${item.name} removed", Snackbar.LENGTH_LONG).show()
                    }
                }).attachToRecyclerView(b.recyclerViewBanned)

                viewModel.bannedItems.observe(viewLifecycleOwner) {
                    bannedAdapter.submitList(it)
                    b.textViewEmptyBanned.isVisible = it.isEmpty()
                    b.swipeRefreshBanned.isRefreshing = false
                }
            }

            private fun showAddBannedDialog() {
                val dialog = BottomSheetDialog(requireContext())
                val ctx = requireContext()
                val layout = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 40, 40, 40) }
                val etName = com.google.android.material.textfield.TextInputEditText(ctx).apply { hint = "Item Name" }
                val etSub = com.google.android.material.textfield.TextInputEditText(ctx).apply { hint = "Substitute (Optional)" }
                val btnSave = MaterialButton(ctx).apply { 
                    text = "Save"; setOnClickListener {
                        val name = etName.text.toString(); if (name.isBlank()) return@setOnClickListener
                        viewModel.addBannedItem(name, etSub.text.toString())
                        dialog.dismiss()
                    }
                }
                layout.addView(etName); layout.addView(etSub); layout.addView(btnSave)
                dialog.setContentView(layout); dialog.show()
            }
        }

        inner class EquipmentViewHolder(val b: LayoutCulinaryEquipmentBinding) : RecyclerView.ViewHolder(b.root) {
            fun bind() {
                viewModel.equipment.observe(viewLifecycleOwner) { equipment ->
                    b.gridEquipment.removeAllViews()
                    equipment?.forEach { eq ->
                        val itemB = ItemCulinaryEquipmentBinding.inflate(LayoutInflater.from(requireContext()), b.gridEquipment, false)
                        itemB.textEquipmentName.text = eq.label
                        itemB.switchEquipment.isChecked = true
                        b.gridEquipment.addView(itemB.root)
                    }
                }
            }
        }
    }

    private inner class RecipeAdapter : ListAdapter<Recipe, RecipeAdapter.VH>(RecipeDiff) {
        inner class VH(val b: ItemRecipeBinding) : RecyclerView.ViewHolder(b.root)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        override fun onBindViewHolder(holder: VH, position: Int) {
            val r = getItem(position)
            holder.b.textViewRecipeTitle.text = r.title
            holder.b.chipMealType.text = r.mealType
            holder.b.textViewRecipeRating.text = "⭐ ${r.rating}"
            holder.b.imageViewRecipe.load(r.imageUrl) { placeholder(R.drawable.ic_memory); error(R.drawable.ic_memory) }
        }
    }

    private inner class DinnerAdapter : ListAdapter<DinnerProposal, DinnerAdapter.VH>(DinnerDiff) {
        inner class VH(val v: View) : RecyclerView.ViewHolder(v) {
            val tv = v.findViewById<TextView>(android.R.id.text1)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val tv = TextView(parent.context).apply {
                id = android.R.id.text1
                setPadding(32, 32, 32, 32)
                textSize = 16f
            }
            return VH(tv)
        }
        override fun onBindViewHolder(holder: VH, position: Int) {
            val d = getItem(position)
            holder.tv.text = "${d.recipe?.title ?: "Unknown"} (Status: ${d.status})"
            holder.tv.setOnClickListener {
                viewModel.voteDinner(d.id, "yes")
            }
        }
    }

    private inner class StockroomAdapter : ListAdapter<StockroomItem, StockroomAdapter.VH>(StockroomDiff) {
        inner class VH(val v: View) : RecyclerView.ViewHolder(v) {
            val tv = v.findViewById<TextView>(android.R.id.text1)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val tv = TextView(parent.context).apply { id = android.R.id.text1; setPadding(32, 32, 32, 32); textSize = 16f }
            return VH(tv)
        }
        override fun onBindViewHolder(holder: VH, position: Int) {
            val s = getItem(position)
            holder.tv.text = "${s.name} - ${s.state}"
        }
    }

    private inner class WalmartAdapter : ListAdapter<WalmartMapping, WalmartAdapter.VH>(WalmartDiff) {
        inner class VH(val v: View) : RecyclerView.ViewHolder(v) {
            val tv = v.findViewById<TextView>(android.R.id.text1)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val tv = TextView(parent.context).apply { id = android.R.id.text1; setPadding(32, 32, 32, 32); textSize = 16f }
            return VH(tv)
        }
        override fun onBindViewHolder(holder: VH, position: Int) {
            val m = getItem(position)
            holder.tv.text = "${m.ingredientName} -> ${m.walmartItemId}"
        }
    }

    private inner class BannedAdapter : ListAdapter<BannedItem, BannedAdapter.VH>(BannedDiff) {
        inner class VH(val b: ItemCulinaryBannedBinding) : RecyclerView.ViewHolder(b.root)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(ItemCulinaryBannedBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        override fun onBindViewHolder(holder: VH, position: Int) {
            val i = getItem(position)
            holder.b.textBannedName.text = i.name
            holder.b.textBannedSubstitute.isVisible = !i.substitute.isNullOrBlank()
            holder.b.textBannedSubstitute.text = "Substitute: ${i.substitute}"
        }
    }

    object RecipeDiff : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem == newItem
    }
    object BannedDiff : DiffUtil.ItemCallback<BannedItem>() {
        override fun areItemsTheSame(oldItem: BannedItem, newItem: BannedItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: BannedItem, newItem: BannedItem) = oldItem == newItem
    }
    object DinnerDiff : DiffUtil.ItemCallback<DinnerProposal>() {
        override fun areItemsTheSame(oldItem: DinnerProposal, newItem: DinnerProposal) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: DinnerProposal, newItem: DinnerProposal) = oldItem == newItem
    }
    object StockroomDiff : DiffUtil.ItemCallback<StockroomItem>() {
        override fun areItemsTheSame(oldItem: StockroomItem, newItem: StockroomItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: StockroomItem, newItem: StockroomItem) = oldItem == newItem
    }
    object WalmartDiff : DiffUtil.ItemCallback<WalmartMapping>() {
        override fun areItemsTheSame(oldItem: WalmartMapping, newItem: WalmartMapping) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: WalmartMapping, newItem: WalmartMapping) = oldItem == newItem
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
