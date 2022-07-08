package com.firstapp.robinpc.tongue_twisters_deluxe.utils

import android.os.Bundle
import com.amplitude.api.Amplitude
import com.firstapp.robinpc.tongue_twisters_deluxe.data.model.Twister
import com.google.firebase.analytics.FirebaseAnalytics
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

class AnalyticsUtil(
    private val firebaseAnalytics: FirebaseAnalytics
    ) {

    companion object {
        const val typeKey = "type"
        const val screenNameKey = "screenName"
        const val valueKey = "valueName"

        //parent event
        const val landed = "landed"
        const val click = "click"
        const val log = "log"

        //event types

        //screen names
        const val splashScreen = "Splash"
        const val readingScreen = "Reading"
        const val homeScreen = "Home"
        const val lengthLevelScreen = "length Level"
        const val difficultyLevelScreen = "Difficulty Level"

        //types
        const val typePreviousClick = "previous click"
        const val typeNextClick = "next click"
        const val typePlayClick = "play click"
        const val typePauseClick = "pause click"
    }

    private fun logEvent(
        parentEvent: String,
        type: String? = null,
        screenName: String? = null,
        value: Any? = null
    ) {
        val firebaseBundle = Bundle()
        val amplitudeJsonArray = JSONObject()
        type?.let {
            firebaseBundle.putString(typeKey, it)
            amplitudeJsonArray.put(typeKey, it)
        }
        screenName?.let {
            firebaseBundle.putString(screenNameKey, it)
            amplitudeJsonArray.put(screenNameKey, it)
        }
        value?.let {
            try { firebaseBundle.putString(valueKey, it.toString()) } catch (ignored: Exception) {}
            amplitudeJsonArray.put(valueKey, it)
        }
        firebaseAnalytics.logEvent(parentEvent, firebaseBundle)
        Amplitude.getInstance().logEvent(parentEvent, amplitudeJsonArray)
    }

    fun logSplashScreenViewEvent() {
        logEvent(landed, screenName = splashScreen)
    }

    fun logReadingScreenViewEvent() {
        logEvent(landed, screenName = readingScreen)
    }

    fun logHomeScreenViewEvent() {
        logEvent(landed, screenName = homeScreen)
    }

    fun logLengthLevelScreenViewEvent() {
        logEvent(landed, screenName = lengthLevelScreen)
    }

    fun logDifficultyLevelScreenViewEvent() {
        logEvent(landed, screenName = difficultyLevelScreen)
    }

    fun logClickEvent(clickType: String, screenName: String, value: Any?) {
        logEvent(click, clickType, screenName, value)
    }

    fun logNavigationEvent(type: String, screenName: String, value: Any?) {
        logEvent(click, type, screenName, value)
    }

    fun logPreviousClickedEvent(screenName: String, twisterBeforeAction: Twister) {
        logClickEvent(typePreviousClick, screenName, twisterBeforeAction.name)
    }

    fun logNextClickedEvent(screenName: String, twisterBeforeAction: Twister) {
        logClickEvent(typeNextClick, screenName, twisterBeforeAction.name)
    }

    fun logPlayClickedEvent(screenName: String, twisterBeforeAction: Twister) {
        logClickEvent(typePlayClick, screenName, twisterBeforeAction.name)
    }

    fun logPauseClickedEvent(screenName: String, twisterBeforeAction: Twister) {
        logClickEvent(typePauseClick, screenName, twisterBeforeAction.name)
    }

    fun logUninitializedTextToSpeechLogevent(screenName: String) {
        logEvent(log, screenName = screenName)
    }
}