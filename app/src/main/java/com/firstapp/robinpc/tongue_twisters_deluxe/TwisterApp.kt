package com.firstapp.robinpc.tongue_twisters_deluxe

import android.app.Activity
import android.app.Service
import androidx.lifecycle.LifecycleObserver
import com.amplitude.api.Amplitude
import com.firstapp.robinpc.tongue_twisters_deluxe.di.component.AppComponent
import com.firstapp.robinpc.tongue_twisters_deluxe.di.component.DaggerAppComponent
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.AnalyticsUtil
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector

import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import timber.log.Timber
import javax.inject.Inject

class TwisterApp : DaggerApplication(), HasAndroidInjector, LifecycleObserver {

    @Inject
    lateinit var component: AppComponent

    override fun onCreate() {
        instance = this

        super.onCreate()

        initCalligraphy()
        initAmplitude()
        initAnalytics()
        initialiseAds()
        initializeTimber()
    }

    private fun initializeTimber() {
        Timber.plant(Timber.DebugTree())
    }

    private fun initAmplitude() {
        Amplitude.getInstance().initialize(
            this, getString(R.string.amplitude_api_key)
        ).enableForegroundTracking(this)
    }

    private fun initCalligraphy() {
        ViewPump.init(ViewPump.builder()
            .addInterceptor(CalligraphyInterceptor(
                CalligraphyConfig.Builder()
                    .setDefaultFontPath("fonts/WS-Regular.ttf")
                    .setFontAttrId(R.attr.fontPath)
                    .build()))
            .build())
    }

    private fun initAnalytics() {
        analyticsUtil = AnalyticsUtil(Firebase.analytics)
    }

    private fun initialiseAds() {
        MobileAds.initialize(this) {}
    }

    companion object {
        val isTestModeEnabled = BuildConfig.DEBUG
        val areAdsEnabled = true

        @JvmStatic
        lateinit var instance: TwisterApp

        lateinit var analyticsUtil: AnalyticsUtil

        fun get(activity: Activity): TwisterApp {
            return activity.application as TwisterApp
        }
        fun get(service: Service): TwisterApp {
            return service.application as TwisterApp
        }
    }

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    override fun applicationInjector(): AndroidInjector<TwisterApp> {
        val injector: AppComponent = DaggerAppComponent.builder()
            .application(this).context(this).build()
        injector.inject(this)
        return injector
    }
}
