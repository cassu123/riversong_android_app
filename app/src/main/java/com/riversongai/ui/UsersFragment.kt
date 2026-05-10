package com.riversongai.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.riversongai.R
import com.riversongai.data.model.AppUser
import com.riversongai.databinding.FragmentUsersBinding
import com.riversongai.databinding.ItemUserBinding
import com.riversongai.ui.viewmodel.UsersViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class UsersFragment : Fragment(R.layout.fragment_users) {

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UsersViewModel by viewModel()
    
    private lateinit var pendingAdapter: UserAdapter
    private lateinit var allUsersAdapter: UserAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUsersBinding.bind(view)

        setupUI()
        observeViewModel()
        
        viewModel.loadUsers()
    }

    private fun setupUI() {
        pendingAdapter = UserAdapter(
            onApprove = { user -> viewModel.approveUser(user.id) },
            onRoleClick = { /* N/A for pending */ }
        )
        binding.recyclerViewPending.apply {
            adapter = pendingAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        allUsersAdapter = UserAdapter(
            onApprove = { /* N/A */ },
            onRoleClick = { user -> cycleRole(user) }
        )
        binding.recyclerViewAll.apply {
            adapter = allUsersAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.swipeRefresh.setOnRefreshListener { viewModel.loadUsers() }
    }

    private fun cycleRole(user: AppUser) {
        val nextRole = when (user.role) {
            "user" -> "parent"
            "parent" -> "admin"
            else -> "user"
        }
        viewModel.updateUserRole(user.id, nextRole)
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = it
            binding.swipeRefresh.isRefreshing = it
        }

        viewModel.users.observe(viewLifecycleOwner) { users ->
            val pending = users.filter { it.isPending }
            val approved = users.filter { !it.isPending }
            
            pendingAdapter.submitList(pending)
            allUsersAdapter.submitList(approved)
            
            binding.cardPending.isVisible = pending.isNotEmpty()
            binding.textPendingCount.text = pending.size.toString()
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private inner class UserAdapter(
        private val onApprove: (AppUser) -> Unit,
        private val onRoleClick: (AppUser) -> Unit
    ) : ListAdapter<AppUser, UserAdapter.VH>(UserDiff) {

        inner class VH(val b: ItemUserBinding) : RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
            VH(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun onBindViewHolder(holder: VH, position: Int) {
            val user = getItem(position)
            holder.b.textDisplayName.text = user.displayName
            holder.b.textEmail.text = user.email
            holder.b.textAvatar.text = user.displayName.take(1).uppercase()
            
            val roleColor = when (user.role) {
                "admin" -> com.google.android.material.R.attr.colorPrimaryContainer
                "parent" -> com.google.android.material.R.attr.colorSecondaryContainer
                else -> com.google.android.material.R.attr.colorSurfaceVariant
            }
            holder.b.chipRole.text = user.role.replaceFirstChar { it.uppercase() }
            holder.b.chipRole.setChipBackgroundColorResource(android.R.color.transparent)
            
            val typedValue = android.util.TypedValue()
            requireContext().theme.resolveAttribute(roleColor, typedValue, true)
            holder.b.chipRole.chipBackgroundColor = ColorStateList.valueOf(typedValue.data)

            if (user.isPending) {
                holder.b.buttonApprove.isVisible = true
                holder.b.chipRole.isVisible = false
                holder.b.buttonApprove.setOnClickListener { onApprove(user) }
            } else {
                holder.b.buttonApprove.isVisible = false
                holder.b.chipRole.isVisible = true
                holder.b.chipRole.setOnClickListener { onRoleClick(user) }
            }
        }
    }

    object UserDiff : DiffUtil.ItemCallback<AppUser>() {
        override fun areItemsTheSame(oldItem: AppUser, newItem: AppUser) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: AppUser, newItem: AppUser) = oldItem == newItem
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
