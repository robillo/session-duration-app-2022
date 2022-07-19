package com.firstapp.robinpc.tongue_twisters_deluxe.ui.scroll_reading

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.reading.reading_fragment.ReadingFragment
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.Constants

class ScrollReadingAdapter(
    fragmentManager: FragmentManager,
    var pageList: MutableList<PageData>,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return pageList.size
    }

    override fun createFragment(position: Int): Fragment {
        return ReadingFragment.newInstance(
            pageList[position].twister?.id ?: -1, Constants.TYPE_DIFFICULTY
        )
    }

    fun add(index: Int, pageData: PageData) {
        pageList.add(index, pageData)
        notifyItemChanged(index)
    }

    fun refreshFragment(index: Int, pageData: PageData) {
        pageList[index] = pageData
        notifyItemChanged(index)
    }

    fun remove(index: Int) {
        pageList.removeAt(index)
        notifyItemChanged(index)
    }

    override fun getItemId(position: Int): Long {
        return pageList[position].hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return pageList.find { it.hashCode().toLong() == itemId } != null
    }
}