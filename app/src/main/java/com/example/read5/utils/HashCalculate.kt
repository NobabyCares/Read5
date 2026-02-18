package com.example.read5.utils

import android.annotation.SuppressLint
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest

object HashCalculate {

    private const val DEFAULT_SAMPLE_SIZE = 1_000_000L // 1 MB

    /**
     * 计算文件的快速哈希：读取前 [sampleSize] 字节
     * 如果文件小于 sampleSize，则读取全部
     */
    @SuppressLint("NewApi")
    fun calculateContentBasedHash(file: File, sampleSize: Long = DEFAULT_SAMPLE_SIZE): String {
        if (!file.exists() || !file.isFile) {
            return fallbackHash(file)
        }

        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            var totalRead = 0L
            val buffer = ByteArray(8192) // 8KB buffer for efficiency

            Files.newInputStream(file.toPath()).use { input ->
                while (totalRead < sampleSize) {
                    val remaining = sampleSize - totalRead
                    val toRead = if (remaining > buffer.size) buffer.size else remaining.toInt()
                    val bytesRead = input.read(buffer, 0, toRead)
                    if (bytesRead == -1) break // EOF
                    digest.update(buffer, 0, bytesRead)
                    totalRead += bytesRead
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            fallbackHash(file)
        }
    }

    /**
     * Fallback: 使用文件大小 + 最后修改时间生成哈希
     */
    private fun fallbackHash(file: File): String {
        val fallbackBytes = "${file.length()}-${file.lastModified()}".toByteArray(Charsets.UTF_8)
        return MessageDigest.getInstance("SHA-256")
            .digest(fallbackBytes)
            .joinToString("") { "%02x".format(it) }
    }
}