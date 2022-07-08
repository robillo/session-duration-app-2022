package com.firstapp.robinpc.tongue_twisters_deluxe.ui.reading.reading_fragment

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.firstapp.robinpc.tongue_twisters_deluxe.R
import com.firstapp.robinpc.tongue_twisters_deluxe.TwisterApp
import com.firstapp.robinpc.tongue_twisters_deluxe.TwisterApp.Companion.analyticsUtil
import com.firstapp.robinpc.tongue_twisters_deluxe.data.model.Twister
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.BaseActivity
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.BaseFragment
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.reading.ReadingActivityViewModel
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.reading.reading_fragment.ads_pager_adapter.AdsPagerAdapter
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.AnalyticsUtil
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.Constants
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.view_pager_transformers.ZoomOutSlideTransformer
import com.google.android.gms.ads.nativead.NativeAd
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.activity_reading.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ReadingFragment : BaseFragment() {

    override fun getLayoutResId() = R.layout.fragment_reading

    override fun inject() {
        AndroidSupportInjection.inject(this)
    }

    companion object {
        private const val space = " "
        private const val EMPTY_STRING = ""
        private const val EXTRA_TWISTER_INDEX = "TWISTER_INDEX"
        private const val EXTRA_LEVEL_HEADER = "LEVEL_HEADER"
        private const val UTTERANCE_ID = "TWISTER_UTTERANCE"
        const val EXTRA_LAUNCHED_FROM = "LAUNCHED_FROM"

        fun newInstance(
            twisterIndex: Int,
            launchedFrom: Int,
            levelHeader: String = Constants.DEFAULT_LEVEL_HEADER,
            statusBarColor: Int?
        ) = ReadingFragment().apply {
            arguments = Bundle().apply {
                putInt(EXTRA_TWISTER_INDEX, twisterIndex)
                putInt(EXTRA_LAUNCHED_FROM, launchedFrom)
                putString(EXTRA_LEVEL_HEADER, levelHeader)
                statusBarColor?.let { putInt(statusBarColorBeforeLaunch, statusBarColor) }
            }
        }
    }

    private lateinit var glideManager: RequestManager
    private lateinit var adsPagerAdapter: AdsPagerAdapter
    private lateinit var currentTwister: Twister
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var viewModel: ReadingActivityViewModel
    private lateinit var loopHandler: Handler
    private lateinit var runnable: Runnable
    private lateinit var glideFadeFactory: DrawableCrossFadeFactory

    private val loopInterval = 5000L

    private var twisterIndex: Int = 0
    private var controlsColor: Int = -1
    private lateinit var levelHeader: String
    private var isTextToSpeechLoaded = false
    private var isTwisterPlaying: Boolean = false
    private var launchedFrom: Int = Constants.TYPE_DAY_TWISTER
    private var shouldLoopViewPager = false
    private var utteranceMap = HashMap<String, String>()

    private var counterValuetoShowInterstitialAd = Constants.RESET_CONSTANT_FOR_INTERSTITIAL_AD

    override fun onNativeAdsLoaded(loadedAds: ArrayList<NativeAd>) {
        try {
            shouldLoopViewPager = true
            adsPagerAdapter = AdsPagerAdapter(childFragmentManager, loadedAds, launchedFrom)
            adsViewPager.visibility = View.VISIBLE
            adsViewPager.adapter = adsPagerAdapter
            adsViewPager.setPageTransformer(false, ZoomOutSlideTransformer())
            loopViewPager()
        }
        catch (ignored: Exception) {}
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

    override fun setup() {
        analyticsUtil.logReadingScreenViewEvent()
        getVariables()
        initVariables()
        setComponent()
        setViews()
        setClickListeners()
        initialiseAdsIfRequired()
    }

    private fun initialiseAdsIfRequired() {
        if(TwisterApp.areAdsEnabled) {
            initNativeAdsLoader()
            initInterstitialAdsLoader(getInterstitialAdId())
        }
    }

    @Suppress("ConstantConditionIf")
    private fun getInterstitialAdId(): String {
        return if(TwisterApp.isTestModeEnabled)
            getString(R.string.test_interstitial_ad)
        else
            getString(R.string.reading_activity_interstitial_ad)
    }

    private fun initVariables() {
        utteranceMap[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = UTTERANCE_ID
        glideManager = Glide.with(this)
        glideFadeFactory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
    }

    fun updateTwister() {
        //todo

    }

    private fun getVariables() {
        twisterIndex = arguments?.getInt(EXTRA_TWISTER_INDEX) ?: 0
        launchedFrom = arguments?.getInt(EXTRA_LAUNCHED_FROM) ?: Constants.TYPE_DAY_TWISTER
        arguments?.getString(EXTRA_LEVEL_HEADER)?.let { levelHeader = it }
    }

    private fun setViews() {
        renderForLaunchSource()
        setTwisterObserver(viewModel.getTwisterForIndex(twisterIndex))
    }

    private fun renderForLaunchSource() {
        when(launchedFrom) {
            Constants.TYPE_LENGTH -> {
                setLengthTwisterColors()
            }
            Constants.TYPE_DIFFICULTY -> {
                setDifficultyTwisterColors()
            }
            Constants.TYPE_DAY_TWISTER -> {
                setRandomTwisterColors()
            }
        }
    }

    private fun setDifficultyTwisterColors() {
//        holderTwisterMetadata.setBackgroundColor(
//            getColorFromId(R.color.status_bar_reading_difficulty)
//        )
        iconBackgroundImageView.borderColor = getColorFromId(R.color.skeleton_one)
        setStatusBarColor(R.color.white, BaseActivity.LIGHT_STATUS_BAR)
        setLevelDetailsBackground(R.color.level_details_reading_difficulty)
        setLevelNameColor(R.color.level_name_reading_difficulty)
        setCompletionPercentageTextColor(R.color.completion_percentage_reading_difficulty)
        setCompleteTextColor(R.color.complete_text_reading_difficulty)
        setLevelProgressHolderBackground(R.color.level_progress_holder_reading_difficulty)
        renderControls(R.color.status_bar_reading_difficulty)
        twisterHeaderTv.setTextColor(getColorFromId(R.color.difficulty_reading_activity_twister_color))
        setProgressBarColors(
            R.color.level_progress_background_reading_difficulty,
            R.color.level_progress_progress_reading_difficulty
        )
        playerControlsHolder.setBackgroundColor(
            getColorFromId(R.color.status_bar_reading_difficulty)
        )
    }

    private fun setLengthTwisterColors() {
//        holderTwisterMetadata.setBackgroundColor(
//            getColorFromId(R.color.status_bar_reading_length)
//        )
        iconBackgroundImageView.borderColor = getColorFromId(R.color.skeleton_one)
        setStatusBarColor(R.color.white, BaseActivity.LIGHT_STATUS_BAR)
        setLevelDetailsBackground(R.color.level_details_reading_length)
        setLevelNameColor(R.color.level_name_reading_length)
        setCompletionPercentageTextColor(R.color.completion_percentage_reading_length)
        setCompleteTextColor(R.color.complete_text_reading_length)
        setLevelProgressHolderBackground(R.color.level_progress_holder_reading_length)
        twisterHeaderTv.setTextColor(getColorFromId(R.color.length_reading_activity_twister_color))
        renderControls(R.color.status_bar_reading_length)
        setProgressBarColors(
            R.color.level_progress_background_reading_length,
            R.color.level_progress_progress_reading_length
        )
        playerControlsHolder.setBackgroundColor(
            getColorFromId(R.color.status_bar_reading_length)
        )
    }

    private fun setRandomTwisterColors() {
//        holderTwisterMetadata.setBackgroundColor(
//            getColorFromId(R.color.status_bar_reading_random)
//        )
        iconBackgroundImageView.borderColor = getColorFromId(R.color.skeleton_one)
        setStatusBarColor(R.color.white, BaseActivity.LIGHT_STATUS_BAR)
        setLevelDetailsBackground(R.color.level_details_reading_random)
        setLevelNameColor(R.color.level_name_reading_random)
        setCompletionPercentageTextColor(R.color.completion_percentage_reading_random)
        setCompleteTextColor(R.color.complete_text_reading_random)
        setLevelProgressHolderBackground(R.color.level_progress_holder_reading_random)
        twisterHeaderTv.setTextColor(getColorFromId(R.color.day_twister_reading_activity_twister_color))
        renderControls(R.color.status_bar_reading_random)
        setProgressBarColors(
            R.color.level_progress_background_reading_random,
            R.color.level_progress_progress_reading_random
        )
        playerControlsHolder.setBackgroundColor(
            getColorFromId(R.color.status_bar_reading_random)
        )
    }

    private fun setProgressBarColors(progressBackgroundColorId: Int, progressColorId: Int) {
//        levelProgressBar.progressBackgroundColor = getColorFromId(progressBackgroundColorId)
//        levelProgressBar.progressColor = getColorFromId(progressColorId)
    }

    private fun setCompletionPercentageTextColor(colorId: Int) {
        completionPercentageTv.setTextColor(getColorFromId(colorId))
    }

    private fun setCompleteTextColor(colorId: Int) {
        completeStaticTv.setTextColor(getColorFromId(colorId))
    }

    private fun setLevelNameColor(colorId: Int) {
        levelNameTv.setTextColor(getColorFromId(colorId))
    }

    private fun setLevelDetailsBackground(colorId: Int) {
        levelDetailsHolder.setBackgroundColor(getColorFromId(colorId))
    }

    private fun setLevelProgressHolderBackground(colorId: Int) {
        progressHolder.setBackgroundColor(getColorFromId(colorId))
    }

    private fun initTextToSpeech() {
        activity?.applicationContext?.let {
            textToSpeech = TextToSpeech(it) { status ->

                Log.e("mytag", "tts load status $status")

                if (isTextToSpeechSuccess(status)) {
                    isTextToSpeechLoaded = true

                    if (!isTwisterPlaying)
                        playPauseHolder.performClick()

                    textToSpeech.language = Locale.ENGLISH
                    textToSpeech.setOnUtteranceProgressListener(speechProgressListener)
                }
            }
        }
    }

    private fun isTextToSpeechSuccess(status: Int): Boolean {
        return status != TextToSpeech.ERROR
    }

    private fun renderControls(controlsColorId: Int) {
        controlsColor = getColorFromId(controlsColorId)
        setControlsColors()
        setPlayDrawable()
    }

    private fun setControlsColors() {
        nextHolder.setBackgroundColor(controlsColor)
        previousHolder.setBackgroundColor(controlsColor)
        playPauseHolder.setBackgroundColor(controlsColor)
//        nextButtonIv.setColorFilter(controlsColor)
//        playPauseButtonIv.setColorFilter(controlsColor)
//        previousButtonIv.setColorFilter(controlsColor)
    }

    private fun setClickListeners() {
        previousHolder.setOnClickListener {
            analyticsUtil.logPreviousClickedEvent(
                AnalyticsUtil.readingScreen, currentTwister
            )
            if(canDecrementCounter()) {
                decrementCounterByUnitValue()
                goPrevious((currentTwister.id - Constants.UNIT_VALUE_INT))
            }
            else {
                resetCounter()
                showReadingActivityInterstitialAd()
            }
        }
        playPauseHolder.setOnClickListener {
            if(isTextToSpeechLoaded) {
                if(isTwisterPlaying) {
                    analyticsUtil.logPauseClickedEvent(
                        AnalyticsUtil.readingScreen, currentTwister
                    )
                    setPlayDrawable()
                    stopTextToSpeech()
                }
                else {
                    analyticsUtil.logPlayClickedEvent(
                        AnalyticsUtil.readingScreen, currentTwister
                    )
                    setPauseDrawable()
                    playTextToSpeech()
                }
            }
            else analyticsUtil.logUninitializedTextToSpeechLogevent(AnalyticsUtil.readingScreen)
        }
        nextHolder.setOnClickListener {
            analyticsUtil.logNextClickedEvent(
                AnalyticsUtil.readingScreen, currentTwister
            )
            if(canDecrementCounter()) {
                decrementCounterByUnitValue()
                goNext((currentTwister.id + Constants.UNIT_VALUE_INT))
            }
            else {
                resetCounter()
                showReadingActivityInterstitialAd()
            }
        }
    }

    private fun showReadingActivityInterstitialAd() {
        setPlayDrawable()
        stopTextToSpeech()
        showInterstitialAd(getInterstitialAdId())
        //todo ..
        //todo ..
        //todo .. go next or go previous after ad dismiss
    }

    private fun resetCounter() {
        counterValuetoShowInterstitialAd = Constants.RESET_CONSTANT_FOR_INTERSTITIAL_AD
    }

    private fun decrementCounterByUnitValue() {
        counterValuetoShowInterstitialAd -= 1
    }

    private fun canDecrementCounter(): Boolean {
        //todo - check internet connection and if ad is loaded also
        if(!TwisterApp.areAdsEnabled) return true
        return counterValuetoShowInterstitialAd != 0
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
        if(::textToSpeech.isInitialized && textToSpeech.isSpeaking) {
            markTwisterAsStopped()
            textToSpeech.stop()
        }
    }

    private fun playTextToSpeech() {
        if(!textToSpeech.isSpeaking) {
            textToSpeech.speak(currentTwister.twister, TextToSpeech.QUEUE_FLUSH, getUtteranceParamsBundle(), UTTERANCE_ID)
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

        twister.observe(this) {
            it?.let {
                this.currentTwister = it
                setTwister()
            }
        }
    }

    private fun canGoNext(twisterIndex: Int): Boolean {
        return twisterIndex < Constants.MAX_TWISTER_INDEX
    }

    private fun canGoPrevious(twisterIndex: Int): Boolean {
        return twisterIndex > Constants.MIN_TWISTER_INDEX
    }

    @Suppress("SameParameterValue")
    private fun showToast(text: String, length: Int) {
        Toast.makeText(requireContext(), text, length).show()
    }

    private fun getColorFromId(colorId: Int): Int {
        return ContextCompat.getColor(requireContext(), colorId)
    }

    private fun getStringFromId(stringId: Int): String {
        return resources.getString(stringId)
    }

    private fun setTwister() {
        setTwisterTexts()
        renderTwisterIcon()
        continuePlayingIfStopped()
    }

    var allWords: List<String>? = null
    var currentTwisterString: String? = null
    var wordCount = -1

    private fun setTwisterTexts() {
        currentTwisterString = currentTwister.twister
        allWords = currentTwisterString?.split(space)
        //wordCount = allWords?.size ?: 0

        twisterHeaderTv.text = currentTwister.name
        twisterContentTv.text = currentTwister.twister
        renderTwisterLevelNameForLaunchOrigin()
    }

    private fun renderTwisterLevelNameForLaunchOrigin() {
        when(launchedFrom) {
            Constants.TYPE_LENGTH -> {
                val lengthLevelName = getLengthTwisterNameByNumber(currentTwister.length)
                fetchLengthLevelDetails(lengthLevelName)
            }
            Constants.TYPE_DIFFICULTY -> {
                val difficultyLevelName = String.format(
                    getString(R.string.difficulty_level_number),
                    currentTwister.difficulty
                )
                fetchDifficultyLevelDetails(difficultyLevelName)
            }
            Constants.TYPE_DAY_TWISTER -> {
                levelHeaderTv.text = Constants.DEFAULT_LEVEL_HEADER
            }
        }
    }

    private fun renderTwisterIcon() {
        if(currentTwister.iconUrl.isNotEmpty()) {
            twisterIconImageView.visibility = View.VISIBLE
            iconBackgroundImageView.visibility = View.VISIBLE

            glideManager.load(currentTwister.iconUrl).placeholder(R.drawable.icon_placeholder)
                .transition(
                    DrawableTransitionOptions.withCrossFade(glideFadeFactory)
                ).into(twisterIconImageView)
        }
        else {
            twisterIconImageView.setImageBitmap(null)
            twisterIconImageView.visibility = View.GONE
            iconBackgroundImageView.visibility = View.GONE
        }
    }

    private fun continuePlayingIfStopped() {
        if(!isTwisterPlaying && isTextToSpeechLoaded)
            playPauseHolder.performClick()
    }

    private fun fetchLengthLevelDetails(levelId: String) {
        viewModel.fetchLengthLevelDetailsForLevel(levelId).observe(this) {
            levelHeaderTv.text = it?.title
            levelNameTv.text = it?.expandedTitle
            levelNameNewTv.text = it?.title
        }
    }

    private fun fetchDifficultyLevelDetails(levelId: String) {
        viewModel.fetchDifficultyLevelDetailsForLevel(levelId).observe(this) {
            levelHeaderTv.text = it?.title
            levelNameTv.text = it?.expandedTitle
            levelNameNewTv.text = it?.title
        }
    }

    private fun getLengthTwisterNameByNumber(lengthNumber: Int): String {
        return when(lengthNumber) {
            1 -> getString(R.string.small_length_caps)
            2 -> getString(R.string.medium_length_caps)
            else -> getString(R.string.long_length_caps)
        }
    }

    private fun setComponent() {
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(ReadingActivityViewModel::class.java)
    }

    private fun highlightText(start: Int, end: Int, frame: Int) {
        lifecycleScope.launch(Dispatchers.Main) {
            wordCount += 1
            Log.d("mytag"," on highlight text called $wordCount $allWords $currentTwisterString")
            var word = EMPTY_STRING
            allWords?.let {
                if(it.size > wordCount)
                    word = it[wordCount]
            }
            val highlightIndex = (currentTwisterString ?: EMPTY_STRING).indexOf(word) + word.length
            val text = SpannableString(twisterContentTv.text)
            text.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(requireContext(), R.color.twister_percentage_read_color)
                ),
                0,
                highlightIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            text.setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                highlightIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            twisterContentTv.text = text
            Log.d("mytag", "start is $start - end is $end - frame is $frame")
        }
    }

    private var speechProgressListener: UtteranceProgressListener =

        object: UtteranceProgressListener() {

            override fun onDone(utteranceId: String?) {
                markTwisterAsStopped()
                wordCount = -1
            }

            override fun onError(utteranceId: String?) {}

            override fun onStart(utteranceId: String?) {}

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                super.onRangeStart(utteranceId, start, end, frame)
                Log.d("mytag","on range start called")
                highlightText(start, end, frame)
            }
        }
}