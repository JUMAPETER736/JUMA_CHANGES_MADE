package com.uyscuti.social.business.adapter

import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.media3.common.util.UnstableApi
import com.uyscuti.social.business.fragment.BusinessFragment
import com.uyscuti.social.business.fragment.CatalogueFragment
import com.uyscuti.social.business.fragment.CategoryFragment
import com.uyscuti.social.business.fragment.ProfileFragment
import com.uyscuti.social.business.fragment.ProfileFragment1


@UnstableApi
class BusinessAdapter(fragmentManager: FragmentManager):  FragmentPagerAdapter(fragmentManager) {

    private var profileFragment: ProfileFragment1? = null
    private var categoryFragment: CategoryFragment? = null
    private var catalogueFragment: CatalogueFragment? = null

    private var businessFragment: BusinessFragment? = null

    @OptIn(UnstableApi::class)
    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> {
                if (businessFragment == null) {
                    businessFragment = BusinessFragment.newInstance("","")
                }
                businessFragment!!
            }

            1 -> {
                if(profileFragment == null) {
                    profileFragment = ProfileFragment1.newInstance("","")
                }
                profileFragment!!
            }

            2 -> {
                if(catalogueFragment == null) {
                    catalogueFragment = CatalogueFragment.newInstance("","")
                }
                catalogueFragment!!
            }

            3 -> {
                if(categoryFragment == null) {
                    categoryFragment = CategoryFragment.newInstance("","")
                }
                categoryFragment!!
            }

            else -> throw IllegalArgumentException("Invalid tab position: $position")
        }
    }

    override fun getCount(): Int {
        return 4
    }

    override fun getPageTitle(position: Int): CharSequence? {
        val title = when(position) {
            0 -> "Market"
            1 -> "Profile"
            2 -> "Catalogue"
            3 -> "Interests"
            else -> ""
        }
        return  title
    }

    fun getProfileFragment(): ProfileFragment1? = profileFragment
    fun getCategoryFragment(): CategoryFragment? = categoryFragment
    fun getCatalogueFragment(): CatalogueFragment? = catalogueFragment

    @OptIn(UnstableApi::class)
    fun getBusinessFragment(): BusinessFragment? = businessFragment

}