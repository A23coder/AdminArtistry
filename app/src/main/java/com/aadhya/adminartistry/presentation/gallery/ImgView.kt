package com.aadhya.adminartistry.presentation.gallery

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aadhya.adminartistry.R
import com.aadhya.adminartistry.databinding.ActivityImageViewBinding
import com.bumptech.glide.Glide

class ImgView : AppCompatActivity() {
    private lateinit var _binding: ActivityImageViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityImageViewBinding.inflate(layoutInflater)
        setContentView(_binding.root)
        val img = intent.getStringExtra("image")
        val name = intent.getStringExtra("name")
        _binding.imgImageText.text = name.toString()
        Glide.with(this).load(img).into(_binding.imgImageView)

        getToolbar()
    }

    private fun getToolbar() {
        val toolbarText = intent.getStringExtra("name").toString()
        _binding.toolImageView.title = toolbarText
        _binding.toolImageView.setTitleTextColor(getColor(R.color.black))
        _binding.toolImageView.setNavigationIcon(getDrawable(R.drawable.ic_back))
        setSupportActionBar(_binding.toolImageView)
        _binding.toolImageView.setNavigationOnClickListener {
            finish()
        }
    }
}