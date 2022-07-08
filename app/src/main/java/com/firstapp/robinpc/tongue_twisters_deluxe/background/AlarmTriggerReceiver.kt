package com.firstapp.robinpc.tongue_twisters_deluxe.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.service.RecurringNotificationService
import java.lang.Exception

class AlarmTriggerReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            try { it.startService(RecurringNotificationService.newIntent(it)) } catch (ignored: Exception) {}
        }
    }
}