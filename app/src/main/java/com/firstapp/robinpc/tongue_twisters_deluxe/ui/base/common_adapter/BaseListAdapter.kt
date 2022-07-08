package com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.common_adapter

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firstapp.robinpc.tongue_twisters_deluxe.R
import org.apache.commons.lang3.tuple.MutablePair

abstract class BaseListAdapter(
    vararg types: Cell<RecyclerItem>,
    val listener: AdapterListener? = null,
    val loaderType: AppEnums.LoaderType
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mHeaderItem: RecyclerItem? = null
    private var isHeaderAdded: Boolean = false
    private val itemThreshold: Int = 5
    private var isLoading: Boolean = false
    private var mCurrState: Boolean = false
    private var mLastState: Boolean = false

    private val minAllowedCellVisiblePercentForImpressionEvent = 1

    private val visibleThreshold = 3
    private var mLastVisibleItem: Int = 0
    private var mTotalItemCount: Int = 0

    lateinit var recyclerView: RecyclerView
    lateinit var recyclerViewPool: RecyclerView.RecycledViewPool
    val cellTypes: CellTypes<RecyclerItem> = CellTypes(*types)

//    private var showLoader: Boolean = false

    fun setShowLoader(show: Boolean) {
        if (loaderType != AppEnums.LoaderType.NONE) {
//            Timber.d(show.toString())
            isLoading = show
            if(::recyclerView.isInitialized) {
                recyclerView?.post { notifyItemChanged(itemCount - 1) }
            }
        }
    }

    fun addHeaderView(headerItem: RecyclerItem, position: Int = 0) {
        isHeaderAdded = true
        mHeaderItem = headerItem
        notifyItemChanged(position)
    }

    var mItemList: ArrayList<RecyclerItem> = ArrayList()

    fun submitList(list: List<RecyclerItem>?) {
        mItemList = ArrayList()
        list?.let {
            mItemList.addAll(ArrayList(it))
            notifyDataSetChanged()
            isLoading = false
            setShowLoader(isLoading)
        }

    }

    fun submitWithoutNotify(list: List<RecyclerItem>?, diffUtilCallBack: DiffUtilCallBack) {
        diffUtilCallBack.bind(this)
        mItemList.clear()
        list?.let {
            val diffCallback = BaseDiffUtil(mItemList, list)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            mItemList.clear()
            mItemList.addAll(list)
            diffResult.dispatchUpdatesTo(diffUtilCallBack)
            setShowLoader(isLoading)
        }

    }

    fun reSubmitList(modelList: List<RecyclerItem>) {
        mItemList = ArrayList()
        mItemList.addAll(modelList)
        notifyDataSetChanged()
    }

    fun updateList(modelList: List<RecyclerItem>) {
        try {
            isLoading = false
            var oldposition = mItemList.size

            if (isHeaderAdded) {
                ++oldposition
            }

            val newcount = modelList.size
            mItemList.addAll(modelList)
            if (oldposition <= 0)
                notifyDataSetChanged()
            else
                notifyItemRangeInserted(oldposition, newcount)

            setShowLoader(isLoading)
        } catch (e: Exception) {}
    }

    fun addItem(item: RecyclerItem) {
        try {
            isLoading = false
            var oldposition = mItemList.size

            if (isHeaderAdded) {
                ++oldposition
            }

            mItemList.add(item)
            if (oldposition <= 0)
                notifyDataSetChanged()
            else
                notifyItemInserted(oldposition)

            setShowLoader(isLoading)
        } catch (e: Exception) {}
    }

    fun updateListDistinct(modelList: List<RecyclerItem>) {
        try {
            isLoading = false
            var oldposition = mItemList.size

            if (isHeaderAdded) {
                ++oldposition
            }

            var newCount = 0
            modelList.forEach {
                if(!mItemList.contains(it)) {
                    mItemList.add(it)
                    newCount++
                }
            }
            if (oldposition <= 0)
                notifyDataSetChanged()
            else
                notifyItemRangeInserted(oldposition, newCount)

            setShowLoader(isLoading)
        }
        catch (ignored: Exception) {}
    }

    fun updateList(position: Int, modelList: List<RecyclerItem>) {
        try {
            isLoading = false
            var oldposition = mItemList.size

            if (isHeaderAdded) {
                ++oldposition
            }

            val newcount = modelList.size
            mItemList.addAll(0, modelList)
            if (oldposition <= 0)
                notifyDataSetChanged()
            else
                notifyItemRangeInserted(0, newcount)

            setShowLoader(isLoading)
        }
        catch (e: Exception) {}
    }

    fun updateExistingList(startPos: Int, modelList: List<RecyclerItem>) {
        try {
            isLoading = false
            mItemList.subList(startPos, mItemList.size).clear()
//            notifyItemRangeRemoved(startPos, mItemList.size)

            mItemList.addAll(startPos, modelList)
//            notifyItemRangeInserted(startPos, modelList.size)
            notifyDataSetChanged()

            setShowLoader(isLoading)
        }
        catch (e: Exception) {}
    }


    fun updateDiffUtilList(modelList: List<RecyclerItem>) {
        try {
            isLoading = false

            val diffCallback = BaseDiffUtil(mItemList, modelList)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            mItemList.clear()
            mItemList.addAll(modelList)
            diffResult.dispatchUpdatesTo(this)

            setShowLoader(isLoading)
        }
        catch (e: Exception) {}
    }

    fun getItem(position: Int): RecyclerItem? {
        try {
            var pos = position
            if (isHeaderAdded) {
                if (position == 0)
                    return mHeaderItem
                pos = position - 1
            }

            if (loaderType != AppEnums.LoaderType.NONE) {
                if (pos == mItemList.size)
                    return ListLoader(isLoading)
                else if (mItemList.size > pos)
                    return mItemList[pos]
            } else {
                if (mItemList.size > pos)
                    return mItemList[pos]
            }
        } catch (e: Exception) {}
        return null
    }

    override fun getItemCount(): Int {
        var count = when (loaderType) {
            AppEnums.LoaderType.NONE -> mItemList.size
            else -> mItemList.size + 1
        }
        if (isHeaderAdded) {
            ++count
        }

        return count
    }

    fun getActualItemCount(): Int {
        return mItemList.size
    }

    fun getItemsForRange(range: MutablePair<Int, Int>): MutableList<MutablePair<RecyclerItem, Int>> {
        val offsetToMakeEndItemInclusive = 1

        val headerItemOffset = getPostListHeaderOffset()
        updatePostItemIndicesRangeForPostOffset(range)

        if(range.left < 0) range.left = 0

        val postsInRangeList = getEmptyItemsRangeList()

        if(isInvalidIndicesRange(range)) return postsInRangeList

        for(index in range.left .. (range.right + offsetToMakeEndItemInclusive)) {
            if(::recyclerView.isInitialized)
                recyclerView.findViewHolderForAdapterPosition(index)?.let {
                    addItemsToRangeListBasedOnVisibilityPercentage(
                        index, headerItemOffset,
                        getVisibleHeightPercentage(it.itemView), postsInRangeList, range
                    )
                }
        }

        return postsInRangeList
    }

    private fun getEmptyItemsRangeList(): MutableList<MutablePair<RecyclerItem, Int>> {
        return ArrayList()
    }

    private fun addItemsToRangeListBasedOnVisibilityPercentage(
        index: Int,
        headerOffset: Int,
        visiblePercentage: Double,
        itemsList: MutableList<MutablePair<RecyclerItem, Int>>,
        range: MutablePair<Int, Int>
    ) {
        val indexInList = index - headerOffset

        if(visiblePercentage > minAllowedCellVisiblePercentForImpressionEvent) {
            val wasHeaderAdded =
                addHeaderItemBasedOnVisibilityIfPossible(indexInList, itemsList)

            if(!wasHeaderAdded)
                itemsList.add(MutablePair(mItemList[indexInList], index))
        }
        else {
            addSingleVisiblePostEvenIfItIsLessVisible(
                range, headerOffset, index, indexInList, itemsList
            )
        }
    }

    private fun addHeaderItemBasedOnVisibilityIfPossible(
        indexInList: Int,
        itemsList: MutableList<MutablePair<RecyclerItem, Int>>
    ): Boolean {
        if(isHeaderAdded && (indexInList < 0)) {
            mHeaderItem?.let {
                itemsList.add(MutablePair(it, 0))
                return true
            }
        }
        return false
    }

    private fun addSingleVisiblePostEvenIfItIsLessVisible(
        range: MutablePair<Int, Int>,
        headerOffset: Int,
        index: Int,
        indexInList: Int,
        itemsList: MutableList<MutablePair<RecyclerItem, Int>>
    ) {
        val isSinglePostVisible = range.left == range.right
        val isPostNotHeader = range.left > headerOffset
        if(isSinglePostVisible && isPostNotHeader)
            itemsList.add(MutablePair(mItemList[indexInList], index))
    }

    private fun isInvalidIndicesRange(range: MutablePair<Int, Int>): Boolean {
        val offsetToMakeEndItemInclusive = 1
        return (range.left < 0 || range.right < 0) ||
            (range.right + offsetToMakeEndItemInclusive > (mItemList.size - 1))
    }

    private fun getPostListHeaderOffset(): Int {
        return if(isHeaderAdded) 1 else 0
    }

    private fun updatePostItemIndicesRangeForPostOffset(range: MutablePair<Int, Int>) {
        val headerOffset = getPostListHeaderOffset()
        range.left = range.left - headerOffset
        range.right = range.right - headerOffset
    }

    private fun getVisibleHeightPercentage(view: View): Double {
        val itemRect = Rect()
        view.getLocalVisibleRect(itemRect)

        val visibleHeight: Double = itemRect.height().toDouble()
        val height: Double = view.measuredHeight.toDouble()
        return visibleHeight / height * 100
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        this.recyclerViewPool = recyclerView.recycledViewPool
//        this.mItemList.add(ListLoader(true))
    }

    override fun getItemViewType(position: Int): Int {
//        Timber.i("getItemViewType %d ", position)
        val item = getItem(position)
        cellTypes.of(item)?.let {
            return it.type()
        }
        return RecyclerView.INVALID_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        cellTypes.of(viewType)?.let {
            return it.holder(parent, viewType)
        }
        return EmptyViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.empty_view, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        Timber.i("onBindViewHolder $position")
        val item = getItem(position)
        cellTypes.of(item)?.bind(holder, item, listener, recyclerViewPool, position)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
//        if (holder is VideoBaseViewHolder) {
//            holder.onViewAttachedToWindow()
//        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
//        if (holder is VideoBaseViewHolder) {
//            holder.onViewDetachedFromWindow()
//        }
    }

    fun onScrollListener(scrollListener: AdapterListener) {
        if(::recyclerView.isInitialized) {
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int, dy: Int
                ) {
                    super.onScrolled(recyclerView, dx, dy)
                    try {
                        val layoutManager = recyclerView.layoutManager
                        if (layoutManager is LinearLayoutManager) {
                            mTotalItemCount = layoutManager.itemCount
                            mLastVisibleItem =
                                layoutManager.findLastVisibleItemPosition()

                            if (!isLoading
                                && mTotalItemCount <= mLastVisibleItem + visibleThreshold
                                && itemCount > itemThreshold
                            ) {
                                isLoading = true
                                setShowLoader(isLoading)
                                scrollListener.onListLastItemReached()
                            }

                            val firstVisiblePosition: Int = layoutManager.findFirstVisibleItemPosition()
                            if (firstVisiblePosition <= visibleThreshold) {
                                mCurrState = true
                                if (mCurrState != mLastState) {
                                    scrollListener.onFirstItemVisible(mCurrState)
                                    mLastState = mCurrState
                                }
                            } else {
                                mCurrState = false
                                if (mCurrState != mLastState) {
                                    scrollListener.onFirstItemVisible(mCurrState)
                                    mLastState = mCurrState
                                }
                            }

                        }
                    }
                    catch (e: Exception) {}
                }
            })
        }
    }

    fun onDestroy() {
        try {
            cellTypes.destroy()
        }
        catch (e: Exception) {}
    }

    fun clearList() {
        mItemList = ArrayList()
        notifyDataSetChanged()
        if (isHeaderAdded) {
            notifyItemChanged(0)
        }
    }

    fun clearListFromPosition(position: Int) {
        val oldList = mItemList.subList(0, minOf(mItemList.size, position))
        mItemList = ArrayList()
        mItemList.addAll(oldList)
        notifyDataSetChanged()
        if (isHeaderAdded) {
            notifyItemChanged(0)
        }
    }

    fun removeItemAtPosition(position: Int) {
        try {
            if (mItemList.size > position) {
                mItemList.removeAt(position)
                if(::recyclerView.isInitialized)
                    recyclerView.post { notifyItemRemoved(position) }
            }
        }
        catch (e: Exception) {}
    }

    fun removeItem(item: RecyclerItem) {
        if(mItemList.isNullOrEmpty()) return
        try {
            var removeIndex = -1
            mItemList.forEachIndexed { index, recyclerItem ->
                if(item == recyclerItem) {
                    mItemList.remove(item)
                    removeIndex = index
                }
            }
            if(removeIndex >= 0) {
                if(::recyclerView.isInitialized)
                    recyclerView.post { notifyItemRemoved(removeIndex) }
            }
        }
        catch (e: Exception) {}
    }

    fun removeItemIndex(item: RecyclerItem): Int {
        if(mItemList.isNullOrEmpty()) return -1
        return try {
            var removeIndex = -1
            mItemList.forEachIndexed { index, recyclerItem ->
                if(item == recyclerItem) {
                    removeIndex = index
                }
            }
            removeIndex
        } catch (e: Exception) {
            -1
        }
    }

    fun removeItemAtPositionWithHeader(position: Int) {
        try {
            var pos = position
            if (isHeaderAdded)
                pos = position - 1
            if (mItemList.size > pos) {
                mItemList.removeAt(pos)
                if(::recyclerView.isInitialized)
                    recyclerView.post { notifyItemRemoved(position) }
            }
        }
        catch (e: Exception) {}
    }

    fun addItemAtPosition(click: AdapterClick?, position: Int) {
        try {
            if (mItemList.size >= position) {
                mItemList.add(position, click as RecyclerItem)
                if (isHeaderAdded)
                    notifyItemInserted(position + 1)
                else
                    notifyItemInserted(position)
            }
        }
        catch (e: Exception) {}
    }

    fun addListAtStart(items: List<RecyclerItem>) {
        try {
            mItemList.addAll(0, items)
            notifyItemRangeInserted(0, items.size)
        } catch (e: Exception) {}
    }

    fun postAndNotify(position: Int) {
        recyclerView.post {
            notifyItemChanged(position)
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun updateItem(position: Int, item: RecyclerItem) {
        try {
            if (mItemList.size > position) {
                mItemList[position] = item
                notifyItemChanged(position)
            }
        }
        catch (e: Exception) {}
    }

    fun notifyNestedItem(parentPosition: Int, position: Int) {
        try {
            if (itemCount > parentPosition) {
                if(::recyclerView.isInitialized) {
                    val viewHolder = recyclerView
                        .findViewHolderForAdapterPosition(parentPosition)

                    if (viewHolder is NestedBaseViewHolder)
                        viewHolder.adapter?.notifyItemChanged(position)
                }
            }
        } catch (e: Exception) {}
    }

    fun getViewHolderByPosition(position: Int): RecyclerView.ViewHolder? {
        try {
            if (itemCount > position) {
                return if(::recyclerView.isInitialized)
                    recyclerView.findViewHolderForAdapterPosition(position)
                else
                    null
            }
        }
        catch (e: Exception) {}
        return null
    }

    fun getActualPosition(position: Int): Int {
        return if (isHeaderAdded) (position - 1) else position
    }

    fun findAndNotifyItem(click: RecyclerItem) {
        val index = mItemList.indexOf(click)
        if (index > 0) {
            updateItem(index, click)
//            notifyItemChanged(mItemList.indexOf(click))
        }
    }

    fun findAndRemoveItem(click: RecyclerItem) {
        try {
            if (mItemList.contains(click)) {
                var position = mItemList.indexOf(click)
                mItemList.removeAt(position)

                if (isHeaderAdded) position += 1

                recyclerView.post {
                    notifyItemRemoved(position)
                }
            }
        }
        catch (e: Exception) {}
    }

    fun updateItem(click: RecyclerItem) {
        try {
            if (mItemList.contains(click)) {
                val position = mItemList.indexOf(click)

                if(::recyclerView.isInitialized)
                    recyclerView.post { notifyItemChanged(position) }
            }
        }
        catch (ignored: Exception) {}
    }
}