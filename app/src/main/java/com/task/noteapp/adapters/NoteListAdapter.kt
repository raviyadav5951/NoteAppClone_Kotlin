package com.task.noteapp.adapters

import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.task.noteapp.R
import com.task.noteapp.common.NoteView
import com.task.noteapp.databinding.NoteItemLayoutBinding
import com.task.noteapp.model.Note
import com.task.noteapp.ui.fragments.NoteListFragmentDirections
import com.task.noteapp.utils.hideKeyboard
import com.task.noteapp.utils.loadHiRezThumbnail
import timber.log.Timber


class NoteListAdapter(private val deleteClick: DeleteListener) : ListAdapter<
        Note,
        NoteListAdapter.NotesViewHolder>(
    DiffUtilCallback()
) {

    inner class NotesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contentBinding = NoteItemLayoutBinding.bind(itemView)
        private val noteView:NoteView=contentBinding.noteView

        val editedTag: MaterialTextView = noteView.editedTagSymbol
        val title: MaterialTextView = noteView.noteTitle
        val content: TextView = noteView.noteDescription
        val date: MaterialTextView = noteView.noteDates
        val image: ImageView = noteView.noteImage
        val imageDeleteNote:ImageView=noteView.noteDeleteImage

        val parent: MaterialCardView = contentBinding.noteItemLayoutParent

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        return NotesViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.note_item_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {

        getItem(position).let { note ->

            holder.apply {
                parent.transitionName = "recyclerView_${note.id}"


                title.text = note.title
                content.text=note.content
                date.text = note.date

                if(note.edited)
                    editedTag.visibility=View.VISIBLE
                else
                    editedTag.visibility=View.GONE

                if (!note.imageUrl.isNullOrBlank() &&  Patterns.WEB_URL.matcher(note.imageUrl).matches()
                ) {

                    Timber.d("image visible url=${note.imageUrl}")
                    image.visibility = View.VISIBLE

                    itemView.context.loadHiRezThumbnail(note.imageUrl, image)

                } else {
                    Timber.d("image not visible")
                    Glide.with(itemView).clear(image)
                    image.isVisible = false
                }

                parent.setCardBackgroundColor(note.color)

                itemView.setOnClickListener {
                    redirectToNoteContentFragment(it, parent, note)
                }
                content.setOnClickListener {
                    redirectToNoteContentFragment(it, parent, note)
                }

                imageDeleteNote.setOnClickListener {
                    deleteClick.onDeleteNote(note)
                }

            }
        }
    }

    private fun redirectToNoteContentFragment(view: View, parentView: View, note: Note) {
        val action = NoteListFragmentDirections.actionNoteFragmentToNoteContentFragment()
            .setNote(note)
        val extras = FragmentNavigatorExtras(parentView to "recyclerView_${note.id}")
        view.hideKeyboard()
        Navigation.findNavController(view).navigate(action, extras)
    }


    interface DeleteListener {
        fun onDeleteNote(note: Note)
    }
}