package com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.common_adapter

import com.firstapp.robinpc.tongue_twisters_deluxe.TwisterApp
import com.firstapp.robinpc.tongue_twisters_deluxe.di.component.AppComponent

object Injector {
    @JvmStatic fun get(): AppComponent = TwisterApp.instance.component
}