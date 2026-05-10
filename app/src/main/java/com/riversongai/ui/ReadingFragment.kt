package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.riversongai.R
import com.riversongai.data.model.Book
import com.riversongai.data.model.BookCreate
import com.riversongai.data.model.BookUpdate
import com.riversongai.databinding.BottomSheetAddBookBinding
import com.riversongai.databinding.FragmentReadingBinding
import com.riversongai.databinding.ItemBookBinding
import com.riversongai.ui.viewmodel.ReadingViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReadingFragment : Fragment(R.layout.fragment_reading) {

    private var _binding: FragmentReadingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReadingViewModel by viewModel()
    
    private lateinit var bookAdapter: BookAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentReadingBinding.bind(view)

        setupUI()
        observeViewModel()
        
        viewModel.loadData()
    }

    private fun setupUI() {
        bookAdapter = BookAdapter(
            onLongClick = { book, anchor -> showBookOptions(book, anchor) }
        )
        binding.recyclerViewBooks.apply {
            adapter = bookAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.swipeRefresh.setOnRefreshListener { viewModel.loadData() }

        binding.chipGroupStatus.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when (checkedIds.firstOrNull()) {
                R.id.chipReading -> "Reading"
                R.id.chipFinished -> "Finished"
                R.id.chipWantToRead -> "Want to Read"
                R.id.chipDNF -> "DNF"
                else -> "All"
            }
            viewModel.setStatusFilter(filter)
        }

        binding.fabAddBook.setOnClickListener { showAddBookDialog() }
    }

    private fun showAddBookDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = BottomSheetAddBookBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        val services = listOf("Manual", "Audible", "Kindle", "Libby", "Google Play", "Kobo", "Apple Books")
        sheetBinding.autoCompleteService.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, services))

        val statuses = listOf("Reading", "Finished", "Want to Read", "DNF")
        sheetBinding.autoCompleteStatus.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, statuses))

        sheetBinding.autoCompleteStatus.setOnItemClickListener { _, _, position, _ ->
            sheetBinding.layoutProgress.isVisible = statuses[position] == "Reading"
        }

        sheetBinding.buttonSaveBook.setOnClickListener {
            val title = sheetBinding.editTextTitle.text.toString()
            val author = sheetBinding.editTextAuthor.text.toString()
            val service = sheetBinding.autoCompleteService.text.toString()
            val status = sheetBinding.autoCompleteStatus.text.toString().lowercase().replace(" ", "_")
            val progress = sheetBinding.editTextProgress.text.toString().toIntOrNull() ?: 0

            if (title.isBlank() || author.isBlank()) {
                if (title.isBlank()) sheetBinding.layoutTitle.error = "Required"
                if (author.isBlank()) sheetBinding.layoutAuthor.error = "Required"
                return@setOnClickListener
            }

            viewModel.addBook(BookCreate(service, title, author, status = status, progressPct = progress))
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showBookOptions(book: Book, anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_book_options, popup.menu)
        
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_mark_finished -> {
                    viewModel.updateBook(book.id, BookUpdate(status = "finished", progressPct = 100))
                    true
                }
                R.id.action_delete -> {
                    viewModel.deleteBook(book.id)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = it
            binding.swipeRefresh.isRefreshing = it
        }

        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            stats?.let {
                binding.textStatTotal.text = it.total.toString()
                binding.textStatReading.text = it.byStatus["reading"]?.toString() ?: "0"
                binding.textStatFinished.text = it.byStatus["finished"]?.toString() ?: "0"
                binding.textStatWantToRead.text = it.byStatus["want_to_read"]?.toString() ?: "0"
            }
        }

        viewModel.filteredBooks.observe(viewLifecycleOwner) { books ->
            bookAdapter.submitList(books)
            binding.textViewEmpty.isVisible = books.isEmpty()
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private inner class BookAdapter(
        private val onLongClick: (Book, View) -> Unit
    ) : ListAdapter<Book, BookAdapter.VH>(DiffCallback) {

        inner class VH(val b: ItemBookBinding) : RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val book = getItem(position)
            holder.b.textViewTitle.text = book.title
            holder.b.textViewAuthor.text = book.author
            holder.b.chipStatus.text = book.status.replace("_", " ").replaceFirstChar { it.uppercase() }
            
            val isReading = book.status == "reading"
            holder.b.layoutProgress.isVisible = isReading
            if (isReading) {
                holder.b.progressBook.progress = book.progressPct
                holder.b.textViewProgressLabel.text = "${book.progressPct}% complete"
            }

            if (book.rating != null) {
                holder.b.textViewRating.isVisible = true
                holder.b.textViewRating.text = "⭐".repeat(book.rating)
            } else {
                holder.b.textViewRating.isVisible = false
            }

            holder.b.imageViewCover.load(book.coverUrl) {
                placeholder(R.drawable.ic_reading)
                error(R.drawable.ic_reading)
            }

            holder.b.cardBook.setOnLongClickListener {
                onLongClick(book, holder.b.cardBook)
                true
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Book, newItem: Book) = oldItem == newItem
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
