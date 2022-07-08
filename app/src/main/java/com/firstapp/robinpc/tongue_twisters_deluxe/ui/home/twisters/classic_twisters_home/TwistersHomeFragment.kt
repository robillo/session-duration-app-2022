package com.firstapp.robinpc.tongue_twisters_deluxe.ui.home.twisters.classic_twisters_home

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firstapp.robinpc.tongue_twisters_deluxe.BuildConfig
import com.firstapp.robinpc.tongue_twisters_deluxe.R
import com.firstapp.robinpc.tongue_twisters_deluxe.TwisterApp
import com.firstapp.robinpc.tongue_twisters_deluxe.data.model.DifficultyLevel
import com.firstapp.robinpc.tongue_twisters_deluxe.data.model.LengthLevel
import com.firstapp.robinpc.tongue_twisters_deluxe.data.model.Twister
import com.firstapp.robinpc.tongue_twisters_deluxe.di.module.activity.HomeActivityModule
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.BaseActivity
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.BaseFragment
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.home.HomeActivityViewModel
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.home.twisters.classic_twisters_home.adapters.DifficultyAdapter
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.list_activity.difficulty_level.DifficultyLevelActivity
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.list_activity.length_level.LengthLevelActivity
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.reading.reading_fragment.ReadingFragment
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.splash.SplashActivity
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.Constants
import com.google.android.gms.ads.nativead.NativeAd
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.installations.FirebaseInstallations
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_twisters_home.*
import java.util.*
import kotlin.collections.ArrayList

class TwistersHomeFragment : BaseFragment(), DifficultyAdapter.DifficultyLevelClickListener {

    private var launchElementIndex = -1

    private lateinit var launchTwister: Twister
    private lateinit var dayTwister: Twister
    private lateinit var difficultyAdapter: DifficultyAdapter
    private lateinit var lengthList: List<LengthLevel>
    private lateinit var viewModel: HomeActivityViewModel
    private lateinit var difficultyList: List<DifficultyLevel>

    override fun getLayoutResId() = R.layout.fragment_twisters_home

    override fun setup() {
        setStatusBar()
        TwisterApp.analyticsUtil.logHomeScreenViewEvent()
        setComponent()
        initLayout()
        setListeners()
        logFirebaseInstanceIdToken()
    }

    override fun onNativeAdsLoaded(loadedAds: ArrayList<NativeAd>) {}

    override fun inject() {
        AndroidSupportInjection.inject(this)
    }

    private fun setComponent() {
        viewModel = ViewModelProvider(
            requireActivity(), viewModelFactory
        )[HomeActivityViewModel::class.java]
    }

    private fun logFirebaseInstanceIdToken() {
        if(BuildConfig.DEBUG) {
            val forceRefresh = false
            FirebaseInstallations.getInstance()
                .getToken(forceRefresh)
                .addOnCompleteListener { task ->
                    val token = task.result?.token
                    Log.e("myTag", "$token")
                }
        }
    }

    private fun initLayout() {
        loadData()
    }

    private fun setListeners() {
        setClickListeners()
    }

    private fun setClickListeners() {
        dayTwisterCardLayout.setOnClickListener {
            openDayTwisterButton.callOnClick()
        }
        openDayTwisterButton.setOnClickListener {
            if(::dayTwister.isInitialized)
                startReadingActivity(
                    dayTwister,
                    StartReadingActivitySource.DayTwister.name,
                    Constants.TYPE_DAY_TWISTER
                )
        }
        continueLengthLevelHolder.setOnClickListener {
            when(preferences.getString(Constants.EXTRA_LAST_OPENED_LENGTH_LEVEL)) {
                getString(R.string.small_length_caps) -> smallTwistersHolderView.performClick()
                getString(R.string.medium_length_caps) -> mediumTwistersHolderView.performClick()
                getString(R.string.long_length_caps) -> longTwistersHolderView.performClick()
            }
        }
        continueDifficultyLevelHolder.setOnClickListener {
            val lastOpenedDifficultyLevel = preferences.getString(Constants.EXTRA_LAST_OPENED_DIFFICULTY_LEVEL)
            difficultyAdapter.clickLastOpenedDifficultyLevel(lastOpenedDifficultyLevel)
        }
        smallTwistersHolderView.setOnClickListener {
            startLengthLevelActivity(lengthList[SMALL_LENGTH_LEVEL_INDEX])
        }
        mediumTwistersHolderView.setOnClickListener {
            startLengthLevelActivity(lengthList[MEDIUM_LENGTH_LEVEL_INDEX])
        }
        longTwistersHolderView.setOnClickListener {
            startLengthLevelActivity(lengthList[LONG_LENGTH_LEVEL_INDEX])
        }
    }

    private fun loadData() {
        activity?.intent?.let {
            launchElementIndex = it.getIntExtra(SplashActivity.EXTRA_LAUNCH_ELEMENT_INDEX, -1)
        }
        loadLengthList()
        loadDifficultyList()
        loadTwisterOfTheDay()
    }

    private fun loadLengthList() {
        viewModel.getAllLengthLevels().observe(this) {
            it?.let {
                this.lengthList = it
            }
        }
    }

    private fun loadDifficultyList() {
        viewModel.getAllDifficultyLevels().observe(this) {
            it?.let {
                this.difficultyList = it
                setDifficultyAdapter()
                navigateForDynamicLinks()
            }
        }
    }

