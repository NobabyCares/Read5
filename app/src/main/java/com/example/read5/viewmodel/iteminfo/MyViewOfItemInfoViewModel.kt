package com.example.read5.viewmodel.iteminfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.read5.bean.ItemInfo
import com.example.read5.global.GlobalSettings
import com.example.read5.repository.iteminfo.ItemInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface MyViewOfItemInfo {
    data object ISSHOW: MyViewOfItemInfo
    data object ISCOLLECT: MyViewOfItemInfo

}
@HiltViewModel
class MyViewOfItemInfoViewModel @Inject constructor(
    private val itemInfoRepository: ItemInfoRepository
): ViewModel(){

    private val _dataSource = MutableStateFlow<MyViewOfItemInfo>(
        MyViewOfItemInfo.ISSHOW
    )

    // 2. 使用 stateIn 转换为 StateFlow<PagingData<ItemInfo>>
    @OptIn(ExperimentalCoroutinesApi::class)
    val items: StateFlow<PagingData<ItemInfo>> = _dataSource
        .flatMapLatest { source ->
            when (source) {
                is MyViewOfItemInfo.ISSHOW ->{
                    itemInfoRepository.searchByIsShow().cachedIn(viewModelScope)  // ✅ 重要
                }

                is MyViewOfItemInfo.ISCOLLECT ->{
                    itemInfoRepository.searchByIsCollect().cachedIn(viewModelScope)  // ✅ 重要
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PagingData.empty()
        )


    fun searchByIsShow() {
        _dataSource.value = MyViewOfItemInfo.ISSHOW
    }

    fun searchByIsCollect() {
        _dataSource.value = MyViewOfItemInfo.ISCOLLECT
    }

}