package com.firstapp.robinpc.tongue_twisters_deluxe.di.module.fragment

import com.firstapp.robinpc.tongue_twisters_deluxe.ui.home.twisters.classic_twisters_home.TwistersHomeFragment
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.home.twisters.vertical_twisters_home.VerticalTwistersHomeFragment
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.reading.reading_fragment.ReadingFragment
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.reading.reading_fragment.ads_pager_fragment.ReadingAdsPageFragment
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.scroll_reading.ScrollReadingFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBindingModule {

    @ContributesAndroidInjector
    abstract fun classicTwistersHomeFragment(): TwistersHomeFragment

    @ContributesAndroidInjector
    abstract fun readingAdsPage(): ReadingAdsPageFragment

    @ContributesAndroidInjector
    abstract fun readingFragment(): ReadingFragment

    @ContributesAndroidInjector
    abstract fun verticalTwistersFragment(): VerticalTwistersHomeFragment

    @ContributesAndroidInjector
    abstract fun scrollReadingFragment(): ScrollReadingFragment
}