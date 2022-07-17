package com.task.noteapp.ui.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.transition.MaterialContainerTransform
import com.task.noteapp.R
import com.task.noteapp.databinding.BottomSheetDialogBinding
import com.task.noteapp.databinding.FragmentNoteContentBinding
import com.task.noteapp.model.Note
import com.task.noteapp.ui.activity.NoteActivity
import com.task.noteapp.utils.*
import com.task.noteapp.viewmodel.NoteActivityViewModel
import kotlinx.coroutines.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class NoteContentFragment:Fragment(R.layout.fragment_note_content) {
    private lateinit var navController: NavController
    private lateinit var contentBinding: FragmentNoteContentBinding
    private lateinit var result: String

    private var note: Note? = null
    private var color = -1
    private val noteActivityViewModel: NoteActivityViewModel by activityViewModels()
    private val currentDate = SimpleDateFormat("dd/MM/yyyy",Locale.getDefault()).format(Date())

    private var isNoteUpdated:Boolean=false

    private val job = CoroutineScope(Dispatchers.Main)
    private val args: NoteContentFragmentArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.d("date :$currentDate")

        val animation = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragment
            scrimColor = Color.TRANSPARENT
            duration = 300L
            setAllContainerColors(requireContext().themeColor(R.attr.colorSurface))
        }
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation
        addSharedElementListener()
    }

    @SuppressLint("InflateParams", "QueryPermissionsNeeded")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentBinding = FragmentNoteContentBinding.bind(view)

        /* Sets the unique transition name for the layout that is
         being inflated using SharedElementEnterTransition class */
        ViewCompat.setTransitionName(
            contentBinding.noteContentFragmentParent,
            "recyclerView_${args.note?.id}"
        )

        navController = Navigation.findNavController(view)
        val activity = activity as NoteActivity

        contentBinding.backBtn.setOnClickListener {
            requireView().hideKeyboard()
            saveNoteAndGoBack()
        }


        contentBinding.noteOptionsMenu.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(
                requireContext(),
                R.style.BottomSheetDialogTheme,
            )
            val bottomSheetView: View = layoutInflater.inflate(
                R.layout.bottom_sheet_dialog,
                null,
            )

            with(bottomSheetDialog) {
                setContentView(bottomSheetView)
                show()
            }
            val bottomSheetBinding = BottomSheetDialogBinding.bind(bottomSheetView)

            bottomSheetBinding.apply {
                colorPicker.apply {
                    setSelectedColor(color)
                    setOnColorSelectedListener { value ->
                        color = value
                        contentBinding.apply {
                            noteContentFragmentParent.setBackgroundColor(color)
                            toolbarFragmentNoteContent.setBackgroundColor(color)
                            activity.window.statusBarColor = color
                        }
                        bottomSheetBinding.bottomSheetParent.setCardBackgroundColor(color)
                    }
                }
                bottomSheetParent.setCardBackgroundColor(color)
            }
            bottomSheetView.post {
                bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }

        }

        //opens with existing note item
        setUpNote()

        activity.onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    saveNoteAndGoBack()
                }
            })
    }

    private fun addSharedElementListener() {
        (sharedElementEnterTransition as Transition).addListener(
            object : TransitionListenerAdapter() {
                override fun onTransitionStart(transition: Transition) {
                    super.onTransitionStart(transition)
                    if (!args.note?.imageUrl.isNullOrBlank() ) {
                        contentBinding.noteImage.isVisible = true

                        job.launch {
                            requireContext().loadHiRezThumbnail(args.note?.imageUrl, contentBinding.noteImage)
                        }
                    } else contentBinding.noteImage.isVisible = false
                }
            }
        )
    }

    /**
     * This Method handles the save and update operation.
     *
     * Checks if the note arg is null
     * It will save the note with a unique id.
     *
     * If note arg has data it will update
     * note to save any changes. */
    private fun saveNoteAndGoBack() {

        if (contentBinding.etTitle.text.toString().isEmpty() &&
            contentBinding.etNoteContent.text.toString().isEmpty()
        ) {
            result = "Empty Note Discarded"
            setFragmentResult("key", bundleOf("bundleKey" to result))
            navController.navigate(
                NoteContentFragmentDirections
                    .actionNoteContentFragmentToNoteFragment()
            )

        } else {
            note = args.note
            when (note) {
                null -> {
                    noteActivityViewModel.saveNote(
                        Note(
                            0,
                            contentBinding.etTitle.trimInputSpace(),
                            contentBinding.etNoteContent.trimInputSpace(),
                            currentDate,
                            color,
                            contentBinding.etNoteImageUrl.trimInputSpace(),
                            edited = false
                        )
                    )
                    result = "Note Saved"
                    setFragmentResult(
                        "key",
                        bundleOf("bundleKey" to result)
                    )
                    navController.navigate(
                        NoteContentFragmentDirections
                            .actionNoteContentFragmentToNoteFragment()
                    )

                }
                else -> {
                    updateNote()
                    navController.popBackStack()
                }
            }
        }
    }

    private fun updateNote() {
        if (note != null) {
            noteActivityViewModel.updateNote(
                Note(
                    note!!.id,
                    contentBinding.etTitle.trimInputSpace(),
                    contentBinding.etNoteContent.trimInputSpace(),
                    note!!.date,
                    color,
                    contentBinding.etNoteImageUrl.trimInputSpace(),
                    checkIfNoteUpdated()
                )
            )
        }
    }

    private fun checkIfNoteUpdated():Boolean{
        if(isNoteTextChanged(oldText = note!!.title,
                currentText =contentBinding.etTitle.trimInputSpace() )){

            return true
        }
        if(isNoteTextChanged(oldText = note!!.imageUrl,
                currentText =contentBinding.etNoteImageUrl.trimInputSpace() )){
            return true
        }
         if(isNoteTextChanged(oldText = note!!.content,
                currentText =contentBinding.etNoteContent.trimInputSpace() )){
             return true
        }

        if(isNoteColorChanged(note!!.color,color))
        {
            return true
        }

        return isNoteUpdated

    }

    /**
     * Displaying data of existing note
     */
    private fun setUpNote() {
        val note = args.note
        val title = contentBinding.etTitle
        val content = contentBinding.etNoteContent
        val createdDate = contentBinding.tvCreatedDate
        val savedImage = contentBinding.etNoteImageUrl

        if (note == null) {
            createdDate.text =
                getString(R.string.created_on, currentDate)
        }

        if (note != null) {
            title.setText(note.title)
            content.setText(note.content)
            createdDate.text = getString(R.string.created_on, note.date)
            savedImage.setText(note.imageUrl)
            color = note.color

            contentBinding.apply {
                job.launch {
                    delay(10)
                    noteContentFragmentParent.setBackgroundColor(color)
                    noteImage.isVisible = true
                }
                toolbarFragmentNoteContent.setBackgroundColor(color)
            }
            activity?.window?.statusBarColor = note.color
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (job.isActive) {
            job.cancel()
        }
    }
}