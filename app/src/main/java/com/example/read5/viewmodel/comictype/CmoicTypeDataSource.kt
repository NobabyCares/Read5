package com.example.read5.viewmodel.comictype

sealed interface ComicTypeDataSource {
    data object GetAll: ComicTypeDataSource

}