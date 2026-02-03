package com.example.read5.viewmodel.storehouse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.read5.bean.StoreHouse
import com.example.read5.repository.StoreHouseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class StoreHouseViewModel @Inject constructor(
    private val storeHouseRepository: StoreHouseRepository
): ViewModel () {

    val storeHouses = storeHouseRepository.getAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun insert(item: StoreHouse): Long {
        var id: Long = -1;
        viewModelScope.launch {
            id = storeHouseRepository.insert(item)
            // ✅ 插入后，上面的 storeHouses 会自动更新！
        }
        return id
    }


}