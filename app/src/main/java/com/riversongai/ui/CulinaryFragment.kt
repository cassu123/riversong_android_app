package com.riversongai.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.riversongai.R
import com.riversongai.data.model.*
import com.riversongai.databinding.*
import com.riversongai.ui.viewmodel.CulinaryViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

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
        
        viewModel.loadRecipes()
        viewModel.loadBannedItems()
        viewModel.loadHousehold()
    }

    private fun setupViewPager() {
        binding.viewPagerCulinary.adapter = CulinaryPagerAdapter()
        TabLayoutMediator(binding.tabLayoutCulinary, binding.viewPagerCulinary) { tab, position ->
            tab.text = when (position) {
                0 -> "Recipes"
                1 -> "Banned Items"
                2 -> "Equipment"
                else -> "Meal Plan"
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
            viewModel.addRecipe(RecipeCreate(
                title = title,
                mealType = sheetBinding.autoCompleteMealType.text.toString(),
                prepTime = sheetBinding.editTextPrepTime.text.toString().toIntOrNull() ?: 0,
                cookTime = sheetBinding.editTextCookTime.text.toString().toIntOrNull() ?: 0,
                servings = sheetBinding.editTextServings.text.toString().toIntOrNull() ?: 1,
                instructions = sheetBinding.editTextInstructions.text.toString()
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
        override fun getItemCount() = 4
        override fun getItemViewType(position: Int) = position
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                0 -> RecipesViewHolder(LayoutCulinaryRecipesBinding.inflate(inflater, parent, false))
                1 -> BannedViewHolder(LayoutCulinaryBannedBinding.inflate(inflater, parent, false))
                2 -> EquipmentViewHolder(LayoutCulinaryEquipmentBinding.inflate(inflater, parent, false))
                else -> MealPlanViewHolder(LayoutCulinaryMealplanBinding.inflate(inflater, parent, false))
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is RecipesViewHolder -> holder.bind()
                is BannedViewHolder -> holder.bind()
                is EquipmentViewHolder -> holder.bind()
            }
        }

        inner class RecipesViewHolder(val b: LayoutCulinaryRecipesBinding) : RecyclerView.ViewHolder(b.root) {
            val recipeAdapter = RecipeAdapter()
            fun bind() {
                b.recyclerViewRecipes.apply { adapter = recipeAdapter; layoutManager = LinearLayoutManager(requireContext()) }
                b.swipeRefreshRecipes.setOnRefreshListener { viewModel.loadRecipes() }
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

        inner class BannedViewHolder(val b: LayoutCulinaryBannedBinding) : RecyclerView.ViewHolder(b.root) {
            val bannedAdapter = BannedAdapter()
            fun bind() {
                b.recyclerViewBanned.apply { adapter = bannedAdapter; layoutManager = LinearLayoutManager(requireContext()) }
                b.swipeRefreshBanned.setOnRefreshListener { viewModel.loadBannedItems() }
                b.buttonAddBanned.setOnClickListener { showAddBannedDialog() }
                
                ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                    override fun onMove(r: RecyclerView, v: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        val item = bannedAdapter.currentList[viewHolder.adapterPosition]
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
                val layout = android.widget.LinearLayout(ctx).apply { 
                    orientation = android.widget.LinearLayout.VERTICAL
                    setPadding(24, 24, 24, 24)
                }
                val etName = com.google.android.material.textfield.TextInputEditText(ctx).apply { hint = "Item Name" }
                val etSub = com.google.android.material.textfield.TextInputEditText(ctx).apply { hint = "Substitute (Optional)" }
                val btnSave = com.google.android.material.button.MaterialButton(ctx).apply { 
                    text = "Save"; setOnClickListener {
                        val name = etName.text.toString(); if (name.isBlank()) return@setOnClickListener
                        viewModel.addBannedItem(BannedItemCreate(name, "preference", etSub.text.toString()))
                        dialog.dismiss()
                    }
                }
                layout.addView(etName); layout.addView(etSub); layout.addView(btnSave)
                dialog.setContentView(layout); dialog.show()
            }
        }

        inner class EquipmentViewHolder(val b: LayoutCulinaryEquipmentBinding) : RecyclerView.ViewHolder(b.root) {
            fun bind() {
                viewModel.household.observe(viewLifecycleOwner) { household ->
                    b.gridEquipment.removeAllViews()
                    household?.equipment?.forEach { (key, hasIt) ->
                        val itemB = ItemCulinaryEquipmentBinding.inflate(LayoutInflater.from(requireContext()), b.gridEquipment, false)
                        itemB.textEquipmentName.text = key.replace("_", " ").replaceFirstChar { it.uppercase() }
                        itemB.switchEquipment.isChecked = hasIt
                        itemB.switchEquipment.setOnCheckedChangeListener { _, checked ->
                            val current = viewModel.household.value?.equipment?.toMutableMap() ?: mutableMapOf()
                            current[key] = checked
                            viewModel.updateEquipment(current)
                        }
                        b.gridEquipment.addView(itemB.root)
                    }
                }
            }
        }

        inner class MealPlanViewHolder(val b: LayoutCulinaryMealplanBinding) : RecyclerView.ViewHolder(b.root)
    }

    private inner class RecipeAdapter : ListAdapter<Recipe, RecipeAdapter.VH>(RecipeDiff) {
        inner class VH(val b: ItemRecipeBinding) : RecyclerView.ViewHolder(b.root)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        override fun onBindViewHolder(holder: VH, position: Int) {
            val r = getItem(position)
            holder.b.textViewRecipeTitle.text = r.title
            holder.b.textViewRecipeTime.text = "${r.prepTime + r.cookTime} min total"
            holder.b.chipMealType.text = r.mealType
            holder.b.textViewRecipeRating.text = "⭐ ${r.rating}"
            holder.b.imageViewRecipe.load(r.imageUrl) { placeholder(R.drawable.ic_memory); error(R.drawable.ic_memory) }
        }
    }

    private inner class BannedAdapter : ListAdapter<BannedItem, BannedAdapter.VH>(BannedDiff) {
        inner class VH(val b: ItemCulinaryBannedBinding) : RecyclerView.ViewHolder(b.root)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(ItemCulinaryBannedBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        override fun onBindViewHolder(holder: VH, position: Int) {
            val i = getItem(position)
            holder.b.textBannedName.text = i.name
            holder.b.chipBannedReason.text = i.reason?.replaceFirstChar { it.uppercase() } ?: "Preference"
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

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
