package com.aadhya.adminartistry.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aadhya.adminartistry.R
import com.aadhya.adminartistry.data.modal.MehandiItem
import com.aadhya.adminartistry.presentation.gallery.ImgView
import com.bumptech.glide.Glide

class MehandiItemAdapter(
    private val context: Context ,
    private val itemList: List<MehandiItem> ,
) : RecyclerView.Adapter<MehandiItemAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.mNameTxt)
        val imageView: ImageView = itemView.findViewById(R.id.mImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(
            R.layout.mitem , parent , false
        )
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder , position: Int) {
        val item = itemList[position]
        holder.nameTextView.text = item.name
        Glide.with(context).load(item.url).into(holder.imageView)
        holder.itemView.setOnClickListener {
            val intent = Intent(
                context ,
                ImgView::class.java
            ).apply {
                putExtra("image" , item.url)
                putExtra("name" , item.name)
            }
            context.startActivity(intent)
        }
    }


}