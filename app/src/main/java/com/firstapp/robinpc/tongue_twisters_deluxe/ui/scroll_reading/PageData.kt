package com.firstapp.robinpc.tongue_twisters_deluxe.ui.scroll_reading

import com.firstapp.robinpc.tongue_twisters_deluxe.data.model.Twister
import com.google.android.gms.ads.nativead.NativeAd

data class PageData(
    var twister: Twister? = null,
    var nativeAd: NativeAd? = null
)