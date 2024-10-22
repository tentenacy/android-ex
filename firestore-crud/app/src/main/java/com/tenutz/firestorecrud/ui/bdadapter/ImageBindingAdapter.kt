package com.tenutz.firestorecrud.ui.bdadapter

import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageButton
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.esafirm.imagepicker.model.Image
import com.tenutz.firestorecrud.R
import com.tenutz.firestorecrud.util.toPx

object ImageBindingAdapter {

    @JvmStatic
    @BindingAdapter("bind:showImage", "bind:imageRadius", "bind:placeholder", requireAll = false)
    fun showImage(imageView: ImageView, uri: Uri?, radius: Int?, placeholderDrawable: Drawable?) {
        Glide.with(imageView.context)
            .asBitmap()
            .load(uri.toString())
            .apply(
                RequestOptions.bitmapTransform(
                    MultiTransformation(
                        *listOfNotNull(
                            CenterCrop(),
                            radius?.let {
                                it.takeIf { it > 0 }?.let {
                                    RoundedCorners(it.toPx.toInt())
                                }
                            } ?: RoundedCorners(8.toPx.toInt()),
                        ).toTypedArray()
                    )
                )
            ).run {
                placeholderDrawable?.let { placeholder(it) } ?: this
            }.into(imageView)
    }
}