package com.firstapp.robinpc.tongue_twisters_deluxe.ui.base

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.firstapp.robinpc.tongue_twisters_deluxe.BuildConfig
import com.firstapp.robinpc.tongue_twisters_deluxe.R
import com.firstapp.robinpc.tongue_twisters_deluxe.TwisterApp
import com.firstapp.robinpc.tongue_twisters_deluxe.data.model.Twister
import com.firstapp.robinpc.tongue_twisters_deluxe.di.module.others.UtilsModule
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.home.twisters.classic_twisters_home.TwistersHomeFragment
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.reading.reading_fragment.ReadingFragment
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.AnalyticsUtil
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.Constants
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.TwisterPreferences
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import javax.inject.Inject

abstract class BaseFragment: Fragment() {

    companion object {
        val statusBarColorBeforeLaunch = "statusBarColorBeforeLaunch"
        private const val adLoadCount = 10
        enum class CountryType {
            INDIA, USA
        }
    }

    @Inject
    lateinit var utilsModule: UtilsModule

    @Inject
    lateinit var preferences: TwisterPreferences

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var nativeAdsLoader: AdLoader
    private lateinit var loadedNativeAds: ArrayList<NativeAd>
    private var interstitialAd: InterstitialAd? = null

    private lateinit var parent: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        parent = inflater.inflate(getLayoutResId(), container, false)
        return parent
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        launchColor = arguments?.getInt(statusBarColorBeforeLaunch)
        setup()
    }

    protected fun animateActivityTransition(enterAnim: Int, exitAnim: Int) {
        context?.let {
            (it as Activity).overridePendingTransition(enterAnim, exitAnim)
        }
    }

    @Suppress("unused")
    protected fun getCountry(): CountryType {
        return when(utilsModule.recogniseCountry()) {
            "in" -> CountryType.INDIA
            "us" -> CountryType.USA
            else -> CountryType.INDIA
        }
    }

    abstract fun getLayoutResId(): Int
    abstract fun inject()
    abstract fun setup()

    override fun onAttach(context: Context) {
        inject()
        super.onAttach(context)
    }

    protected fun startReadingActivity(
        twister: Twister,
        source: String,
        type: Int,
        title: String? = null
    ) {
        val screenName =
            when(source) {
                TwistersHomeFragment.StartReadingActivitySource.DeepLink.name,
                TwistersHomeFragment.StartReadingActivitySource.DayTwister.name
                -> AnalyticsUtil.homeScreen
                TwistersHomeFragment.StartReadingActivitySource.DifficultyLevelListItem.name,
                TwistersHomeFragment.StartReadingActivitySource.DifficultyLevelSuggestedTwisterWidget.name
                -> AnalyticsUtil.difficultyLevelScreen
                else
                -> AnalyticsUtil.homeScreen
            }
        TwisterApp.analyticsUtil.logNavigationEvent(source, screenName, twister.name)
        val fragment = ReadingFragment.newInstance(
            twister.id, type, levelHeader = title ?: Constants.DEFAULT_LEVEL_HEADER, R.color.white
        )
        activity?.supportFragmentManager?.beginTransaction()?.add(
            android.R.id.content, fragment, fragment.tag
        )?.addToBackStack(fragment.tag)?.commit()
    }

    protected fun replaceFragment(
        containerView: Int, fragment: BaseFragment, manager: FragmentManager?
    ) {
        manager?.beginTransaction()?.replace(
            containerView, fragment, fragment.tag
        )?.commit()
    }

    protected fun addFragment(fragment: BaseFragment) {
        activity?.supportFragmentManager?.beginTransaction()?.add(
            android.R.id.content, fragment, fragment.tag
        )?.addToBackStack(fragment.tag)?.commit()
    }

    protected fun initNativeAdsLoader() {
        loadedNativeAds = ArrayList()

        nativeAdsLoader = AdLoader.Builder(
            requireContext(),
            if(BuildConfig.DEBUG)
                getString(R.string.ad_test_native_id)
            else
                getString(R.string.native_ad_unit_id)
        )
            .forNativeAd {

                loadedNativeAds.add(it)

                if(!nativeAdsLoader.isLoading)
                    onNativeAdsLoaded(loadedNativeAds)
            }.build()

        loadNativeAdsForCount()
    }

    protected fun initInterstitialAdsLoader(interstitialAdId: String) {
        InterstitialAd.load(
            requireContext(),
            interstitialAdId,
            AdRequest.Builder().build(),
            object: InterstitialAdLoadCallback() {
                override fun onAdLoaded(loadedAd: InterstitialAd) {
                    super.onAdLoaded(loadedAd)
                    interstitialAd = loadedAd
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    interstitialAd = null
                }
            }
        )
        //interstitialAd = InterstitialAd(this)
        //interstitialAd.adUnitId = interstitialAdId
        //interstitialAd.loadAd(AdRequest.Builder().build())
    }

    private fun loadNativeAdsForCount() {
        nativeAdsLoader.loadAds(AdRequest.Builder().build(), adLoadCount)
        //.addTestDevice(getString(R.string.samsung_afifty_global_device_id))
    }

    protected fun showInterstitialAd(adId: String) {
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object: FullScreenContentCallback() {
                override fun onAdClicked() {
                    super.onAdClicked()
                    initInterstitialAdsLoader(adId)
                }
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    initInterstitialAdsLoader(adId)
                }
            }
            ad.show(requireActivity())
        } ?: run {
            initInterstitialAdsLoader(adId)
        }
    }

    abstract fun onNativeAdsLoaded(loadedAds: ArrayList<NativeAd>)

    @Suppress("SameParameterValue")
    fun setStatusBarColor(color: Int, shouldShowLightStatusBar: Boolean) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            try {
                activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), color)
            }
            catch (ignored: Exception) {}
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowLightStatusBar) {
            try {
                activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            catch (ignored: Exception) {}
        }
    }

    override fun onDestroy() {
        resetStatusBarColor()
        super.onDestroy()
    }

    var launchColor: Int? = null

    private fun resetStatusBarColor() {
        launchColor?.let { setStatusBarColor(it, shouldShowLightStatusBar = false) }
    }
}