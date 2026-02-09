package com.example.read5.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeUitls {
    // 格式化时间
    fun formatUpdateTime(timestamp: Long): String {
        val date = Date(timestamp)
        val now = Date()
        val diff = now.time - timestamp

        return when {
            diff < 24 * 60 * 60 * 1000 -> "今天"
            diff < 2 * 24 * 60 * 60 * 1000 -> "昨天"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${(diff / (24 * 60 * 60 * 1000)).toInt()}天前"
            else -> SimpleDateFormat("MM/dd", Locale.getDefault()).format(date)
        }
    }
}