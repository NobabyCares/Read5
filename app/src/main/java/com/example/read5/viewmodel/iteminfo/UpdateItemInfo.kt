package com.example.read5.viewmodel.iteminfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.read5.bean.ItemKey
import com.example.read5.repository.ItemInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateItemInfo @Inject constructor(
    private val itemInfoRepository: ItemInfoRepository
) : ViewModel() {


     fun updateCollectStatus(key: ItemKey, isCollect: Boolean) {
        viewModelScope.launch {
            itemInfoRepository.updateCollectStatus(key, isCollect)
        }
    }

    suspend fun updateCurrentPage(key: ItemKey, currentPage: Int) {
        itemInfoRepository.updateCurrentPage(key, currentPage)
    }



}