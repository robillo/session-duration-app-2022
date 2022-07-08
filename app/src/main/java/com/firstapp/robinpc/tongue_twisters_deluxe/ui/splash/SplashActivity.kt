package com.firstapp.robinpc.tongue_twisters_deluxe.ui.splash

import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.view.View
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.firstapp.robinpc.tongue_twisters_deluxe.R
import com.firstapp.robinpc.tongue_twisters_deluxe.TwisterApp.Companion.analyticsUtil
import com.firstapp.robinpc.tongue_twisters_deluxe.di.component.activity.DaggerSplashActivityComponent
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.BaseActivity
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.home.HomeActivity
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.Constants.Companion.DIFFICULTY_LEVEL_COUNT
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.Constants.Companion.EXTRA_IS_DATA_LOAD_COMPLETE
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.Constants.Companion.LENGTH_LEVEL_COUNT
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.Constants.Companion.TWISTER_COUNT
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.TwisterPreferences
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.background.AlarmSchedulerUtil
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.service.RecurringNotificationService.Companion.WAIT_DURATION_MILLIS
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.service.RecurringNotificationService.Companion.NOTIFICATION_ID
import com.google.android.gms.ads.nativead.NativeAd
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_splash.*
import timber.log.Timber
import javax.inject.Inject

class SplashActivity : BaseActivity() {

    @Inject
    lateinit var preferences: TwisterPreferences

    private var launchElementIndex = defaultIntValue
    private lateinit var currentProgress: String
    private lateinit var viewModel: SplashViewModel

    companion object {
        private const val STEP_VALUE = 200L
        private const val TIMER_VALUE = 800L
        const val defaultIntValue = 1
        const val EXTRA_LAUNCH_ELEMENT_INDEX = "launchElementIndex"
        fun newIntent(context: Context, elementIndex: Int): Intent {
            val intent = Intent(context, SplashActivity::class.java)
            intent.putExtra(EXTRA_LAUNCH_ELEMENT_INDEX, elementIndex)
            return intent
        }
    }

    override fun onNativeAdsLoaded(loadedAds: ArrayList<NativeAd>) {

    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_splash
    }

    override fun setup() {
        setStatusBarColor(R.color.white, LIGHT_STATUS_BAR)
        analyticsUtil.logSplashScreenViewEvent()
        setDefaultValues()
        setComponent()
        getParameters()
        setObservers()
        checkForDatabaseCompletion()
        startNotificationAlarm()
        dismissActiveNotifications()
        Timber.d("mytag setup splash screen #2 ${intent.extras} ${intent.data}")
    }

    private fun dismissActiveNotifications() {
        with(NotificationManagerCompat.from(this)) {
            cancel(NOTIFICATION_ID)
        }
    }

    private fun getParameters() {
        intent?.let {
            Timber.d("mytag setup splash screen #3 => setting index as ${it.getIntExtra(EXTRA_LAUNCH_ELEMENT_INDEX, defaultIntValue)}")
            launchElementIndex = it.getIntExtra(EXTRA_LAUNCH_ELEMENT_INDEX, defaultIntValue)
        }
    }

    private fun startNotificationAlarm() {
        AlarmSchedulerUtil.setAlarm(
            this, System.currentTimeMillis() + WAIT_DURATION_MILLIS
        )
    }

    private fun setDefaultValues() {
        currentProgress = SplashViewModel.Companion.Progress.APP_STARTED.name
    }

    private fun checkForDatabaseCompletion() {
        viewModel.getDatabaseElementsCount().observe(this) { totalCount ->
            if (totalCount == getExpectedElementsCount()) {
                if (preferences.getBoolean(EXTRA_IS_DATA_LOAD_COMPLETE))
                    startHomeActivity()
                else
                    startTimer()
            } else fetchJsonFromAssets()
        }
    }

    private fun setObservers() {
        viewModel.loadProgressLiveData.observe(this) { progress ->

            currentProgress = progress

            when (currentProgress) {
                SplashViewModel.Companion.Progress.JSON_LOADED.name -> {
                    viewModel.convertArrays()
                }
                SplashViewModel.Companion.Progress.ARRAYS_CONVERTED.name -> {
                    viewModel.insertListsIntoRoom()
                }
            }
        }
    }

    private fun getExpectedElementsCount(): Int {
        return TWISTER_COUNT + LENGTH_LEVEL_COUNT + DIFFICULTY_LEVEL_COUNT
    }

    private fun fetchJsonFromAssets() {
        if(currentProgress == SplashViewModel.Companion.Progress.APP_STARTED.name)
            viewModel.fetchJsonFromAssets()
    }

    private fun startTimer() {
        loadingLottie.visibility = View.VISIBLE
        loadingProgressTv.visibility = View.VISIBLE
        loadingProgressBar.visibility = View.VISIBLE

        val timer = object: CountDownTimer(TIMER_VALUE, STEP_VALUE) {
            override fun onFinish() {
                preferences.putBoolean(EXTRA_IS_DATA_LOAD_COMPLETE, true)
                startHomeActivity()
            }

            override fun onTick(millisUntilFinished: Long) {
                loadingProgressBar.progress = getCompletionPercentage(millisUntilFinished, TIMER_VALUE)
            }
        }
        timer.start()
    }

    @Suppress("SameParameterValue")
    private fun getCompletionPercentage(remainingTime: Long, timerValue: Long): Int {
        val timeElapsed = timerValue - remainingTime + STEP_VALUE
        return timeElapsed.toInt() * 100 / timerValue.toInt()
    }

    private fun startHomeActivity() {
        startActivity(HomeActivity.newIntent(this, launchElementIndex))
        //animateActivityTransition(R.anim.slide_in_right_activity, R.anim.slide_out_left_activity)
    }

    private fun setComponent() {
        AndroidInjection.inject(this)
//        DaggerSplashActivityComponent.builder()
//                .appComponent(getAppComponent())
//                .build().injectSplashActivity(this)

        viewModel = ViewModelProvider(this, viewModelFactory)[SplashViewModel::class.java]
    }
}
