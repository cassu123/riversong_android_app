package com.riversongai.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.riversongai.R
import com.riversongai.data.model.CalendarEvent
import com.riversongai.data.model.GmailMessage
import com.riversongai.databinding.FragmentGoogleBinding
import com.riversongai.databinding.ItemCalendarEventBinding
import com.riversongai.databinding.ItemGmailMessageBinding
import com.riversongai.ui.viewmodel.GoogleViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

class GoogleFragment : Fragment(R.layout.fragment_google) {

    private var _binding: FragmentGoogleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GoogleViewModel by viewModel()
    
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var gmailAdapter: GmailAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGoogleBinding.bind(view)

        setupUI()
        observeViewModel()
        
        viewModel.loadAll()
    }

    private fun setupUI() {
        calendarAdapter = CalendarAdapter()
        binding.recyclerViewCalendar.apply {
            adapter = calendarAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        gmailAdapter = GmailAdapter()
        binding.recyclerViewGmail.apply {
            adapter = gmailAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.swipeRefresh.setOnRefreshListener { viewModel.loadAll() }

        binding.buttonConnect.setOnClickListener {
            viewModel.fetchAuthUrl()
        }

        binding.buttonDoneConnecting.setOnClickListener {
            binding.buttonDoneConnecting.isVisible = false
            viewModel.loadAll()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = it
            binding.swipeRefresh.isRefreshing = it
        }

        viewModel.status.observe(viewLifecycleOwner) { status ->
            val connected = status?.connected == true
            binding.layoutConnected.isVisible = connected
            binding.layoutNotConnected.isVisible = !connected
            binding.cardCalendar.isVisible = connected
            binding.cardGmail.isVisible = connected
            
            if (connected) {
                binding.textConnectedEmail.text = "Connected as ${status?.email ?: "Unknown"}"
                binding.infoCalendar.alpha = 1.0f
                binding.infoGmail.alpha = 1.0f
                binding.infoMaps.alpha = 1.0f
                binding.infoMusic.alpha = 1.0f
            } else {
                binding.infoCalendar.alpha = 0.5f
                binding.infoGmail.alpha = 0.5f
                binding.infoMaps.alpha = 0.5f
                binding.infoMusic.alpha = 0.5f
            }
        }

        viewModel.events.observe(viewLifecycleOwner) { events ->
            calendarAdapter.submitList(events)
            binding.textCalendarEmpty.isVisible = events.isEmpty()
        }

        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            gmailAdapter.submitList(messages)
            binding.textGmailEmpty.isVisible = messages.isEmpty()
        }

        viewModel.authUrl.observe(viewLifecycleOwner) { url ->
            url?.let {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                startActivity(intent)
                viewModel.clearAuthUrl()
                binding.buttonDoneConnecting.isVisible = true
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private inner class CalendarAdapter : ListAdapter<CalendarEvent, CalendarAdapter.VH>(CalendarDiff) {
        inner class VH(val b: ItemCalendarEventBinding) : RecyclerView.ViewHolder(b.root)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
            VH(ItemCalendarEventBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        
        override fun onBindViewHolder(holder: VH, position: Int) {
            val event = getItem(position)
            holder.b.textSummary.text = event.summary
            holder.b.textDateTime.text = formatDateTime(event.start)
        }

        private fun formatDateTime(iso: String): String {
            return try {
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(iso)
                SimpleDateFormat("EEE, MMM d 'at' h:mm a", Locale.getDefault()).format(date!!)
            } catch (e: Exception) {
                iso
            }
        }
    }

    private inner class GmailAdapter : ListAdapter<GmailMessage, GmailAdapter.VH>(GmailDiff) {
        inner class VH(val b: ItemGmailMessageBinding) : RecyclerView.ViewHolder(b.root)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
            VH(ItemGmailMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        
        override fun onBindViewHolder(holder: VH, position: Int) {
            val msg = getItem(position)
            holder.b.textFrom.text = msg.from
            holder.b.textSubject.text = msg.subject
        }
    }

    object CalendarDiff : DiffUtil.ItemCallback<CalendarEvent>() {
        override fun areItemsTheSame(oldItem: CalendarEvent, newItem: CalendarEvent) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: CalendarEvent, newItem: CalendarEvent) = oldItem == newItem
    }

    object GmailDiff : DiffUtil.ItemCallback<GmailMessage>() {
        override fun areItemsTheSame(oldItem: GmailMessage, newItem: GmailMessage) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: GmailMessage, newItem: GmailMessage) = oldItem == newItem
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
