/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smox.smoxuser.ui.adapter

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.smox.smoxuser.R
import com.smox.smoxuser.manager.Constants


@BindingAdapter("app:imageUrl", "app:placeHolder", "app:isCircle")
fun loadCircleImage(
    view: ImageView,
    imageUrl: String?,
    placeHolder: Drawable?,
    isCircle: Boolean = false
) {
    if (imageUrl == null) return
    val url = Constants.downloadUrlll(imageUrl)
    var requestOptions: RequestOptions = RequestOptions().centerCrop()
    if (isCircle) {
        requestOptions = requestOptions.circleCrop()
    } else {
        requestOptions = requestOptions.transform(RoundedCorners(10))
    }
    if (placeHolder != null) {
        requestOptions = requestOptions.placeholder(placeHolder)
    }
    Glide.with(view.context)
        .load(url)
        .apply(requestOptions)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(view)

}

@BindingAdapter("app:productUrl")
fun loadProductImage(view: ImageView, imageUrl: String?) {
    if (imageUrl == null) return
    val url = Constants.downloadUrlOfProduct(imageUrl)
    val requestOptions: RequestOptions = RequestOptions()
        .placeholder(R.drawable.small_placeholder)

    Glide.with(view.context)
        .load(url)
        .apply(requestOptions)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(view)
}


@BindingAdapter("app:placerHolderImage")
fun loadplacerHolderImage(view: ImageView, imageUrl: String?) {
    if (imageUrl == null) return
    val url = Constants.downloadUrlOfProduct(imageUrl)
    val requestOptions: RequestOptions = RequestOptions().fitCenter()
        .placeholder(R.drawable.big_placeholder)


    Glide.with(view.context)
        .load(url)
        .apply(requestOptions)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(view)
}

@BindingAdapter("app:placerHolderfav")
fun loadfavImage(view: ImageView, imageUrl: String?) {
    if (imageUrl == null) return
    val url = Constants.downloadUrlOfProduct(imageUrl)
    val requestOptions: RequestOptions = RequestOptions().fitCenter()
        .placeholder(R.drawable.small_placeholder)


    Glide.with(view.context)
        .load(url)
        .apply(requestOptions)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(view)
}


@BindingAdapter("app:productListUrl")
fun loadProductListImage(view: ImageView, imageUrl: String?) {
    if (imageUrl == null) return
    val url = Constants.downloadUrlOfProduct(imageUrl)
    val requestOptions: RequestOptions = RequestOptions()
        .placeholder(R.drawable.img_place_holder)
    Glide.with(view.context)
        .load(url)
        .apply(requestOptions)
        .into(view)
}

@BindingAdapter("app:isGone")
fun bindIsGone(view: View, isGone: Boolean) {
    view.visibility = if (isGone) {
        View.GONE
    } else {
        View.VISIBLE
    }
}

@BindingAdapter("android:src")
fun resource(view: ImageView, resource: Int) {
    view.setImageResource(resource)
}

//@BindingAdapter("layoutHeight")
//fun setLayoutHeight(view: View, height: Int) {
//    val layoutParams = view.layoutParams
//    layoutParams.height = height
//    view.layoutParams = layoutParams
//}
//
//@BindingAdapter("layoutWidth")
//fun setLayoutWidth(view: View, width: Int) {
//    val layoutParams = view.layoutParams
//    layoutParams.width = width
//    view.layoutParams = layoutParams
//}