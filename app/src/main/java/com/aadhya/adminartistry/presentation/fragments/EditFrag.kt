package com.aadhya.adminartistry.presentation.fragments

import android.R
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aadhya.adminartistry.adapter.EditAdapter
import com.aadhya.adminartistry.data.modal.MehandiItem
import com.aadhya.adminartistry.data.utils.Utils
import com.aadhya.adminartistry.databinding.LayoutEditFragmentBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditFrag : Fragment() {
    private lateinit var _binding: LayoutEditFragmentBinding
    private lateinit var recyclerView: RecyclerView
    private val itemList = mutableListOf<MehandiItem>()
    private lateinit var adapter: EditAdapter
    private lateinit var myRef: DatabaseReference
    var categoryname = ""
    var sCategoryname = ""

    var selectedCategory = ""
    var selectedSubCategory = ""

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)
        getSpinnerAdapter()
        initListeners()
    }

    override fun onCreateView(
        inflater: LayoutInflater , container: ViewGroup? , savedInstanceState: Bundle? ,
    ): View {
        _binding = LayoutEditFragmentBinding.inflate(inflater , container , false)
        recyclerView = _binding.editRecyclerView
        myRef = FirebaseDatabase.getInstance().reference.child("images")
        adapter = EditAdapter(itemList , requireContext() , myRef)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        _binding.btnSearch.setOnClickListener {
            if (selectedCategory.isEmpty()) {
                Toast.makeText(requireContext() , "Please Select the Category" , Toast.LENGTH_SHORT)
                    .show()
            } else {
                getFirebaseDataList(selectedCategory)
            }
        }
        return _binding.root
    }

    private fun getFirebaseDataList(selectedCategory: String) {
        val myRef = FirebaseDatabase.getInstance().getReference("images")

        myRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                itemList.clear()
                for (dataSnapshot in snapshot.children) {
                    val item = dataSnapshot.getValue(MehandiItem::class.java)
                    item?.let {
                        if (selectedCategory == "Mehandi Design") {
                            if (it.subCategory?.trim() == selectedSubCategory.trim()) {
                                itemList.add(it)
                            }
                        } else {
                            if (it.category?.trim() == selectedCategory.trim()) {
                                itemList.add(it)
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged()
                Log.d("FirebaseData" , "Items: $itemList")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError" , "Error: ${error.message}")
            }
        })
    }

    private fun getSpinnerAdapter() {
        val mainCateAdapter = ArrayAdapter(
            requireContext() , R.layout.simple_spinner_dropdown_item , Utils.mainCategory
        )
        _binding.mainCategory.adapter = mainCateAdapter
        _binding.mainCategory.isEnabled = true
    }

    private fun initListeners() {


        _binding.mainCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*> , view: View? , position: Int , id: Long ,
            ) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                selectedCategory = if (selectedItem == "Select Category") "" else selectedItem
                updateSubCategorySpinner()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        _binding.subCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*> , view: View? , position: Int , id: Long ,
            ) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                selectedSubCategory = if (selectedItem == "Select Subcategory") "" else selectedItem
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun updateSubCategorySpinner() {
        if (selectedCategory == "Mehandi Design") {
            val subCateAdapter = ArrayAdapter(
                requireContext() , R.layout.simple_spinner_dropdown_item , Utils.subCategory
            )
            _binding.subCategory.adapter = subCateAdapter
            _binding.subCategory.visibility = View.VISIBLE
            _binding.subCategory.isEnabled = true
        } else {
            _binding.subCategory.visibility = View.GONE
            _binding.subCategory.adapter = null
            _binding.subCategory.isEnabled = false
        }
    }
}
