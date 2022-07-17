package com.task.noteapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.res.use
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.FutureTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

fun View.hideKeyboard() =
    (context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
        .hideSoftInputFromWindow(windowToken, HIDE_NOT_ALWAYS)


fun View.trimInputSpace()=(this as EditText).text.toString().trim()

fun Context.loadHiRezThumbnail(
    imageUrl: String?,
    image: ImageView
) = Glide.with(this)
    .load(imageUrl)
    .override(500, 500)
    .diskCacheStrategy(DiskCacheStrategy.ALL)
    .thumbnail(0.1f)
    .transition(DrawableTransitionOptions.withCrossFade(200))
    .into(image)


/**
 * Retrieve a color from the current [android.content.res.Resources.Theme].
 */
@ColorInt
@SuppressLint("Recycle")
fun Context.themeColor(
    @AttrRes themeAttrId: Int
): Int {
    return obtainStyledAttributes(
        intArrayOf(themeAttrId)
    ).use {
        it.getColor(0, Color.MAGENTA)
    }
}

fun isNoteTextChanged(oldText:String,currentText:String):Boolean{
    return oldText!=currentText
}

fun isNoteColorChanged(oldId:Int,currentId:Int):Boolean=oldId!=currentId

fun Context.shortToast(message: String?) = makeText(this, message, LENGTH_SHORT).show()