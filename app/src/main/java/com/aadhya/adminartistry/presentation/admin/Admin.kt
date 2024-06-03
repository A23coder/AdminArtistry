package com.aadhya.adminartistry.presentation.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.aadhya.adminartistry.databinding.ActivityAdminBinding
import com.aadhya.adminartistry.presentation.fragments.AddFrag
import com.aadhya.adminartistry.presentation.fragments.EditFrag

class Admin : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        setupViewPager(binding.tabViewpager)
        binding.tabTablayout.setupWithViewPager(binding.tabViewpager)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun setupViewPager(tabViewpager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(AddFrag() , "Add Data")
        adapter.addFragment(EditFrag() , "Edit Data")

        tabViewpager.adapter = adapter
    }

    class ViewPagerAdapter(supportFragmentManager: FragmentManager) :
        FragmentPagerAdapter(supportFragmentManager) {

        private var fragmentList1: ArrayList<Fragment> = ArrayList()
        private var fragmentTitleList1: ArrayList<String> = ArrayList()
        override fun getCount(): Int {
            return fragmentList1.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList1[position]
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList1[position]
        }

        fun addFragment(fragment: Fragment , fragmentName: String) {
            fragmentList1.add(fragment)
            fragmentTitleList1.add(fragmentName)
        }
    }
}