    private fun setDifficultyAdapter() {
        difficultyRecycler.layoutManager = GridLayoutManager(
            context, HomeActivityModule.GRID_SPAN_COUNT,
            RecyclerView.VERTICAL, HomeActivityModule.SHOULD_REVERSE_GRID
        )
        difficultyAdapter = DifficultyAdapter(difficultyList)
        difficultyAdapter.setDifficultyClickListener(this)
        difficultyRecycler.adapter = difficultyAdapter
    }

    @Suppress("UNNECESSARY_SAFE_CALL")
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
                        startReadingActivity(
                            twister,
                            StartReadingActivitySource.DeepLink.name,
                            Constants.TYPE_DAY_TWISTER
                        )
                    }
                }
                deepLink?.getQueryParameter(Constants.PARAMETER_LEVEL_NUMBER)?.let { levelNumber ->
                    if(::difficultyList.isInitialized) {
                        difficultyList.forEach { level ->
                            if(level.title[level.title.length-1].toString() == levelNumber) {
                                startDifficultyLevelActivity(level)
                            }
                        }
                    }
                }
            }
    }

    private fun loadTwisterOfTheDay() {
        val dayTwisterId = Random().nextInt(Constants.TWISTER_COUNT)
        viewModel.getTwisterForIndex(dayTwisterId).observe(this) {
            it?.let {
                dayTwister = it
                renderForDayTwister()
            }
        }
        if(launchElementIndex > -1) {
            viewModel.getTwisterForIndex(launchElementIndex).observe(this) {
                it?.let {
                    launchTwister = it
                    launchNotificationTwister()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        showContinueLengthLevelIfRequired()
        showContinueDifficultyLevelIfRequired()
    }

    private fun showContinueLengthLevelIfRequired() {
        val lastOpenedLengthLevel = preferences.getString(Constants.EXTRA_LAST_OPENED_LENGTH_LEVEL)
        if(lastOpenedLengthLevel.isNotBlank() && lastOpenedLengthLevel.isNotEmpty()) {
            continueLengthLevelHolder.visibility = View.VISIBLE
            continueLengthLevelDynamicTv.text = String.format(
                getString(R.string.s_length_twisters),
                lastOpenedLengthLevel.toLowerCase(Locale.ENGLISH)
            )
        }
        else {
            continueLengthLevelHolder.visibility = View.GONE
        }
    }

    private fun showContinueDifficultyLevelIfRequired() {
        val lastOpenedDifficultyLevel = preferences.getString(Constants.EXTRA_LAST_OPENED_DIFFICULTY_LEVEL)
        if(lastOpenedDifficultyLevel.isNotBlank() && lastOpenedDifficultyLevel.isNotEmpty()) {
            continueDifficultyLevelHolder.visibility = View.VISIBLE
            continueDifficultyLevelDynamicTv.text = String.format(
                getString(R.string.difficulty_s_twisters),
                lastOpenedDifficultyLevel.toLowerCase(Locale.ENGLISH)
            )
        }
        else {
            continueDifficultyLevelHolder.visibility = View.GONE
        }
    }

    private fun launchNotificationTwister() {
        if(::launchTwister.isInitialized) {
            val fragment = ReadingFragment.newInstance(
                launchTwister.id, Constants.TYPE_DAY_TWISTER, statusBarColor = R.color.white
            )
            activity?.supportFragmentManager?.beginTransaction()?.add(
                android.R.id.content, fragment, fragment.tag
            )?.addToBackStack(fragment.tag)?.commit()
        }
    }

    private fun renderForDayTwister() {
        dayTwisterNameTv.text = dayTwister.name
    }


    private fun setStatusBar() {
        (activity as BaseActivity).setStatusBarColor(R.color.white, BaseActivity.LIGHT_STATUS_BAR)
    }

    override fun difficultyClicked(difficultyLevel: DifficultyLevel) {
        startDifficultyLevelActivity(difficultyLevel)
    }

    private fun startLengthLevelActivity(lengthLevel: LengthLevel) {
        preferences.putStringSync(Constants.EXTRA_LAST_OPENED_LENGTH_LEVEL, lengthLevel.title)
        startActivity(LengthLevelActivity.newIntent(requireContext(), lengthLevel))
        animateActivityTransition(R.anim.slide_in_right_activity, R.anim.slide_out_left_activity)
    }

    private fun startDifficultyLevelActivity(difficultyLevel: DifficultyLevel) {
        //todo - log event - difficulty level open (level, title)
        preferences.putStringSync(Constants.EXTRA_LAST_OPENED_DIFFICULTY_LEVEL, difficultyLevel.title)
        startActivity(DifficultyLevelActivity.newIntent(requireContext(), difficultyLevel))
        animateActivityTransition(R.anim.slide_in_right_activity, R.anim.slide_out_left_activity)
    }

    enum class StartReadingActivitySource {
        DeepLink,
        DayTwister,
        DifficultyLevelListItem,
        DifficultyLevelSuggestedTwisterWidget
    }

    companion object {
        private const val SMALL_LENGTH_LEVEL_INDEX = 0
        private const val MEDIUM_LENGTH_LEVEL_INDEX = 1
        private const val LONG_LENGTH_LEVEL_INDEX = 2
        fun newInstance() = TwistersHomeFragment()
    }
}