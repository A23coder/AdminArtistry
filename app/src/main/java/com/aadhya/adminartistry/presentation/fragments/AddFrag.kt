package com.aadhya.adminartistry.presentation.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.aadhya.adminartistry.data.utils.Utils
import com.aadhya.adminartistry.R
import com.aadhya.adminartistry.databinding.LayoutAddFragmentBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AddFrag : Fragment() {
    private lateinit var binding: LayoutAddFragmentBinding
    private val GALLERY_REQUEST_CODE = 123
    private val PERMISSION_REQUEST_CODE = 456
    private var selectedCategory = ""
    private var selectedSubCategory = ""
    private var imgUri: String = ""
    private lateinit var mDatabaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater ,
        container: ViewGroup? ,
        savedInstanceState: Bundle? ,
    ): View {
        binding = LayoutAddFragmentBinding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)
        init()
        mDatabaseReference = FirebaseDatabase.getInstance().reference
    }

    private fun init() {
        setupSpinnerAdapters()
        initListeners()
    }

    private fun setupSpinnerAdapters() {
        val mainCateAdapter = ArrayAdapter(
            requireContext() , android.R.layout.simple_spinner_dropdown_item , Utils.mainCategory
        )
        binding.mainCategory.adapter = mainCateAdapter
        binding.mainCategory.isEnabled = true
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initListeners() {
        binding.mainCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*> , view: View? , position: Int , id: Long ,
            ) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                selectedCategory = if (selectedItem == "Select Category") "" else selectedItem
                updateSubCategorySpinner()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.subCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*> , view: View? , position: Int , id: Long ,
            ) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                selectedSubCategory = if (selectedItem == "Select Subcategory") "" else selectedItem
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.btnUpload.setOnClickListener {
            if (isFormValid()) {
                uploadImageToFirebaseStorage(imgUri)
            }
        }

        binding.btnAddImage.setOnClickListener {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(
                        requireContext() ,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        requireActivity() ,
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES) ,
                        PERMISSION_REQUEST_CODE
                    )
                } else
                    openGallery()
            } else {
                if (ContextCompat.checkSelfPermission(
                        requireContext() ,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        requireActivity() ,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE) ,
                        PERMISSION_REQUEST_CODE
                    )
                } else {
                    openGallery()
                }
            }
        }
    }

    private fun checkPermissionAndOpenGallery(permission: String) {
        if (ContextCompat.checkSelfPermission(
                requireContext() ,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity() ,
                arrayOf(permission) ,
                PERMISSION_REQUEST_CODE
            )
        } else {
            openGallery()
        }
    }

    private fun updateSubCategorySpinner() {
        if (selectedCategory == "Mehandi Design") {
            val subCateAdapter = ArrayAdapter(
                requireContext() , android.R.layout.simple_spinner_dropdown_item , Utils.subCategory
            )
            binding.subCategory.adapter = subCateAdapter
            binding.subCategory.visibility = View.VISIBLE
            binding.subCategory.isEnabled = true
        } else {
            binding.subCategory.visibility = View.GONE
            binding.subCategory.adapter = null
            binding.subCategory.isEnabled = false
        }
    }

    private fun isFormValid(): Boolean {
        return when {
            binding.imgAddView.drawable == null -> {
                Toast.makeText(requireContext() , "Please choose an image" , Toast.LENGTH_SHORT)
                    .show()
                false
            }

            selectedCategory.isEmpty() -> {
                Toast.makeText(requireContext() , "Please select a category" , Toast.LENGTH_SHORT)
                    .show()
                false
            }

            selectedCategory == "Mehandi Design" && selectedSubCategory.isEmpty() -> {
                Toast.makeText(
                    requireContext() ,
                    "Please select a subcategory" ,
                    Toast.LENGTH_SHORT
                ).show()
                false
            }

            binding.edtName.text.isEmpty() -> {
                Toast.makeText(requireContext() , "Please enter a name" , Toast.LENGTH_SHORT).show()
                false
            }

            else -> true
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        startActivityForResult(intent , GALLERY_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int , resultCode: Int , data: Intent?) {
        super.onActivityResult(requestCode , resultCode , data)
        if (resultCode == Activity.RESULT_OK && requestCode == GALLERY_REQUEST_CODE) {
            imgUri = data?.data.toString()
            if (imgUri.isNotEmpty()) {
                binding.imgAddView.setImageURI(data?.data)
            }
        }
    }

    private fun uploadImageToFirebaseStorage(imgUri: String) {
        val storageReference = FirebaseStorage.getInstance().reference
        val imageRef = storageReference.child("images/${UUID.randomUUID()}.jpg")

        imageRef.putFile(imgUri.toUri()).addOnSuccessListener { taskSnapshot ->
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                saveImageDetailsToDatabase(downloadUrl)
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(
                requireContext() ,
                "Upload failed: ${exception.message}" ,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun saveImageDetailsToDatabase(imageUrl: String) {
        val imageDetails = hashMapOf(
            "url" to imageUrl ,
            "name" to binding.edtName.text.toString() ,
            "category" to selectedCategory ,
            "timestamp" to System.currentTimeMillis().toString()
        )
        if (selectedCategory == "Mehandi Design") {
            imageDetails["subCategory"] = selectedSubCategory
        }
        mDatabaseReference.child("images").push().setValue(imageDetails).addOnSuccessListener {
            Toast.makeText(requireContext() , "Your data has been uploaded" , Toast.LENGTH_SHORT)
                .show()
            clearForm()
        }.addOnFailureListener { exception ->
            Toast.makeText(
                requireContext() ,
                "Failed to upload data: ${exception.message}" ,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun clearForm() {
        binding.edtName.text.clear()
        binding.imgAddView.setImageResource(R.drawable.ic_upload)
        binding.mainCategory.setSelection(0)
        binding.subCategory.setSelection(0)
        binding.subCategory.visibility = View.GONE
        selectedCategory = ""
        selectedSubCategory = ""
        imgUri = ""
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int ,
        permissions: Array<out String> ,
        grantResults: IntArray ,
    ) {
        super.onRequestPermissionsResult(requestCode , permissions , grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(requireContext() , "Permission denied" , Toast.LENGTH_SHORT).show()
            }
        }
    }
}
