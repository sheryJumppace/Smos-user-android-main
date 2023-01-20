package com.smox.smoxuser.ui.adapter

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.smox.smoxuser.databinding.AdapterCardListBinding
import com.smox.smoxuser.model.Cards

class CardAdapter internal constructor(context: Context, cardActions: CardActions) :
    RecyclerView.Adapter<CardAdapter.ViewHolder>() {

    private var selectedPosition: Int = 0
    var context: Context = context
    var cardActions: CardActions = cardActions
    var cardList = arrayListOf<Cards>();

    inner class ViewHolder(val binding: AdapterCardListBinding, cardActions: CardActions) :
        RecyclerView.ViewHolder(binding.root),
        CompoundButton.OnCheckedChangeListener {

        var cardActions: CardActions = cardActions

        fun setDataToView(position: Int) {
            var data: Cards = cardList.get(position)
            binding.tvCard.text = data.CardHolderName
            binding.tvCardNumber.text = "**** **** **** " + data.CardLastFourDigit

            var date: String
            if (data.CardMonth!!.length == 1) {
                date = "0" + data.CardMonth +"/"+ (data.CardYear)!!.substring(2)
            } else {
                date = data.CardMonth +"/"+ (data.CardYear)!!.substring(2)
            }
            binding.tvExpiryDate.text = date

            if (position == selectedPosition) {
                binding.radioSelected.setChecked(true)
            } else {
                binding.radioSelected.setChecked(false)
            }
            binding.radioSelected.setOnCheckedChangeListener(this)
            binding.ivDelete.setOnClickListener{
                cardActions.onDeleteClick(position)
            }

        }

        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            selectedPosition = adapterPosition
            Handler().post { notifyDataSetChanged() }
        }
    }

    fun doRefresh(cardList: ArrayList<Cards>) {
        this.cardList = cardList
        Handler().post { notifyDataSetChanged() }
    }

    fun getCard():Cards{
        return cardList.get(selectedPosition)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context);
        val binding = AdapterCardListBinding.inflate(inflater)
        return ViewHolder(binding, cardActions)
    }

    override fun getItemCount(): Int {
        return cardList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setDataToView(position)
    }

    public interface CardActions {
        fun onDeleteClick(pos: Int)
    }
}