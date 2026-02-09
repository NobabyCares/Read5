package com.example.read5.utils

import java.io.File
import java.io.RandomAccessFile
import java.security.MessageDigest

object HashCalculate {

    private const val SAMPLE_SIZE = 4096 // 每块采样 4KB

    fun calculateContentBasedHash(file: File): String {
        if (!file.exists() || !file.isFile) {
            return fallbackHash(file)
        }

        return try {
            val fileSize = file.length()
            val digest = MessageDigest.getInstance("SHA-256")

            // 使用 RandomAccessFile 支持高效随机读取
            RandomAccessFile(file, "r").use { raf ->
                // 定义采样位置（去重 + 排序）
                val positions = setOf(
                    0L, // 开头
                    (fileSize * 0.25).toLong(),
                    (fileSize * 0.5).toLong(),
                    (fileSize * 0.75).toLong(),
                    (fileSize - SAMPLE_SIZE).coerceAtLeast(0L) // 结尾前 4KB
                ).sorted()

                for (pos in positions) {
                    if (pos >= fileSize) continue
                    raf.seek(pos)
                    val toRead = SAMPLE_SIZE.coerceAtMost((fileSize - pos).toInt())
                    val buffer = ByteArray(toRead)
                    raf.readFully(buffer) // 确保读满
                    digest.update(buffer)
                }
            }

            // 加入文件大小，进一步降低碰撞概率
            digest.update(fileSize.toString().toByteArray(Charsets.UTF_8))

            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            fallbackHash(file)
        }
    }

    private fun fallbackHash(file: File): String {
        val fallbackBytes = "${file.length()}-${file.lastModified()}".toByteArray(Charsets.UTF_8)
        return MessageDigest.getInstance("SHA-256")
            .digest(fallbackBytes)
            .joinToString("") { "%02x".format(it) }
    }
}