package com.smox.smoxuser.ui.adapter

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.smox.smoxuser.databinding.ListItemCategoriesBinding
import com.smox.smoxuser.model.Category

class CategorySelectorAdapter internal constructor(
    context: Context, categoryActions: CategorySelectActions,
    private val isLower: Boolean = true
) :
    RecyclerView.Adapter<CategorySelectorAdapter.ViewHolder>() {

    var context: Context = context
    var categoryActions: CategorySelectActions = categoryActions
    var categoryList = arrayListOf<Category>();


    class ViewHolder(
        binding: ListItemCategoriesBinding,
        categoryActions: CategorySelectActions,
        categoryList: ArrayList<Category>,
        private val lower: Boolean
    ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        var categoryActions: CategorySelectActions = categoryActions
        val binding: ListItemCategoriesBinding = binding
        var categoryList = categoryList
        fun setDataToView(position: Int) {
            binding.root.setOnClickListener(this)
            binding.txtName.text = categoryList.get(position).cat_name
            binding.txtName.isAllCaps = lower
        }

        override fun onClick(v: View?) {
            if (v!!.id == binding.root.id) {
                categoryActions.onItemClick(adapterPosition)
            }
        }
    }

    public fun doRefresh(categoryList: ArrayList<Category>) {
        this.categoryList = categoryList
        Handler().post { notifyDataSetChanged() }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context);
        val binding = ListItemCategoriesBinding.inflate(inflater)
        return ViewHolder(binding, categoryActions, categoryList, isLower)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setDataToView(position)
    }

    public interface CategorySelectActions {
        fun onItemClick(pos: Int)
    }
}