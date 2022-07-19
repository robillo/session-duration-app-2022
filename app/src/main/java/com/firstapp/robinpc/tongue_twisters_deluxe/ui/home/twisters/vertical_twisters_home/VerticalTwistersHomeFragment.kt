package com.firstapp.robinpc.tongue_twisters_deluxe.ui.home.twisters.vertical_twisters_home

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.firstapp.robinpc.tongue_twisters_deluxe.R
import com.firstapp.robinpc.tongue_twisters_deluxe.data.model.DifficultyLevel
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.BaseFragment
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.common_adapter.AdapterListener
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.common_adapter.AppEnums
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.common_adapter.CommonListAdapter
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.home.HomeActivityViewModel
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.home.twisters.vertical_twisters_home.widgets.LevelHeaderChipWidget
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.reading.reading_fragment.ReadingFragment
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.scroll_reading.ScrollReadingFragment
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.splash.SplashActivity.Companion.EXTRA_LAUNCH_ELEMENT_INDEX
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.splash.SplashActivity.Companion.defaultIntValue
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.Constants
import com.google.android.gms.ads.nativead.NativeAd
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_vertical_twisters_home.*
import timber.log.Timber
import kotlin.collections.ArrayList

class VerticalTwistersHomeFragment : BaseFragment(), AdapterListener {

    private lateinit var viewModel: HomeActivityViewModel

    override fun getLayoutResId() = R.layout.fragment_vertical_twisters_home

    override fun inject() {
        AndroidSupportInjection.inject(this)
    }

    override fun setup() {
        Timber.d("mytag setting up vertical twisters")
        viewModel = ViewModelProvider(
            requireActivity(), viewModelFactory
        )[HomeActivityViewModel::class.java]

        loadDifficultyList()
    }

    private fun loadDifficultyList() {
        viewModel.getAllDifficultyLevels().observe(this) {
            it?.let {
                //todo - fix this
//                if(it.isNotEmpty()) {
//                    fetchSpecificTwister(it[0].startIndex, it[0].title)
//                }
                setDifficultyAdapter(it)
                navigateForDynamicLinks()
            }
        }
    }

    private fun fetchSpecificLevelTwisters(levelId: Int, startIndex: Int, endIndex: Int) {
        val fragment = ScrollReadingFragment.newInstance(levelId, startIndex, endIndex)
        replaceFragment(
            R.id.containerView, fragment, childFragmentManager
        )
    }

//    private fun fetchSpecificTwister(twisterId: Int, levelTitle: String) {
//        viewModel.getTwisterForIndex(twisterId).observe(viewLifecycleOwner) {
//            it?.let {
//                val fragment = ReadingFragment.newInstance(
//                    it.id, Constants.TYPE_DIFFICULTY, levelTitle, R.color.white
//                )
//                replaceFragment(
//                    R.id.containerView, fragment, childFragmentManager
//                )
//            }
//        }
//    }

    private lateinit var levelChipsAdapter: CommonListAdapter

    private fun setDifficultyAdapter(list: List<DifficultyLevel>) {
        val fetchTwisterId = arguments?.getInt(EXTRA_LAUNCH_ELEMENT_INDEX, defaultIntValue) ?: defaultIntValue
        var levelIndex = 0
        list.forEachIndexed { index, level ->
            if(level.startIndex <= fetchTwisterId && fetchTwisterId <= level.endIndex) {
                levelIndex = index
                level.isSelected = true
                fetchSpecificLevelTwisters(
                    index, level.startIndex, level.endIndex
                )
            }
        }
        levelChipsAdapter = CommonListAdapter(
            listener = this,
            AppEnums.LoaderType.NONE,
            LevelHeaderChipWidget { _, position ->
                list.forEachIndexed { index, level ->
                    list[index].isSelected = index == position
                    if(index == position)
                        fetchSpecificLevelTwisters(
                            index, level.startIndex, level.endIndex
                        )
                    levelChipsAdapter.submitList(list)
                }
            }
        )
        levelChipsAdapter.submitList(list)
        levelsRv.adapter = levelChipsAdapter
        try {
            levelsRv.post {
                levelsRv.layoutManager?.scrollToPosition(levelIndex)
            }
        }
        catch (ignored: Exception) {}
    }

    private fun navigateForDynamicLinks() {
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(activity?.intent)
            .addOnSuccessListener {
                val deepLink = it?.link
                Log.d("mytag", "$deepLink")
                deepLink?.getQueryParameter(Constants.PARAMETER_TWISTER_NUMBER)?.let { twisterNumber ->
                    viewModel.getTwisterForIndex(
                        Integer.valueOf(twisterNumber)
                    ).observe(this) { twister ->
                        //todo - fix this
//                        startReadingActivity(
//                            twister,
//                            TwistersHomeFragment.StartReadingActivitySource.DeepLink.name,
//                            Constants.TYPE_DAY_TWISTER
//                        )
                    }
                }
                deepLink?.getQueryParameter(Constants.PARAMETER_LEVEL_NUMBER)?.let { levelNumber ->
//                    if(::difficultyList.isInitialized) {
//                        difficultyList.forEach { level ->
//                            if(level.title[level.title.length-1].toString() == levelNumber) {
//                                startDifficultyLevelActivity(level)
//                            }
//                        }
//                    }
                }
            }
    }

    override fun onNativeAdsLoaded(loadedAds: ArrayList<NativeAd>) {}

    companion object {
        fun newInstance(twisterIndex: Int) =
            VerticalTwistersHomeFragment().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_LAUNCH_ELEMENT_INDEX, twisterIndex)
                }
            }
    }
}