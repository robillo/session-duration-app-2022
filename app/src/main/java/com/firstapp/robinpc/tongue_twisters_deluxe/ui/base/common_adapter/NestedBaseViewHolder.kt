package com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.common_adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class NestedBaseViewHolder(view: View) : RecyclerView.ViewHolder(view), AdapterListener {
    var adapter: CommonListAdapter? = null
}