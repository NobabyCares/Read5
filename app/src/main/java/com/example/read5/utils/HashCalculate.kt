package com.example.read5.utils

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

object HashCalculate {

    private const val DEFAULT_SAMPLE_SIZE = 1_000_000L // 1 MB
    private const val BUFFER_SIZE = 8192 // 8 KB

    /**
     * 仅基于文件的前 1MB 二进制内容计算 SHA-256 哈希。
     * - 如果文件不存在或无法读取，返回固定的错误哈希 "0000000000000000000000000000000000000000000000000000000000000000"。
     * - 不依赖文件大小、修改时间、路径等任何外部因素。
     */
    fun calculateContentBasedHash(file: File): String {
        if (!file.exists() || !file.isFile) {
            return "0".repeat(64) // 64个0，代表SHA-256全零哈希
        }

        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            var totalBytesRead = 0L

            FileInputStream(file).use { input ->
                val buffer = ByteArray(BUFFER_SIZE)
                while (totalBytesRead < DEFAULT_SAMPLE_SIZE) {
                    val bytesToRead = minOf(BUFFER_SIZE.toLong(), DEFAULT_SAMPLE_SIZE - totalBytesRead).toInt()
                    val bytesRead = input.read(buffer, 0, bytesToRead)
                    if (bytesRead == -1) break // 文件已读完

                    digest.update(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                }
            }

            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            // 任何IO异常都返回固定错误哈希
            "0".repeat(64)
        }
    }
}