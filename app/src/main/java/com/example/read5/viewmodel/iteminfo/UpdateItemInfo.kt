package com.example.read5.viewmodel.iteminfo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.read5.bean.ItemKey
import com.example.read5.repository.iteminfo.ItemInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateItemInfo @Inject constructor(
    private val itemInfoRepository: ItemInfoRepository
) : ViewModel() {

    val TAG = "UpdateItemInfo"
     fun updateCollectStatus(key: ItemKey, isCollect: Boolean) {
        viewModelScope.launch {
            itemInfoRepository.updateByCollect(key, isCollect)
        }
    }

    suspend fun updateCurrentPage(key: ItemKey, currentPage: Int) {
        itemInfoRepository.updateByCurrentPage(key, currentPage)
    }

     fun updateByIsShow(key: ItemKey, isShow: Boolean) {
         viewModelScope.launch { // 👈 在协程中调用 suspend 函数
             val result = itemInfoRepository.updateByIsShow(key, isShow)
             Log.d(TAG, "updateByIsShow: $result")
         }
     }

}