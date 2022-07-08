package com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.common_adapter

import android.util.Log

class CellTypes<T>(vararg types: Cell<T>) {

    private val cellTypes: ArrayList<Cell<T>> = ArrayList()

    init {
        types.forEach { addType(it) }
    }

    fun addType(type: Cell<T>) {
        cellTypes.add(type)
    }

    fun of(item: T?): Cell<T>? {
        for (cellType in cellTypes) {
            if (cellType.belongsTo(item)) return cellType
        }
        Log.d("mytag", "NoSuchRecyclerItemTypeException")
        return null
    }

    fun destroy() {
        for (cellType in cellTypes) {
            cellType.destroy()
        }
    }

    fun of(viewType: Int): Cell<T>? {
        for (cellType in cellTypes) {
            if (cellType.type() == viewType) return cellType
        }
        Log.d("mytag", "NoSuchRecyclerItemTypeException")
        return null
    }

}