/**
 * Copyright (C) 2018 Google Inc. All Rights Reserved.
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

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.smox.smoxuser.R
import kotlinx.android.synthetic.main.inventory_item.view.sku_description
import kotlinx.android.synthetic.main.inventory_item.view.sku_image
import kotlinx.android.synthetic.main.inventory_item.view.sku_price
import kotlinx.android.synthetic.main.inventory_item.view.sku_title

/**
 * This is an [AugmentedSkuDetails] adapter. It can be used anywhere there is a need to display a
 * list of AugmentedSkuDetails. In this app it's used to display both the list of subscriptions and
 * the list of in-app products.
 */
open class SkuDetailsAdapter : RecyclerView.Adapter<SkuDetailsAdapter.SkuDetailsViewHolder>() {


    override fun getItemCount() = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkuDetailsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
                R.layout.inventory_item, parent, false
        )
        return SkuDetailsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SkuDetailsViewHolder, position: Int) {
    }

    fun getItem(position: Int) = 0



    /**
     * In the spirit of keeping simple things simple: this is a friendly way of allowing clients
     * to listen to clicks. You should consider doing this for all your other adapters.
     */


    inner class SkuDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {

            }
        }



        private fun onDisabled(enabled: Boolean, res: Resources) {
            if (enabled) {
                itemView?.apply {
                    setBackgroundColor(res.getColor(R.color.colorAccentLight))
                    sku_title.setTextColor(res.getColor(R.color.textColor))
                    sku_description.setTextColor(res.getColor(R.color.textColor))
                    sku_price.setTextColor(res.getColor(R.color.textColor))
                    sku_image.setColorFilter(null)
                }
            } else {
                itemView?.apply {
                    setBackgroundColor(res.getColor(R.color.textDisabledHint))
                    val color = res.getColor(R.color.imgDisableHint)
                    sku_image.setColorFilter(color)
                    sku_title.setTextColor(color)
                    sku_description.setTextColor(color)
                    sku_price.setTextColor(color)
                }
            }
        }

        /**
         * Keeping simple things simple, the icons are named after the SKUs. This way, there is no
         * need to create some elaborate system for matching icons to SKUs when displaying the
         * inventory to users. It is sufficient to do
         *
         * ```
         * sku_image.setImageResource(resources.getIdentifier(sku, "drawable", view.context.packageName))
         *
         * ```
         *
         * Alternatively, in the case where more than one SKU should match the same drawable,
         * you can check with a when{} block. In this sample app, for instance, both gold_monthly and
         * gold_yearly should match the same gold_subs_icon; so instead of keeping two copies of
         * the same icon, when{} is used to set imgName
         */
        private fun getSkuDrawableId(sku: String, view: View): Int {
            var imgName: String = when {
                sku.startsWith("gold_") -> "gold_subs_icon"
                else -> sku
            }
            val drawableId = view.resources.getIdentifier(imgName, "drawable",
                    view.context.packageName)
            return drawableId
        }
    }
}