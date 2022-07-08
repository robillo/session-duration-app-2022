package com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.common_adapter

import androidx.annotation.Keep
import java.io.Serializable

@Keep
sealed class AppEnums : Serializable {

    sealed class ImageFilter : AppEnums(), Serializable {
        object NORMAL : ImageFilter()
        object SEPIA : ImageFilter()
        object BRIGHTNESS : ImageFilter()
        object BOXBLUR : ImageFilter()
        object CONTRAST : ImageFilter()
        object DARK_BLENDER : ImageFilter()
        object GRAY_SCALE : ImageFilter()
        object POSTERIZE : ImageFilter()
        object SKETCH : ImageFilter()
        object MONOCHROME : ImageFilter()
        object INVERT : ImageFilter()
        object OVERLAY : ImageFilter()
        object OPACITY : ImageFilter()
        object HUE : ImageFilter()
    }

    sealed class LoaderType : AppEnums(), Serializable {
        object NONE : LoaderType()
        object VERTICAL : LoaderType()
        object HORIZONTAL : LoaderType()
    }

    sealed class ListenerType : AppEnums(), Serializable {
        object Demo: ListenerType()
    }
}