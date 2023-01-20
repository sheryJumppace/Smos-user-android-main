package com.smox.smoxuser.ui.adapter

import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.CustProductItemRowBinding
import com.smox.smoxuser.model.Products
import com.smox.smoxuser.utils.shortToast
import java.util.*

class ProductAdapter(val context: Context, itemClickListner: ItemClickListner) :
    RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    var products: ArrayList<Products.ProductItem>? = ArrayList()
    var itemClickListner: ItemClickListner? = null

    init {
        this.itemClickListner = itemClickListner
    }

    override fun getItemCount(): Int {
        return if (products == null) 0 else products!!.size
    }

    inner class ViewHolder : RecyclerView.ViewHolder {
        var listItemProductBinding: CustProductItemRowBinding? = null

        constructor(binding: CustProductItemRowBinding) : super(binding.root) {
            listItemProductBinding = binding
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val binding: ViewDataBinding

        binding = DataBindingUtil.inflate<CustProductItemRowBinding>(
            inflater,
            R.layout.cust_product_item_row, viewGroup, false
        )

        return ViewHolder(binding)
    }

    fun setData(product: ArrayList<Products.ProductItem>) {
        products?.clear()
        products?.addAll(product)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val product: Products.ProductItem = products!![position]
            holder.apply {
                listItemProductBinding!!.product = product
                listItemProductBinding!!.txtPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            }

            holder.itemView.setOnClickListener {
                itemClickListner?.onItemClickListner(product)
            }
            holder.listItemProductBinding?.btnAddToCart?.setOnClickListener {
                if (product.is_cart_added == 0) {
                    if (product.stock == 0)
                        shortToast("Product is out of stock.")
                    else
                        itemClickListner?.onAddToCartClickListner(product, position)
                } else
                    shortToast("Product already added.")
            }

            if (product.is_cart_added == 0)
                if (product.stock == 0) {
                    holder.listItemProductBinding?.btnAddToCart?.background =
                        ContextCompat.getDrawable(context, R.drawable.rect_border_red)
                    holder.listItemProductBinding?.btnAddToCart?.text=context.getString(R.string.out_of_stock)
                }
                else {
                    holder.listItemProductBinding?.btnAddToCart?.background =
                        ContextCompat.getDrawable(context, R.drawable.rect_border_yellow)
                    holder.listItemProductBinding?.btnAddToCart?.text=context.getString(R.string.add_to_cart)
                }
            else{
                holder.listItemProductBinding?.btnAddToCart?.background =
                    ContextCompat.getDrawable(context, R.drawable.rect_border_grey)
                holder.listItemProductBinding?.btnAddToCart?.text=context.getString(R.string.added_to_cart)
            }

        } catch (ex: Exception) {
        }
    }

    interface ItemClickListner {
        fun onItemClickListner(product: Products.ProductItem)
        fun onAddToCartClickListner(product: Products.ProductItem, position: Int)
    }
}
