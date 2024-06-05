package com.aadhya.adminartistry.presentation.edit

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.aadhya.adminartistry.R
import com.aadhya.adminartistry.data.utils.Utils
import com.aadhya.adminartistry.databinding.ActivityEditPageBinding
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class EditPage : AppCompatActivity() {
    private val GALLERY_REQUEST_CODE = 123
    private val PERMISSION_REQUEST_CODE = 456
    private lateinit var _binding: ActivityEditPageBinding
    private var imgUri: Uri? = null
    private var timeStamp: String? = null
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var storageRef: StorageReference
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityEditPageBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        progressBar = _binding.progressBar
        database = FirebaseDatabase.getInstance()
        myRef = database.reference.child("images")
        storageRef = FirebaseStorage.getInstance().reference.child("images")

        _binding.btnUploadImage.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this , Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this ,
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES) ,
                        PERMISSION_REQUEST_CODE
                    )
                } else {
                    openGallery()
                }
            } else {
                if (ContextCompat.checkSelfPermission(
                        this , Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this ,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE) ,
                        PERMISSION_REQUEST_CODE
                    )
                } else {
                    openGallery()
                }
            }
        }

        initializeView()
    }

    private fun initializeView() {
        val intent = intent

        val uriString: String? = intent.getStringExtra("image")
        if (uriString.isNullOrEmpty()) {
            _binding.imgView.setImageResource(R.drawable.ic_upload)
        } else {
            val imageUri: Uri = Uri.parse(uriString)
            imgUri = imageUri
            Glide.with(this).load(imageUri).into(_binding.imgView)
        }

        val selectedItem = intent.getStringExtra("subCategory")
        timeStamp = intent.getStringExtra("time")

        if (selectedItem != null) {
            getAdapter(selectedItem)
        }

        val mainCategory = intent.getStringExtra("category")
        _binding.txtmainCategory.text = mainCategory
        var name = intent.getStringExtra("name").toString()
        _binding.edtName.setText(name)
        println("NAME IS $name")
        if (mainCategory == "Mehandi Design") {
            _binding.txtsubCategory.visibility = View.VISIBLE
        } else {
            _binding.txtsubCategory.visibility = View.GONE
        }

        _binding.btnEdit.setOnClickListener {
            name = _binding.edtName.text.toString()
            uploadImageToFirebase { downloadUrl ->
                firebaseDataUpdate(
                    downloadUrl , mainCategory , name , selectedItem
                )
            }
        }
    }

    private fun uploadImageToFirebase(callback: (String) -> Unit) {
        imgUri?.let { uri ->
            progressBar.visibility = View.VISIBLE
            val fileName = UUID.randomUUID().toString()
            val fileRef = storageRef.child("$fileName.jpg")
            fileRef.putFile(uri).addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    progressBar.visibility = View.GONE
                    callback(downloadUri.toString())
                }.addOnFailureListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this , "Failed to get download URL" , Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this , "Image upload failed" , Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this , "No image selected" , Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseDataUpdate(
        imgUri: String? ,
        mainCategory: String? ,
        name: String ,
        selectedItem: String? ,
    ) {
        if (imgUri != null && name.isNotEmpty()) {
            updateData(imgUri , mainCategory , name , selectedItem)
        } else {
            Toast.makeText(this , "Please Fill Data." , Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateData(
        imgUri: String ,
        mainCategory: String? ,
        name: String ,
        selectedItem: String? ,
    ) {
        val data = mapOf(
            "url" to imgUri ,
            "category" to mainCategory ,
            "name" to name ,
            "subCategory" to selectedItem
        )
        Log.d("IMAGERI" , imgUri)
        progressBar.visibility = View.VISIBLE
        timeStamp?.let { timeStamp ->
            Log.d("EditPage" , "Querying with timestamp: $timeStamp")

            myRef.orderByChild("timestamp").equalTo(timeStamp)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (dataSnapshot in snapshot.children) {
                                Log.d("EditPage" , "Found matching record: ${dataSnapshot.key}")
                                dataSnapshot.ref.updateChildren(data).addOnSuccessListener {
                                    progressBar.visibility = View.GONE
                                    Toast.makeText(
                                        this@EditPage ,
                                        "Document updated successfully" ,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Log.d("DATAATATAT" , data.toString())
                                }.addOnFailureListener { e ->
                                    progressBar.visibility = View.GONE
                                    Toast.makeText(
                                        this@EditPage ,
                                        "Error updating document: ${e.message}" ,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            progressBar.visibility = View.GONE
                            Log.d(
                                "EditPage" , "No record found with the given timestamp: $timeStamp"
                            )
                            Toast.makeText(
                                this@EditPage ,
                                "No record found with the given timestamp" ,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        progressBar.visibility = View.GONE
                        Log.d("EditPage" , error.message)
                    }
                })
        } ?: run {
            progressBar.visibility = View.GONE
            Toast.makeText(this , "Timestamp is missing" , Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAdapter(selectedItem: String) {
        val adapter = ArrayAdapter(
            this , android.R.layout.simple_spinner_dropdown_item , Utils.subCategory
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        _binding.txtsubCategory.adapter = adapter

        val spinnerPosition = adapter.getPosition(selectedItem)
        _binding.txtsubCategory.setSelection(spinnerPosition)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent , GALLERY_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int , resultCode: Int , data: Intent?) {
        super.onActivityResult(requestCode , resultCode , data)
        if (resultCode == Activity.RESULT_OK && requestCode == GALLERY_REQUEST_CODE) {
            val selectedImageUri: Uri? = data?.data
            if (selectedImageUri != null) {
                _binding.imgView.setImageURI(selectedImageUri)
                imgUri = selectedImageUri
            } else {
                Toast.makeText(this , "Failed to get image" , Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int ,
        permissions: Array<out String> ,
        grantResults: IntArray ,
    ) {
        super.onRequestPermissionsResult(requestCode , permissions , grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                openGallery()
            } else {
                Toast.makeText(this , "Permission denied" , Toast.LENGTH_SHORT).show()
            }
        }
    }
}
