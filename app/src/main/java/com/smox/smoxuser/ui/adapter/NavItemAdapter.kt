package com.smox.smoxuser.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smox.smoxuser.R

class NavItemAdapter(private val myItemClickListener: MyItemClickListener) :
    RecyclerView.Adapter<NavItemAdapter.MyViewHolder>() {


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtHeading: TextView = itemView.findViewById(R.id.txtHeading)
        val txtSubHeading: TextView = itemView.findViewById(R.id.txtSubHeading)
        val img: ImageView = itemView.findViewById(R.id.img)
        val rlRoot: RelativeLayout = itemView.findViewById(R.id.rlRoot)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
       return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_row,parent,false))
    }


    override fun getItemCount(): Int {
        return 8
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        when(position){
            0-> {
                holder.img.resources.getDrawable(R.drawable.ic_icon_ionic_ios_search)
                holder.txtHeading.text = "Profile"
                holder.txtSubHeading.text = "See , edit your profile"
                holder.rlRoot.setOnClickListener { myItemClickListener.clicked(position) }
            }
            1-> {
                holder.img.resources.getDrawable(R.drawable.ic_icon_ionic_ios_search)
                holder.txtHeading.text = "Search"
                holder.txtSubHeading.text = "Search services , location here....."
                holder.rlRoot.setOnClickListener { myItemClickListener.clicked(position) }

            }
            2-> {
                holder.img.resources.getDrawable(R.drawable.ic_icon_ionic_ios_search)
                holder.txtHeading.text = "Appointments"
                holder.txtSubHeading.text = "Check pending, approved, etc....."
                holder.rlRoot.setOnClickListener { myItemClickListener.clicked(position) }

            }

            3-> {
                holder.img.resources.getDrawable(R.drawable.ic_icon_ionic_ios_search)
                holder.txtHeading.text = "Favorites"
                holder.txtSubHeading.text = "Your favorites are saved here....."
                holder.rlRoot.setOnClickListener { myItemClickListener.clicked(position) }

            }
            4-> {
                holder.img.resources.getDrawable(R.drawable.ic_icon_ionic_ios_search)
                holder.txtHeading.text = "Events"
                holder.txtSubHeading.text = "Check events here....."
                holder.rlRoot.setOnClickListener { myItemClickListener.clicked(position) }

            }
            5-> {
                holder.img.resources.getDrawable(R.drawable.ic_icon_ionic_ios_search)
                holder.txtHeading.text = "Products"
                holder.txtSubHeading.text = "Browse our products here...…."
                holder.rlRoot.setOnClickListener { myItemClickListener.clicked(position) }

            }
            6-> {
                holder.img.resources.getDrawable(R.drawable.ic_icon_ionic_ios_search)
                holder.txtHeading.text = "My orders"
                holder.txtSubHeading.text = "Know about your orders......"
                holder.rlRoot.setOnClickListener { myItemClickListener.clicked(position) }

            }
            7 -> {
                holder.img.resources.getDrawable(R.drawable.ic_icon_ionic_ios_search)
                holder.txtHeading.text = "About us"
                holder.txtSubHeading.text = "Wanna know us ?...…."
                holder.rlRoot.setOnClickListener { myItemClickListener.clicked(position) }

            }
            8 -> {
                holder.img.resources.getDrawable(R.drawable.ic_icon_ionic_ios_search)
                holder.txtHeading.text = "Contact us"
                holder.txtSubHeading.text = "Connect with us...…."
                holder.rlRoot.setOnClickListener { myItemClickListener.clicked(position) }

            }
        }

    }


    interface MyItemClickListener {
        fun clicked(id: Int)
    }


}

