package com.firstapp.robinpc.tongue_twisters_deluxe.ui.scroll_reading

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.firstapp.robinpc.tongue_twisters_deluxe.R
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.BaseFragment
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.reading.ReadingActivityViewModel
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.reading.reading_fragment.ReadingFragment
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.reading.reading_fragment.ReadingFragment.Companion.EXTRA_LEVEL_INDEX
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.reading.reading_fragment.ReadingFragment.Companion.levelEndIndex
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.reading.reading_fragment.ReadingFragment.Companion.levelStartIndex
import com.google.android.gms.ads.nativead.NativeAd
import dagger.android.support.AndroidSupportInjection

class ScrollReadingFragment : BaseFragment() {

    private var level = 1
    private var levelStart = 0
    private var levelEnd = 0

    private var pageList: MutableList<PageData> = ArrayList()

    private lateinit var viewModel: ReadingActivityViewModel
    private lateinit var scrollPagerAdapter: ScrollReadingAdapter

    override fun getLayoutResId() = R.layout.fragment_scroll_reading

    override fun inject() {
        AndroidSupportInjection.inject(this)
    }

    override fun setup() {
        extractLevel()
        initNativeAdsLoader()
        setViewModel()
        fetchTwistersForLevel()
        //todo - fetch tongue twisters
    }

    private fun fetchTwistersForLevel() {
        viewModel.getTwistersInRange(levelStart, levelEnd)
            .observe(this) {
                it?.let {
                    it.forEach {
                        pageList.add(PageData(it))
                    }
                    //scrollPagerAdapter = ScrollReadingAdapter()
                    //todo - set scroll pager adapter for page data list
                }
            }
    }

    override fun onNativeAdsLoaded(loadedAds: ArrayList<NativeAd>) {
        //scrollPagerAdapter = ScrollReadingAdapter()
        //todo - append ads to fetched twisters list
    }

    private fun setViewModel() {
        viewModel = ViewModelProvider(
            viewModelStore, viewModelFactory
        )[ReadingActivityViewModel::class.java]
    }

    private fun extractLevel() {
        level = arguments?.getInt(EXTRA_LEVEL_INDEX, defaultLevel) ?: defaultLevel
        levelStart = arguments?.getInt(levelStartIndex, defaultLevel) ?: defaultLevel
        levelEnd = arguments?.getInt(levelEndIndex, defaultLevel) ?: defaultLevel
    }

    companion object {
        const val defaultLevel = 0
        fun newInstance(levelIndex: Int, levelStartIndex: Int, levelEndIndex: Int) =
            ScrollReadingFragment().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_LEVEL_INDEX, levelIndex)
                    putInt(ReadingFragment.levelStartIndex, levelStartIndex)
                    putInt(ReadingFragment.levelEndIndex, levelEndIndex)
                }
        }
    }
}