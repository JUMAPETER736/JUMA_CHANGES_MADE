package com.uyscuti.social.circuit

import android.graphics.Bitmap
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
//import android.support.annotation.Dimension
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.uyscuti.social.chatsuit.commons.ImageLoader
import com.uyscuti.social.chatsuit.dialogs.DialogsListAdapter
import com.uyscuti.social.circuit.data.model.Dialog

import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
abstract class MainDialogsFragment : Fragment(), DialogsListAdapter.OnDialogClickListener<Dialog>,
    DialogsListAdapter.OnDialogLongClickListener<Dialog> {
    protected lateinit var imageLoader: ImageLoader
    protected lateinit var dialogsAdapter: DialogsListAdapter<Dialog>
//    private lateinit var dialogRepository: DialogRepository
    var isGroup = false

    private var selectedDialogs = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        dialogRepository = DialogRepository(ChatDatabase.getInstance(requireContext()).dialogDao())

        imageLoader = ImageLoader { imageView, url, _ ->
            if (url!!.isNotEmpty()) {
//                Picasso.get().load(url).into(imageView)

                var radius = 0f

                radius = if (imageView.id == com.uyscuti.social.chatsuit.R.id.dialogLastMessageUserAvatar) {
                    resources.getDimension(R.dimen.sender_radius)
                } else {
                    resources.getDimension(R.dimen.dialog_radius)
                }

                Glide.with(this)
                    .asBitmap()
                    .load(url)
                    .placeholder(R.drawable.user)
                    .error(R.drawable.user)
                    .centerCrop()
                    .into(object : SimpleTarget<Bitmap>() {

                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                        ) {
                            val drawable = RoundedBitmapDrawableFactory.create(resources, resource)

//                            drawable.cornerRadius = radius
                            drawable.isCircular = true

                            val marginDrawable = InsetDrawable(drawable, 0, 0, 10, 0)
                            imageView.setImageDrawable(marginDrawable)
                        }
                    })
            } else {
//                Log.d("Dialogs", "No Images Found For Dialog")

//                val holder = if (di)

                Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.user)
                    .placeholder(R.drawable.user)
                    .error(R.drawable.user)
                    .centerCrop()
                    .into(object : SimpleTarget<Bitmap>() {

                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                        ) {
                            val drawable = RoundedBitmapDrawableFactory.create(resources, resource)

//                            drawable.cornerRadius = radius
                            drawable.isCircular = true

                            val marginDrawable = InsetDrawable(drawable, 0, 0, 10, 0)
                            imageView.setImageDrawable(marginDrawable)
                        }
                    })
            }
        }
    }




}