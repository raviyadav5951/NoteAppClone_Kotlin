package com.task.noteapp.common

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.textview.MaterialTextView
import com.task.noteapp.R
import com.task.noteapp.databinding.CustomNoteLayoutBinding


class NoteView(context: Context, @Nullable attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private val binding =
        CustomNoteLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    private var noteItemTitle: MaterialTextView
    private var editedTag: MaterialTextView
    private var noteContentItemTitle: TextView
    private var noteDate: MaterialTextView
    private var imgNote: ImageView
    private var imgDeleteNote: ImageView


    private var imageDeleteDrawable: Drawable? = context.getDrawable(R.drawable.ic_round_delete_24)

    private var title: String?
    private var description: String?
    private var date: String?

    init {
        imgNote = binding.itemNoteImage
        imgDeleteNote = binding.imgDeleteNote

        editedTag = binding.editedTag
        noteItemTitle = binding.noteItemTitle
        noteDate = binding.noteDate
        noteContentItemTitle = binding.noteContentItemTitle

        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.NoteView, 0, 0)

        try {
            title = typedArray.getString(R.styleable.NoteView_setTitle)
            description = typedArray.getString(R.styleable.NoteView_setDescription)
            date = typedArray.getString(R.styleable.NoteView_setDate)

            imageDeleteDrawable =
                typedArray.getDrawable(R.styleable.NoteView_setDeleteImageDrawable)
            imgDeleteNote.setImageDrawable(imageDeleteDrawable)

            noteItemTitle.text = title
            noteContentItemTitle.text = description
            noteDate.text = date
        } finally {
            typedArray.recycle()
        }
    }


    //getters to access the private views

    val noteTitle get() = noteItemTitle
    val noteDescription get() = noteContentItemTitle
    val noteDates get() = noteDate
    val editedTagSymbol get() = editedTag

    val noteImage get() = imgNote
    val noteDeleteImage get() = imgDeleteNote




}