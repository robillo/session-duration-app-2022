package com.firstapp.robinpc.tongue_twisters_deluxe.ui.base

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.firstapp.robinpc.tongue_twisters_deluxe.BuildConfig
import com.firstapp.robinpc.tongue_twisters_deluxe.R
import com.firstapp.robinpc.tongue_twisters_deluxe.TwisterApp
import com.firstapp.robinpc.tongue_twisters_deluxe.TwisterApp.Companion.analyticsUtil
import com.firstapp.robinpc.tongue_twisters_deluxe.data.model.Twister
import com.firstapp.robinpc.tongue_twisters_deluxe.di.component.AppComponent
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.home.twisters.classic_twisters_home.TwistersHomeFragment
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.reading.reading_fragment.ReadingFragment
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.AnalyticsUtil.Companion.difficultyLevelScreen
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.AnalyticsUtil.Companion.homeScreen
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.Constants
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import dagger.android.AndroidInjection
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var nativeAdsLoader: AdLoader
    private lateinit var loadedNativeAds: ArrayList<NativeAd>
    private var interstitialAd: InterstitialAd? = null

    companion object {
        private const val adLoadCount = 5
        const val LIGHT_STATUS_BAR = true
        const val DARK_STATUS_BAR = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResId())

        AndroidInjection.inject(this)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        hiddenSetup()
        setup()
    }

    private fun hiddenSetup() {}

    protected fun initNativeAdsLoader() {
        loadedNativeAds = ArrayList()

        nativeAdsLoader = AdLoader.Builder(
            this,
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
            this,
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
            ad.show(this)
        } ?: run {
            initInterstitialAdsLoader(adId)
        }
//        interstitialAd.adListener = object : AdListener() {
//            override fun onAdClosed() {
//                super.onAdClosed()
//
//                initInterstitialAdsLoader(adId)
//            }
//
//            override fun onAdClicked() {
//                super.onAdClicked()
//
//                initInterstitialAdsLoader(adId)
//            }
//
//            override fun onAdLeftApplication() {
//                super.onAdLeftApplication()
//
//                initInterstitialAdsLoader(adId)
//            }
//        }
        //interstitialAd.show()
    }

    abstract fun onNativeAdsLoaded(loadedAds: ArrayList<NativeAd>)

    abstract fun getLayoutResId(): Int

    abstract fun setup()

    @Suppress("SameParameterValue")
    fun setStatusBarColor(color: Int, shouldShowLightStatusBar: Boolean) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, color)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowLightStatusBar)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    protected fun animateActivityTransition(enterAnim: Int, exitAnim: Int) {
        overridePendingTransition(enterAnim, exitAnim)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    @Suppress("unused")
    protected fun getDrawableForId(drawableId: Int): Drawable {
        ContextCompat.getDrawable(this, drawableId)?.let {
            return it
        } ?: run {
            return ColorDrawable(getColorForId(R.color.black))
        }
    }

    @Suppress("SameParameterValue")
    protected fun getColorForId(colorId: Int): Int {
        return ContextCompat.getColor(this, colorId)
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
                -> homeScreen
                TwistersHomeFragment.StartReadingActivitySource.DifficultyLevelListItem.name,
                TwistersHomeFragment.StartReadingActivitySource.DifficultyLevelSuggestedTwisterWidget.name
                -> difficultyLevelScreen
                else
                -> homeScreen
            }
        analyticsUtil.logNavigationEvent(source, screenName, twister.name)
        val fragment = ReadingFragment.newInstance(
            twister.id, type, levelHeader = title ?: Constants.DEFAULT_LEVEL_HEADER, R.color.white
        )
        supportFragmentManager.beginTransaction().add(
            android.R.id.content, fragment, fragment.tag
        ).addToBackStack(fragment.tag).commit()
    }
}