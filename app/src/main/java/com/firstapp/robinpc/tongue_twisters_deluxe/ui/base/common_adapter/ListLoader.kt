package com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.common_adapter

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ListLoader(
    @SerializedName("loading")
    @Expose
    var loading: Boolean = true
) : RecyclerItem {

    override val id: String?
        get() = "StaticID"

    override fun equals(o: Any?): Boolean {
        if (o is ListLoader) {
            val toCompare = o as ListLoader?
            return this.id.equals(toCompare?.id)
        }
        return false
    }

    override fun hashCode(): Int {
        return loading.hashCode()
    }
}