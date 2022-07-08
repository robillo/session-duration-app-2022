package com.firstapp.robinpc.tongue_twisters_deluxe.ui.list_activity.difficulty_level

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.firstapp.robinpc.tongue_twisters_deluxe.R
import com.firstapp.robinpc.tongue_twisters_deluxe.TwisterApp.Companion.analyticsUtil
import com.firstapp.robinpc.tongue_twisters_deluxe.data.model.DifficultyLevel
import com.firstapp.robinpc.tongue_twisters_deluxe.data.model.Twister
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.BaseActivity
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.home.twisters.classic_twisters_home.TwistersHomeFragment
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.list_activity.adapter.TwisterListAdaper
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.Constants.Companion.TYPE_DIFFICULTY
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.android.synthetic.main.activity_difficulty_level.*
import kotlinx.android.synthetic.main.continue_playing_twister_widget.*

class DifficultyLevelActivity : BaseActivity(), TwisterListAdaper.TwisterClickListener {

    private lateinit var difficultyLevel: DifficultyLevel
    private lateinit var twisterList: List<Twister>
    private lateinit var viewModel: DifficultyLevelActivityViewModel

    companion object {
        private const val EXTRA_DIFFICULTY_LEVEL = "DIFFICULTY LEVEL"
        fun newIntent(context: Context, difficultyLevel: DifficultyLevel): Intent {
            val intent = Intent(context, DifficultyLevelActivity::class.java)
            intent.putExtra(EXTRA_DIFFICULTY_LEVEL, difficultyLevel)
            return intent
        }
    }

    override fun onNativeAdsLoaded(loadedAds: ArrayList<NativeAd>) {
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_difficulty_level
    }

    override fun setup() {
        setStatusBarColor(R.color.white, LIGHT_STATUS_BAR)
        analyticsUtil.logDifficultyLevelScreenViewEvent()
        getArguments()
        setComponent()
        loadData()
        setViews()
        initialiseAds()
    }

    private fun initialiseAds() {
        refreshAd()
    }

    private fun refreshAd() {
//        adView.loadAd(
//                AdRequest.Builder()
//                        .addTestDevice(getString(R.string.samsung_afifty_global_device_id))
//                        .build()
//        )
    }

    private fun setViews() {
        setHeader()
        setBottomOutline()
    }

    private fun setHeader() {
        levelHeaderTv.text = difficultyLevel.title
    }

    private fun setBottomOutline() {
        bottomOutlineIv.setBackgroundColor(
                getColorFromId(R.color.difficulty_level_header_bg)
        )
    }

    private fun getColorFromId(@Suppress("SameParameterValue") id: Int): Int {
        return ContextCompat.getColor(this, id)
    }

    private fun getArguments() {
        difficultyLevel = intent.getParcelableExtra(EXTRA_DIFFICULTY_LEVEL)!!
    }

    private fun loadData() {
        loadTwistersFromRoom()
    }

    private fun loadTwistersFromRoom() {
        viewModel.getTwistersInRange(difficultyLevel.startIndex, difficultyLevel.endIndex)
                .observe(this, Observer {
                    it?.let {
                        twisterList = it
                        setTwisterAdapter()
                        showSuggestedTwister()
                    }
                })
    }

    //todo - this is intermediate code
    private fun showSuggestedTwister() {
        //lifecycleScope.launch {
            //delay(2000)
            val randomTwister = twisterList.random()
            Log.e("mytag", "random twister is ${twisterList.size} ${randomTwister.name} $randomTwister")
            Glide.with(this@DifficultyLevelActivity)
                .load(randomTwister.iconUrl)
                .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
                .placeholder(R.drawable.icon_placeholder)
                .transition(
                    DrawableTransitionOptions.withCrossFade(
                        DrawableCrossFadeFactory.Builder()
                            .setCrossFadeEnabled(true).build()
                    )
                )
                .into(twisterIconIv)

            twisterTitleTv.text = randomTwister.name
            widgetMessageTv.text = getString(R.string.a_suggestion_from_list)
            twisterSuggestionWidgetHolder.visibility = View.VISIBLE
            twisterSuggestionWidgetHolder.setOnClickListener {
                startReadingActivity(
                    randomTwister,
                    TwistersHomeFragment.StartReadingActivitySource.DifficultyLevelSuggestedTwisterWidget.name,
                    TYPE_DIFFICULTY,
                    difficultyLevel.title
                )
            }
        //}
    }

    private fun setTwisterAdapter() {
        twisterRecycler.layoutManager = LinearLayoutManager(this)
        val adapter = TwisterListAdaper(twisterList, TYPE_DIFFICULTY)
        adapter.setTwisterClickListener(this)
        twisterRecycler.adapter = adapter
    }

    override fun onBackPressed() {
        super.onBackPressed()
        animateActivityTransition(R.anim.slide_in_left_activity, R.anim.slide_out_right_activity)
    }

    override fun onUnlockedTwisterClicked(twister: Twister) {
        startReadingActivity(
            twister,
            TwistersHomeFragment.StartReadingActivitySource.DifficultyLevelListItem.name,
            TYPE_DIFFICULTY,
            difficultyLevel.title
        )
    }

    override fun onLockedTwisterClicked(twister: Twister) {
        startReadingActivity(
            twister,
            TwistersHomeFragment.StartReadingActivitySource.DifficultyLevelListItem.name,
            TYPE_DIFFICULTY,
            difficultyLevel.title
        )
    }

    private fun setComponent() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(DifficultyLevelActivityViewModel::class.java)
    }
}
