package com.app.imcovery.recyclerdraghelper

import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.smox.smoxuser.model.Service
import com.smox.smoxuser.ui.adapter.ServiceAdapter
import java.util.*

class RecyclerDragHelperService(var list: ArrayList<Service>, var mAdapter: ServiceAdapter) :
    ItemTouchHelper.Callback() {

    val TAG = RecyclerDragHelperService::class.java.simpleName

    var onDragListener: OnDragListener? = null
    var onSwipeListener: OnSwipeListener? = null
    var onDragStartEndListener: OnDragStartEndListener? = null

    private fun onMoved(fromPos: Int, toPos: Int) {
        list.removeAt(toPos)
        mAdapter.notifyItemRemoved(toPos)
    }

    private fun onItemMoved(fromPosition: Int, toPosition: Int) {
        mAdapter.notifyItemMoved(fromPosition, toPosition)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {

        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            Log.e(TAG, "ActionState: Drag")
            viewHolder?.itemView?.alpha = 0.5f
            val nullableInt: Int? = viewHolder?.adapterPosition
            if (nullableInt != null) {
                // allowed if nullableInt could not have changed since the null check
                val nonNullableInt: Int = nullableInt
                onDragStartEndListener?.onDragStartListener(nonNullableInt)
            }
        } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            Log.e(TAG, "ActionState: Idle")
        } else if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            Log.e(TAG, "ActionState: Swipe")
        }

        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        viewHolder.itemView.alpha = 1.0f
        onDragStartEndListener?.onDragEndListener(viewHolder.adapterPosition)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        onMoved(viewHolder.oldPosition, viewHolder.adapterPosition)
        onSwipeListener?.onSwipeItemListener()
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        var dragFlags = 0
        val swipeFlags = 0
        if (list[viewHolder.adapterPosition].isDraggable()) {
            dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        }
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {

        val fromPos = viewHolder.adapterPosition
        val toPos = target.adapterPosition

        if (list[fromPos].isDraggable() && list[toPos].isDraggable()) {
            onItemMoved(fromPos, toPos)
        }
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    fun setOnDragItemListener(onDragListener: OnDragListener): RecyclerDragHelperService {
        this.onDragListener = onDragListener
        return this
    }

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    fun refreshList(newList: ArrayList<Service>) {
        list = newList
        mAdapter.notifyDataSetChanged()
    }

}