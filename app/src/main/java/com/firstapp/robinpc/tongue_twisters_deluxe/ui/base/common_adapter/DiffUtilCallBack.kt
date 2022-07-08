package com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.common_adapter

import androidx.recyclerview.widget.ListUpdateCallback
import javax.inject.Singleton

@Singleton
class DiffUtilCallBack : ListUpdateCallback {

    init {
        Injector.get().inject(this)
    }

//    @Inject
//    lateinit var singletonData: SingletonData

    var adapter: BaseListAdapter? = null
    fun bind(adapter: BaseListAdapter?) {
        this.adapter = adapter
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        adapter?.notifyItemRangeChanged(position, count, payload)
    }

    override fun onInserted(position: Int, count: Int) {
        adapter?.notifyItemRangeInserted(position, count)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        adapter?.notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRemoved(position: Int, count: Int) {
        adapter?.notifyItemRangeRemoved(position, count)
    }
}