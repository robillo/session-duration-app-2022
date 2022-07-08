package com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.common_adapter

import androidx.recyclerview.widget.DiffUtil

class BaseDiffUtil(
    private val oldList: List<RecyclerItem>,
    private val newList: List<RecyclerItem>
) :
    DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        try {
            return oldList[oldPosition].id == newList[newPosition].id
        }
        catch (e: Exception) {}
        return false
    }
}