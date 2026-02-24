package com.example.read5.viewmodel.iteminfo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.read5.bean.ItemKey
import com.example.read5.global.GlobalSettings
import com.example.read5.repository.StoreHouseRepository
import com.example.read5.repository.iteminfo.ItemInfoRepository
import com.example.read5.viewmodel.storehouse.StoreHouseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateItemInfo @Inject constructor(
    private val itemInfoRepository: ItemInfoRepository,
    private val storeHouseRepository: StoreHouseRepository
) : ViewModel() {

    val TAG = "UpdateItemInfo"
     fun updateCollectStatus(id: Long, isCollect: Boolean) {
        viewModelScope.launch {
            itemInfoRepository.updateByCollect(id, isCollect)
        }
    }

    suspend fun updateCurrentPage(id: Long, currentPage: Int) {
        itemInfoRepository.updateByCurrentPage(id, currentPage)
    }

     fun updateByIsShow(id: Long, isShow: Boolean) {
         viewModelScope.launch(Dispatchers.IO) { // 👈 在协程中调用 suspend 函数
             val result = itemInfoRepository.updateByIsShow(id, isShow)
             Log.d(TAG, "updateByIsShow: $result")
         }
     }

    fun updateByCount(id: Long, addOrSub: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val storeHouse = storeHouseRepository.searchById(id)
                    val count = if(addOrSub) storeHouse.count + 1 else storeHouse.count - 1
                    GlobalSettings.setitemCount(count)
                    storeHouseRepository.updateByCount(id, count)
            } catch (e: Exception) {
                Log.e(TAG, "updateByCount error", e)
            }
        }
    }

}