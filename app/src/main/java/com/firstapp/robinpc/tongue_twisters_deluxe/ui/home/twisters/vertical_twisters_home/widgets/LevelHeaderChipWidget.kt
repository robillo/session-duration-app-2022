package com.firstapp.robinpc.tongue_twisters_deluxe.ui.home.twisters.vertical_twisters_home.widgets

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.firstapp.robinpc.tongue_twisters_deluxe.R
import com.firstapp.robinpc.tongue_twisters_deluxe.data.model.DifficultyLevel
import com.firstapp.robinpc.tongue_twisters_deluxe.databinding.LevelHeaderChipWidgetBinding
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.common_adapter.AdapterListener
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.common_adapter.Cell
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.common_adapter.RecyclerItem

class LevelHeaderChipWidget(
    val method: (plan: DifficultyLevel, position: Int) -> Unit
): Cell<RecyclerItem>() {

    override fun destroy() {}
    override fun type() = R.layout.level_header_chip_widget
    override fun belongsTo(item: RecyclerItem?) = item is DifficultyLevel
    override fun holder(parent: ViewGroup, viewType: Int) = ViewHolder(parent.viewOf(type()))

    override fun bind(
        holder: RecyclerView.ViewHolder,
        item: RecyclerItem?,
        listener: AdapterListener?,
        recyclerViewPool: RecyclerView.RecycledViewPool,
        position: Int
    ) {
        if (holder is ViewHolder && item is DifficultyLevel) holder.bind(item, method, position)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = LevelHeaderChipWidgetBinding.bind(view)

        fun bind(
            item: RecyclerItem?,
            method: (plan: DifficultyLevel, position: Int) -> Unit, position: Int
        ) {
            if(item is DifficultyLevel) {
                binding.parentView.setBackgroundResource(
                    if(item.isSelected) R.drawable.bg_dark_purple_one_dull_rounded
                    else R.drawable.bg_rounded_stroked_light_gray
                )
                binding.titleTv.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        if(item.isSelected) R.color.white
                        else R.color.translucent_black_dark
                    )
                )
                binding.titleTv.text = item.title
                binding.root.setOnClickListener { method.invoke(item, position) }
            }
        }
    }
}