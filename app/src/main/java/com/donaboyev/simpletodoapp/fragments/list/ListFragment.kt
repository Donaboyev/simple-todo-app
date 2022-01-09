package com.donaboyev.simpletodoapp.fragments.list

import android.os.Bundle
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.donaboyev.simpletodoapp.R
import com.donaboyev.simpletodoapp.data.models.ToDoData
import com.donaboyev.simpletodoapp.data.viewmodel.ToDoViewModel
import com.donaboyev.simpletodoapp.databinding.FragmentListBinding
import com.donaboyev.simpletodoapp.fragments.SharedViewModel
import com.donaboyev.simpletodoapp.fragments.list.adapter.ListAdapter
import com.donaboyev.simpletodoapp.utils.hideKeyboard
import com.google.android.material.snackbar.Snackbar
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

class ListFragment : Fragment(), SearchView.OnQueryTextListener {

    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()
    private var _binding: FragmentListBinding? = null
    private val binding
        get() = _binding!!
    private val adapter: ListAdapter by lazy { ListAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        setupRecyclerView()
        mToDoViewModel.getAllData.observe(viewLifecycleOwner, { data ->
            mSharedViewModel.checkIfDatabaseEmpty(data)
            adapter.setData(data)
        })
        mSharedViewModel.emptyDatabase.observe(viewLifecycleOwner, {
            checkDatabaseForEmpty(it)
        })
        setHasOptionsMenu(true)
        hideKeyboard(requireActivity())
        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_addFragment)
        }
        adapter.setOnItemClickListener {
            findNavController().navigate(
                ListFragmentDirections.actionListFragmentToUpdateFragment(it)
            )
        }
        return binding.root
    }

    private fun checkDatabaseForEmpty(empty: Boolean) {
        when (empty) {
            true -> {
                binding.noDataImageView.visibility = VISIBLE
                binding.noDataTextView.visibility = VISIBLE
            }
            false -> {
                binding.noDataImageView.visibility = INVISIBLE
                binding.noDataTextView.visibility = INVISIBLE
            }
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = binding.recyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.itemAnimator = SlideInUpAnimator().apply {
            addDuration = 300
        }
        swipeToDelete(recyclerView)
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallback = object : SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedItem = adapter.dataList[viewHolder.adapterPosition]
                mToDoViewModel.deleteItem(deletedItem)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)
                restoreDeletedItem(viewHolder.itemView, deletedItem)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun restoreDeletedItem(view: View, deletedItem: ToDoData) {
        Snackbar.make(
            view,
            "Deleted '${deletedItem.title}'",
            Snackbar.LENGTH_LONG
        ).setAction("Undo") {
            mToDoViewModel.insertData(deletedItem)
        }.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_list, menu)
        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_delete_all -> confirmRemoval()
            R.id.menu_priority_high -> mToDoViewModel.sortByHighPriority.observe(
                this,
                { adapter.setData(it) })
            R.id.menu_priority_low -> mToDoViewModel.sortByLowPriority.observe(
                this,
                { adapter.setData(it) })
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) searchThroughDatabase(query)
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null) searchThroughDatabase(query)
        return true
    }

    private fun searchThroughDatabase(query: String) {
        val searchQuery = "%$query%"
        mToDoViewModel.searchDatabase(searchQuery).observe(this, { list ->
            list?.let {
                adapter.setData(it)
            }
        })
    }

    private fun confirmRemoval() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            mToDoViewModel.deleteAll()
            Toast.makeText(
                requireContext(),
                "Successfully Removed Everything!",
                Toast.LENGTH_SHORT
            ).show()
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete everything?")
        builder.setMessage("Are you sure you want to remove everything?")
        builder.create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}