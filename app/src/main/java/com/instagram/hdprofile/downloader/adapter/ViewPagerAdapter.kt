package com.instagram.hdprofile.downloader.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * ViewPagerAdapter
 * Created on: Sep 21, 2024
 * @since v1.0.0
 */
class ViewPagerAdapter(fragmentManager: FragmentManager) :
    FragmentPagerAdapter(fragmentManager) {
    private val mFragmentList: MutableList<Fragment> = ArrayList()

    override fun getItem(arg0: Int): Fragment {
        return mFragmentList[arg0]
    }

    override fun getCount(): Int {
        return mFragmentList.size
    }

    fun addFragment(fragment: Fragment) {
        mFragmentList.add(fragment)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return null
    }
}