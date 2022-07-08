package com.firstapp.robinpc.tongue_twisters_deluxe.ui.home

import android.content.Context
import android.content.Intent
import com.firstapp.robinpc.tongue_twisters_deluxe.R
import com.firstapp.robinpc.tongue_twisters_deluxe.TwisterApp.Companion.analyticsUtil
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.BaseActivity
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.home.twisters.vertical_twisters_home.VerticalTwistersHomeFragment
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.splash.SplashActivity
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.splash.SplashActivity.Companion.EXTRA_LAUNCH_ELEMENT_INDEX
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.splash.SplashActivity.Companion.defaultIntValue
import com.google.android.gms.ads.nativead.NativeAd
import kotlin.collections.ArrayList

class HomeActivity : BaseActivity() {

    companion object {
        fun newIntent(context: Context, launchElementIndex: Int): Intent {
            val intent = Intent(context, HomeActivity::class.java)
            intent.putExtra(EXTRA_LAUNCH_ELEMENT_INDEX, launchElementIndex)
            return intent
        }
    }

    override fun getLayoutResId() = R.layout.activity_home

    override fun setup() {
        analyticsUtil.logHomeScreenViewEvent()
        initNativeAdsLoader()

        val fragment = VerticalTwistersHomeFragment.newInstance(
            intent?.getIntExtra(
                EXTRA_LAUNCH_ELEMENT_INDEX, defaultIntValue
            ) ?: defaultIntValue
        )
        //val fragment = TwistersHomeFragment.newInstance()

        supportFragmentManager.beginTransaction().replace(
            R.id.containerView, fragment, fragment.tag
        ).commit()
    }

    override fun onBackPressed() {
        val goHome = Intent(Intent.ACTION_MAIN)
        goHome.addCategory(Intent.CATEGORY_HOME)
        goHome.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(goHome)
    }

    override fun onNativeAdsLoaded(loadedAds: ArrayList<NativeAd>) {
        //show ads
    }
}