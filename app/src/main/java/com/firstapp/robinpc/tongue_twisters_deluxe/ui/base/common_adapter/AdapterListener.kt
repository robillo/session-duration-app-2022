package com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.common_adapter

import android.view.View

interface AdapterListener {

    fun listen(click: AdapterClick?, position: Int) {}
    fun listen(click: AdapterClick?, position: Int, parentPosition: Int) {}

    fun listen(
        click: AdapterClick?,
        position: Int,
        clickType: AppEnums.ListenerType
    ) {}

    fun listen(
        click: AdapterClick?,
        position: Int,
        clickType: AppEnums.ListenerType,
        view: View
    ) {}

    fun listen(
        click: AdapterClick?,
        parentPosition: Int,
        position: Int,
        clickType: AppEnums.ListenerType,
        view: View
    ) {}

    fun onListLastItemReached() {}

    fun onFirstItemVisible(visible: Boolean) {}
}