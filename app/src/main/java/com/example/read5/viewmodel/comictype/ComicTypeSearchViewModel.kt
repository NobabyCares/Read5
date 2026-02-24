package com.example.read5.viewmodel.comictype

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.read5.bean.ComicType
import com.example.read5.repository.comictype.ComicTypeRepositoryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ComicTypeSearchViewModel @Inject constructor(
    private val repository: ComicTypeRepositoryApi
): ViewModel() {
    private var _dataSource = MutableStateFlow<ComicTypeDataSource>(
        ComicTypeDataSource.GetAll
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val items: StateFlow<PagingData<ComicType>> = _dataSource
        .flatMapLatest { source ->
            when (source) {
                is ComicTypeDataSource.GetAll -> {
                    repository.getAll().cachedIn(viewModelScope)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PagingData.empty()
    )

    fun getAll(){
        _dataSource.value = ComicTypeDataSource.GetAll
    }


}