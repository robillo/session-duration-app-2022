package com.firstapp.robinpc.tongue_twisters_deluxe.ui.reading.ads_pager_adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.reading.ads_pager_fragment.ReadingAdsPageFragment
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.nativead.NativeAd

class AdsPagerAdapter(
        fragmentManager: FragmentManager,
        private val loadedAds: ArrayList<NativeAd>,
        private val pageType: Int
):
        FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var firstFragment: ReadingAdsPageFragment = ReadingAdsPageFragment.newInstance()
    private var secondFragment: ReadingAdsPageFragment = ReadingAdsPageFragment.newInstance()
    private var thirdFragment: ReadingAdsPageFragment = ReadingAdsPageFragment.newInstance()
    private var fourthFragment: ReadingAdsPageFragment = ReadingAdsPageFragment.newInstance()
    private var fifthFragment: ReadingAdsPageFragment = ReadingAdsPageFragment.newInstance()
    private var elseFragment: ReadingAdsPageFragment = ReadingAdsPageFragment.newInstance()

    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> firstFragment.setUnifiedAd(loadedAds[position], pageType)
            1 -> secondFragment.setUnifiedAd(loadedAds[position], pageType)
            2 -> thirdFragment.setUnifiedAd(loadedAds[position], pageType)
            3 -> fourthFragment.setUnifiedAd(loadedAds[position], pageType)
            4 -> fifthFragment.setUnifiedAd(loadedAds[position], pageType)
            else -> elseFragment.setUnifiedAd(loadedAds[position], pageType)
        }
    }

    override fun getCount(): Int {
        return loadedAds.size
    }
}