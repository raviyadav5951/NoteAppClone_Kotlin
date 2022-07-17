package com.task.noteapp.ui.fragments

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.task.noteapp.R
import com.task.noteapp.adapters.NoteListAdapter
import com.task.noteapp.databinding.FragmentNoteListBinding
import com.task.noteapp.model.Note
import com.task.noteapp.ui.activity.NoteActivity
import com.task.noteapp.utils.hideKeyboard
import com.task.noteapp.viewmodel.NoteActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

class NoteListFragment : Fragment(R.layout.fragment_note_list), NoteListAdapter.DeleteListener {

    private val noteActivityViewModel: NoteActivityViewModel by activityViewModels()
    private lateinit var noteListAdapter: NoteListAdapter
    private lateinit var noteListBinding: FragmentNoteListBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialElevationScale(false).apply {
            duration = 350
        }
        enterTransition = MaterialElevationScale(true).apply {
            duration = 350
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noteListBinding = FragmentNoteListBinding.bind(view)
        val activity = activity as NoteActivity
        val navController = Navigation.findNavController(view)

        requireView().hideKeyboard()
        CoroutineScope(Dispatchers.Main).launch {
            delay(10)
            activity.window.statusBarColor = Color.WHITE
        }

        val count = parentFragmentManager.backStackEntryCount
        Log.d("backStackCount", count.toString())

        refreshListAfterNoteAction(view)

        //sets up RecyclerView
        recyclerViewDisplay()

        //implements search function
        noteListBinding.search.addTextChangedListener(searchTextWatcher)

        noteListBinding.search.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                v.clearFocus()
                requireView().hideKeyboard()
            }
            return@setOnEditorActionListener true
        }

        noteListBinding.clearText.setOnClickListener {
            clearTxtFunction()
            it.isVisible = false
            noteListBinding.noData.isVisible = false
        }

        noteListBinding.addNoteFab.setOnClickListener {
            noteListBinding.appBarLayout1.visibility = View.INVISIBLE
            navController.navigate(NoteListFragmentDirections.actionNoteFragmentToNoteContentFragment())
        }

        noteListBinding.innerFab.setOnClickListener {
            navController.navigate(NoteListFragmentDirections.actionNoteFragmentToNoteContentFragment())
        }

        hideAddNoteOnListScroll()
    }


    /**
     * Recycler view configuration started----------
     */

    private fun recyclerViewDisplay() {
        @SuppressLint("SwitchIntDef")
        when (resources.configuration.orientation) {
            //not using landscape mode for now. (span count will be 2 for portrait)
            Configuration.ORIENTATION_PORTRAIT -> setUpRecyclerView(2)

            Configuration.ORIENTATION_LANDSCAPE -> setUpRecyclerView(3)
        }
    }

    private fun setUpRecyclerView(spanCount: Int) {
        noteListBinding.rvNote.apply {
            layoutManager =
                StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)

            noteListAdapter = NoteListAdapter(this@NoteListFragment)

            noteListAdapter.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            adapter = noteListAdapter

            postponeEnterTransition(300L, TimeUnit.MILLISECONDS)
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }
        observerDataChanges()
    }

    /**
     * To hide the add note while list is getting scrolled
     */
    private fun hideAddNoteOnListScroll() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            noteListBinding.rvNote.setOnScrollChangeListener { _, scrollX, scrollY, _, oldScrollY ->
                when {
                    scrollY > oldScrollY -> {
                        noteListBinding.chatFabText.isVisible = false

                    }
                    scrollX == scrollY -> {
                        noteListBinding.chatFabText.isVisible = true

                    }
                    else -> {
                        noteListBinding.chatFabText.isVisible = true
                    }
                }
            }
        } else {
            noteListBinding.rvNote.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0 && noteListBinding.chatFabText.visibility == View.VISIBLE) {
                        noteListBinding.chatFabText.isVisible = false
                    } else if (dy < 0 && noteListBinding.chatFabText.visibility != View.VISIBLE) {
                        noteListBinding.chatFabText.isVisible = true
                    }
                }
            })
        }
    }


    /**
     * Observer for viewmodel if entry is added/updated
     */

    private fun observerDataChanges() {
        noteActivityViewModel.getAllNotes().observe(viewLifecycleOwner) { list ->
            noteListBinding.noData.isVisible = list.isEmpty()

            //Submits a new list to be diffed, and displayed.
            noteListAdapter.submitList(list)
        }
    }

    /**
     * Clear the search field input text
     */
    private fun clearTxtFunction() {
        noteListBinding.search.apply {
            text.clear()
            hideKeyboard()
            clearFocus()
            observerDataChanges()
        }
    }

    /**
     *     Receives confirmation from the noteContentFragment
     */

    private fun refreshListAfterNoteAction(view: View) {

        setFragmentResultListener("key") { _, bundle ->
            when (val result = bundle.getString("bundleKey")) {
                "Note Saved", "Empty Note Discarded" -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        Snackbar.make(view, result, Snackbar.LENGTH_SHORT).apply {
                            animationMode = Snackbar.ANIMATION_MODE_FADE
                            setAnchorView(R.id.addNoteFab)
                        }.show()
                        noteListBinding.rvNote.isVisible = false
                        delay(300)
                        recyclerViewDisplay()
                        noteListBinding.rvNote.isVisible = true
                    }
                }
            }
        }
    }

    /**
     * Listener for search text
     */

    private val searchTextWatcher: TextWatcher = object : TextWatcher {

        override fun beforeTextChanged(
            s: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) {
            noteListBinding.noData.isVisible = false
        }

        override fun onTextChanged(
            s: CharSequence?,
            start: Int,
            before: Int,
            count: Int
        ) {
            if (s.toString().isNotEmpty()) {
                noteListBinding.clearText.visibility = View.VISIBLE
                val text = s.toString()
                val query = "%$text%"
                if (query.isNotEmpty()) {
                    noteActivityViewModel.searchNote(query).observe(viewLifecycleOwner) {
                        noteListAdapter.submitList(it)
                    }
                } else {
                    observerDataChanges()
                }
            } else {
                observerDataChanges()
            }
        }

        override fun afterTextChanged(s: Editable?) {
            if (s.toString().isEmpty()) {
                noteListBinding.clearText.visibility = View.GONE
            }
        }

    }

    override fun onDeleteNote(note: Note) {
        Timber.d("delete clicked")
        noteActivityViewModel.deleteNote(note)
        noteListBinding.search.apply {
            hideKeyboard()
            clearFocus()
        }
        if (noteListBinding.search.text.toString().isEmpty()) {
            observerDataChanges()
        }
        Snackbar.make(
            requireView(), "Note Deleted", Snackbar.LENGTH_LONG
        ).show()
    }

}