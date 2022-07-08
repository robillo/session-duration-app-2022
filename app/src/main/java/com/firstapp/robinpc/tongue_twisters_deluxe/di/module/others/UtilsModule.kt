package com.firstapp.robinpc.tongue_twisters_deluxe.di.module.others

import android.content.Context
import android.telephony.TelephonyManager
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

class UtilsModule @Inject constructor(private val context: Context) {

    fun recogniseCountry(): String? {
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val simCountry = tm.simCountryIso
            if (simCountry != null && simCountry.length == 2) { // SIM country code is available
                return simCountry.lowercase(Locale.US)
            } else if (tm.phoneType != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                val networkCountry = tm.networkCountryIso
                if (networkCountry != null && networkCountry.length == 2) { // network country code is available
                    return networkCountry.lowercase(Locale.US)
                }
            }
        }
        catch (ignored: Exception) {}
        return null
    }
}