package com.firstapp.robinpc.tongue_twisters_deluxe.ui.reading

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.firstapp.robinpc.tongue_twisters_deluxe.R
import com.firstapp.robinpc.tongue_twisters_deluxe.data.model.Twister
import com.firstapp.robinpc.tongue_twisters_deluxe.di.component.activity.DaggerReadingActivityComponent
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.BaseActivity
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.reading.ads_pager_adapter.AdsPagerAdapter
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.Constants.Companion.MAX_TWISTER_INDEX
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.Constants.Companion.MIN_TWISTER_INDEX
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.Constants.Companion.TYPE_DAY_TWISTER
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.Constants.Companion.TYPE_DIFFICULTY
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.Constants.Companion.TYPE_LENGTH
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.Constants.Companion.UNIT_VALUE_INT
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.TwisterPreferences
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.view_pager_transformers.ZoomOutSlideTransformer
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.android.synthetic.main.activity_reading.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class ReadingActivity : BaseActivity() {

    @Inject
    lateinit var preferences: TwisterPreferences

    private lateinit var adsPagerAdapter: AdsPagerAdapter
    private lateinit var twister: Twister
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var viewModel: ReadingActivityViewModel
    private lateinit var loopHandler: Handler
    private lateinit var runnable: Runnable

    private val loopInterval = 5000L

    private var controlsColor: Int = -1
    private var isTextToSpeechLoaded = false
    private var isTwisterPlaying: Boolean = false
    private var launchedFrom: Int = TYPE_DAY_TWISTER
    private var shouldLoopViewPager = false
    private var utteranceMap = HashMap<String, String>()

    companion object {

        private const val EMPTY_STRING = ""
        private const val EXTRA_TWISTER = "TWISTER"
        private const val UTTERANCE_ID = "TWISTER_UTTERANCE"
        const val EXTRA_LAUNCHED_FROM = "LAUNCHED_FROM"

        fun newIntent(context: Context, twister: Twister, launchedFrom: Int): Intent {
            val intent = Intent(context, ReadingActivity::class.java)
            intent.putExtra(EXTRA_TWISTER, twister)
            intent.putExtra(EXTRA_LAUNCHED_FROM, launchedFrom)
            return intent
        }
    }

    private fun loopViewPager() {
        loopHandler = Handler()
        runnable = Runnable {

            if(adsViewPager.currentItem == adsPagerAdapter.count - 1)
                adsViewPager.currentItem = 0
            else
                adsViewPager.currentItem = adsViewPager.currentItem + 1

            loopHandler.postDelayed(runnable, loopInterval)
        }
        loopHandler.postDelayed(runnable, loopInterval)
    }

    private fun stopViewPagerLoop() {
        loopHandler.removeCallbacks(runnable)
    }

    override fun onNativeAdsLoaded(loadedAds: ArrayList<NativeAd>) {}

    override fun getLayoutResId(): Int {
        return R.layout.activity_reading
    }

    override fun setup() {
        setStatusBarColor(R.color.white, LIGHT_STATUS_BAR)
        getVariables()
        initVariables()
        setComponent()
        setViews()
        setClickListeners()
        initialiseAds()
    }

    private fun initialiseAds() {
        //initAdLoader()
    }

    private fun initVariables() {
        utteranceMap[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = UTTERANCE_ID
    }

    private fun getVariables() {
//        (intent.getParcelableExtra(EXTRA_TWISTER) as Twister).let {
//            twister = it
//        }
        launchedFrom = intent.getIntExtra(EXTRA_LAUNCHED_FROM, TYPE_DAY_TWISTER)
    }

    private fun setViews() {
        renderForTwisterSource()
        setTwister()
    }

    private fun renderForTwisterSource() {
        when(launchedFrom) {
            TYPE_LENGTH -> render(R.color.length_reading_activity_twister_color)
            TYPE_DIFFICULTY -> render(R.color.difficulty_reading_activity_twister_color)
            TYPE_DAY_TWISTER -> render(R.color.day_twister_reading_activity_twister_color)
        }
    }

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->

            if(isTextToSpeechSuccess(status)) {
                isTextToSpeechLoaded = true

                if(!isTwisterPlaying)
                    playPauseHolder.performClick()

                textToSpeech.language = Locale.ENGLISH
                textToSpeech.setOnUtteranceProgressListener(speechProgressListener)
            }
        })
    }

    private fun isTextToSpeechSuccess(status: Int): Boolean {
        return status != TextToSpeech.ERROR
    }

    private fun render(colorId: Int) {
        controlsColor = getColorFromId(colorId)
        twisterHeaderTv.setTextColor(controlsColor)
        setControlsColors()
        setPlayDrawable()
    }

    private fun setControlsColors() {
        nextButtonIv.setColorFilter(controlsColor)
        playPauseButtonIv.setColorFilter(controlsColor)
        previousButtonIv.setColorFilter(controlsColor)
    }

    private fun setClickListeners() {
        previousHolder.setOnClickListener {
            goPrevious((twister.id - UNIT_VALUE_INT))
        }
        playPauseHolder.setOnClickListener {
            if(isTextToSpeechLoaded)
                playPauseTwister()
        }
        nextHolder.setOnClickListener {
            goNext((twister.id + UNIT_VALUE_INT))
        }
    }

    private fun playPauseTwister() {
        if(isTwisterPlaying) {
            setPlayDrawable()
            stopTextToSpeech()
        }
        else {
            setPauseDrawable()
            playTextToSpeech()
        }
    }

    private fun markTwisterAsStopped() {
        setPlayDrawable()
        isTwisterPlaying = false
    }

    private fun markTwisterAsPlaying() {
        setPauseDrawable()
        isTwisterPlaying = true
    }

    private fun stopTextToSpeech() {
        if(textToSpeech.isSpeaking) {
            markTwisterAsStopped()
            textToSpeech.stop()
        }
    }

    private fun playTextToSpeech() {
        if(!textToSpeech.isSpeaking) {
            textToSpeech.speak(twister.twister, TextToSpeech.QUEUE_FLUSH, getUtteranceParamsBundle(), UTTERANCE_ID)
            markTwisterAsPlaying()
        }
    }

    private fun getUtteranceParamsBundle(): Bundle {
        val bundle = Bundle()
        bundle.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, EMPTY_STRING)
        return bundle
    }

    private fun setPauseDrawable() {
        playPauseButtonIv.setImageResource(R.drawable.ic_pause_filled)
    }

    private fun setPlayDrawable() {
        playPauseButtonIv.setImageResource(R.drawable.ic_play_filled)
    }

    override fun onStart() {
        if(shouldLoopViewPager)
            loopViewPager()

        initTextToSpeech()

        super.onStart()
    }

    override fun onStop() {
        killTextToSpeech()

        if(shouldLoopViewPager)
            stopViewPagerLoop()

        super.onStop()
    }

    private fun killTextToSpeech() {
        if(isTwisterPlaying)
            markTwisterAsStopped()

        if(::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    private fun goNext(twisterIndex: Int) {

        if(canGoNext(twisterIndex))
            setTwisterObserver(viewModel.getTwisterForIndex(twisterIndex))

        else
            showToast(getStringFromId(R.string.last_item_reached), Toast.LENGTH_SHORT)
    }

    private fun goPrevious(twisterIndex: Int) {

        if(canGoPrevious(twisterIndex))
            setTwisterObserver(viewModel.getTwisterForIndex(twisterIndex))

        else
            showToast(getStringFromId(R.string.first_item_reached), Toast.LENGTH_SHORT)
    }

    private fun setTwisterObserver(twister: LiveData<Twister>) {
        stopTextToSpeech()
        markTwisterAsStopped()

        twister.observe(this, Observer {
            it?.let {
                this.twister = it
                setTwister()
            }
        })
    }

    private fun canGoNext(twisterIndex: Int): Boolean {
        return twisterIndex < MAX_TWISTER_INDEX
    }

    private fun canGoPrevious(twisterIndex: Int): Boolean {
        return twisterIndex > MIN_TWISTER_INDEX
    }

    @Suppress("SameParameterValue")
    private fun showToast(text: String, length: Int) {
        Toast.makeText(this, text, length).show()
    }

    private fun getColorFromId(colorId: Int): Int {
        return ContextCompat.getColor(this, colorId)
    }

    private fun getStringFromId(stringId: Int): String {
        return resources.getString(stringId)
    }

    private fun setTwister() {
        twisterHeaderTv.text = twister.name
        twisterContentTv.text = twister.twister

        if(!isTwisterPlaying && isTextToSpeechLoaded)
            playPauseHolder.performClick()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        animateActivityTransition(R.anim.slide_in_left_activity, R.anim.slide_out_right_activity)
    }

    private fun setComponent() {
//        DaggerReadingActivityComponent.builder()
//            .appComponent(getAppComponent())
//            .build().injectReadingActivity(this)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(ReadingActivityViewModel::class.java)
    }

    private var speechProgressListener: UtteranceProgressListener =

        object: UtteranceProgressListener() {

            override fun onDone(utteranceId: String?) {
                markTwisterAsStopped()
            }

            override fun onError(utteranceId: String?) {}

            override fun onStart(utteranceId: String?) {}

        }
}