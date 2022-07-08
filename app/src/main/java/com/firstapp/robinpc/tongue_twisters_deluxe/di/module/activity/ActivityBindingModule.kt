package com.firstapp.robinpc.tongue_twisters_deluxe.di.module.activity

import com.firstapp.robinpc.tongue_twisters_deluxe.ui.home.HomeActivity
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.list_activity.difficulty_level.DifficultyLevelActivity
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.list_activity.length_level.LengthLevelActivity
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.splash.SplashActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBindingModule {

    @ContributesAndroidInjector
    abstract fun splashActivity(): SplashActivity

    @ContributesAndroidInjector
    abstract fun homeActivity(): HomeActivity

    @ContributesAndroidInjector
    abstract fun difficultyLevelActivity(): DifficultyLevelActivity

    @ContributesAndroidInjector
    abstract fun lengthLevelActivity(): LengthLevelActivity
}