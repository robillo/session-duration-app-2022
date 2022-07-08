package com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.common_adapter

interface RecyclerItem : AdapterClick {
    val id: String?
    override fun equals(other: Any?): Boolean
}