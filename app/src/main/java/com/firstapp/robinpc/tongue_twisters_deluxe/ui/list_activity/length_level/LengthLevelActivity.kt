package com.firstapp.robinpc.tongue_twisters_deluxe.ui.list_activity.length_level

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.firstapp.robinpc.tongue_twisters_deluxe.R
import com.firstapp.robinpc.tongue_twisters_deluxe.TwisterApp
import com.firstapp.robinpc.tongue_twisters_deluxe.data.model.LengthLevel
import com.firstapp.robinpc.tongue_twisters_deluxe.data.model.Twister
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.BaseActivity
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.list_activity.adapter.TwisterListAdaper
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.reading.reading_fragment.ReadingFragment
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.Constants.Companion.TYPE_LENGTH
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.android.synthetic.main.activity_length_level.*

class LengthLevelActivity : BaseActivity(), TwisterListAdaper.TwisterClickListener {

    private lateinit var lengthLevel: LengthLevel
    private lateinit var twisterList: List<Twister>
    private lateinit var viewModel: LengthLevelActivityViewModel

    companion object {
        private const val EXTRA_LENGTH_LEVEL = "LENGTH LEVEL"
        fun newIntent(context: Context, lengthLevel: LengthLevel): Intent {
            val intent = Intent(context, LengthLevelActivity::class.java)
            intent.putExtra(EXTRA_LENGTH_LEVEL, lengthLevel)
            return intent
        }
    }

    override fun onNativeAdsLoaded(loadedAds: ArrayList<NativeAd>) {
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_length_level
    }

    override fun setup() {
        setStatusBarColor(R.color.white, LIGHT_STATUS_BAR)
        TwisterApp.analyticsUtil.logLengthLevelScreenViewEvent()
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
        levelHeaderTv.text = lengthLevel.title
    }

    private fun setBottomOutline() {
        bottomOutlineIv.setBackgroundColor(
                getColorFromId(R.color.length_level_header_bg)
        )
    }

    private fun getColorFromId(@Suppress("SameParameterValue") id: Int): Int {
        return ContextCompat.getColor(this, id)
    }

    private fun getArguments() {
        lengthLevel = intent.getParcelableExtra(EXTRA_LENGTH_LEVEL)!!
    }

    private fun loadData() {
        loadTwistersFromRoom()
    }

    private fun loadTwistersFromRoom() {
        viewModel.getTwistersInRange(lengthLevel.startIndex, lengthLevel.endIndex)
                .observe(this) {
                    it?.let {
                        twisterList = it
                        setTwisterAdapter()
                    }
                }
    }

    private fun setTwisterAdapter() {
        twisterRecycler.layoutManager = LinearLayoutManager(this)
        val adapter = TwisterListAdaper(twisterList, TYPE_LENGTH)
        adapter.setTwisterClickListener(this)
        twisterRecycler.adapter = adapter
    }

    override fun onBackPressed() {
        super.onBackPressed()
        animateActivityTransition(R.anim.slide_in_left_activity, R.anim.slide_out_right_activity)
    }

    override fun onUnlockedTwisterClicked(twister: Twister) {
        startReadingActivity(twister)
    }

    override fun onLockedTwisterClicked(twister: Twister) {
        startReadingActivity(twister)
    }

    private fun startReadingActivity(twister: Twister) {
        val fragment = ReadingFragment.newInstance(
            twister.id, TYPE_LENGTH, lengthLevel.title, R.color.white
        )
        supportFragmentManager.beginTransaction().add(
            android.R.id.content, fragment, fragment.tag
        ).addToBackStack(fragment.tag).commit()
    }

    private fun setComponent() {
//        DaggerLengthLevelActivityComponent.builder()
//                .appComponent(getAppComponent())
//                .build().injectLengthLevelActivity(this)

        viewModel = ViewModelProvider(this, viewModelFactory)
                .get(LengthLevelActivityViewModel::class.java)
    }
}