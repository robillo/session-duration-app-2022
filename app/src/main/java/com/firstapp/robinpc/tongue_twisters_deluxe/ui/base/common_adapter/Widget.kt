package com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.common_adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

abstract class Cell<T> {

    abstract fun belongsTo(item: T?): Boolean
    abstract fun type(): Int
    abstract fun destroy()
    open fun onViewRecycled() {
    }

    open fun getNestedAdapter(): RecyclerView.Adapter<*>? {
        return null
    }

    abstract fun holder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    abstract fun bind(
        holder: RecyclerView.ViewHolder,
        item: T?,
        listener: AdapterListener?,
        recyclerViewPool: RecyclerView.RecycledViewPool,
        position: Int
    )

    open fun postNextItemForP2p(nextItem: RecyclerItem?) {
    }

    protected fun ViewGroup.viewOf(@LayoutRes resource: Int): View {
        return LayoutInflater
            .from(context)
            .inflate(resource, this, false)
    }
}