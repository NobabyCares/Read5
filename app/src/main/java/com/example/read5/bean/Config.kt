package com.example.read5.bean

import kotlinx.serialization.Serializable


@Serializable
data class Config (
    var history: List<Long> = emptyList(),
    var recentStoreHouse: Long = 0L,

)