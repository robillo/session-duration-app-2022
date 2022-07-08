package com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.common_adapter

class CommonListAdapter(
    listener: AdapterListener,
    loaderType: AppEnums.LoaderType = AppEnums.LoaderType.NONE,
    vararg cell: Cell<RecyclerItem>
) :
    BaseListAdapter(
        *cell,
        listener = listener,
        loaderType = loaderType
    )