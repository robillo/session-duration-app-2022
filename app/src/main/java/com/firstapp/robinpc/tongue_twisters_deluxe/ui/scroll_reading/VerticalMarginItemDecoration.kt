package com.firstapp.robinpc.tongue_twisters_deluxe.ui.scroll_reading

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView

class VerticalMarginItemDecoration(context: Context, @DimenRes verticalMarginInDp: Int) :
    RecyclerView.ItemDecoration() {

    private val verticalMarginInPx: Int =
        context.resources.getDimension(verticalMarginInDp).toInt()

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        outRect.top = verticalMarginInPx
        outRect.bottom = verticalMarginInPx
    }
}