package com.aadhya.adminartistry.adapter

import android.content.ContentValues.TAG
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.aadhya.adminartistry.R
import com.aadhya.adminartistry.data.modal.MehandiItem
import com.aadhya.adminartistry.presentation.edit.EditPage
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class EditAdapter(
    private var data: List<MehandiItem> ,
    var context: Context ,
    var myRef: DatabaseReference ,
) : RecyclerView.Adapter<EditAdapter.MyViewViewHolder>() {
    class MyViewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardImage: ImageView = itemView.findViewById(R.id.catImage)
        val cardName: TextView = itemView.findViewById(R.id.txt_name)
        val cateEdit: ImageView = itemView.findViewById(R.id.icEdit)
        val cateDelete: ImageView = itemView.findViewById(R.id.icDelete)

    }


    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): MyViewViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.edit_items , parent , false)
        return MyViewViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewViewHolder , position: Int) {
        val item = data[position]
        holder.cardName.text = item.name
        Glide.with(holder.itemView)
            .load(item.url)
            .into(holder.cardImage)
        myRef = FirebaseDatabase.getInstance().reference.child("images")
        val query = myRef.orderByChild("timestamp").equalTo(item.timestamp)

        holder.cateEdit.setOnClickListener {
            println("timestamp.... ${item.timestamp.toString()}")
            val intent = Intent(context , EditPage::class.java).apply {
                putExtra("name" , item.name)
                putExtra("image" , item.url)
                putExtra("subCategory" , item.subCategory)
                putExtra("category" , item.category)
                putExtra("time" , item.timestamp.toString())
            }
            context.startActivity(intent)
        }
        holder.cateDelete.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Are you sure you want to Delete this ${item.name}?")
            builder.setTitle("Delete Item!!")
            builder.setCancelable(true)

            builder.setPositiveButton("Yes") { dialog, which ->
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (appleSnapshot in snapshot.children) {
                                appleSnapshot.ref.removeValue().addOnSuccessListener {
                                    Toast.makeText(context, "${item.name} deleted successfully.", Toast.LENGTH_SHORT).show()
                                }.addOnFailureListener { e ->
                                    Toast.makeText(context, "Failed to delete: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "No record found with the given timestamp.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "onCancelled", error.toException())
                        Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            builder.setNegativeButton("No" , object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface? , which: Int) {
                    dialog?.cancel()
                }
            })
            val alertDialog = builder.create()
            alertDialog.show()
        }
//        holder.itemView.setOnClickListener {
//            val intent = Intent(context , EditPage::class.java).apply {
//                  putExtra("category" , holder.cardText.text)
//            }
//            context.startActivity(intent)
//        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}