package com.firstapp.robinpc.tongue_twisters_deluxe.di.module.service

import com.firstapp.robinpc.tongue_twisters_deluxe.utils.service.RecurringNotificationService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface ServiceBindingModule {

    @ContributesAndroidInjector
    fun recurringNotificationService(): RecurringNotificationService
}