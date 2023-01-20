package com.smox.smoxuser.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ItemRowAddCardBinding
import com.smox.smoxuser.model.SavedCardListResponse

class NewCardAdapter internal constructor(var context: Context, var cardActions: CardActions) :
    RecyclerView.Adapter<NewCardAdapter.ViewHolder>() {

    private var selectedPosition: Int = 0
    var cardList = arrayListOf<SavedCardListResponse.CardList>();

    inner class ViewHolder(val binding: ItemRowAddCardBinding, var cardActions: CardActions) :
        RecyclerView.ViewHolder(binding.root),
        CompoundButton.OnCheckedChangeListener {

        @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
        fun setDataToView(position: Int) {
            val data: SavedCardListResponse.CardList = cardList.get(position)
            binding.txtHolderName.text = data.name
            binding.txtCardNumber.text = "**** **** **** " + data.last4


            if (selectedPosition == position) {
                binding.imgCardArrow.setBackgroundResource(R.drawable.icon_circle_check)
            } else {
                binding.imgCardArrow.setBackgroundResource(R.drawable.icon_circle_uncheck)
            }

            binding.rlCardBG.setOnClickListener {
                selectedPosition = position
                cardActions.onCardSelected(position)
                notifyDataSetChanged()
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            selectedPosition = adapterPosition
            notifyDataSetChanged()
        }
    }

    fun doRefresh(cardList: ArrayList<SavedCardListResponse.CardList>) {
        this.cardList.clear()
        this.cardList.addAll(cardList)
        notifyDataSetChanged()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context);
        val binding = ItemRowAddCardBinding.inflate(inflater)
        return ViewHolder(binding, cardActions)
    }

    override fun getItemCount(): Int {
        return cardList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setDataToView(position)
    }

    public interface CardActions {
        fun onCardSelected(pos: Int)
    }
}