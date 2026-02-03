package com.example.read5.utils

import java.io.File
import java.security.MessageDigest

object CalculateHash {
    //    前后4kb
    private const val SAMPLE_SIZE = 4096 // 4KB

    //计算hash
    fun calculateContentBasedHash(file: File): String {
        return try {
            val fileSize = file.length()
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(SAMPLE_SIZE)
                var totalRead = 0
                while (totalRead < SAMPLE_SIZE) {
                    val toRead = SAMPLE_SIZE - totalRead
                    val bytesRead = input.read(buffer, 0, toRead)
                    if (bytesRead == -1) break
                    digest.update(buffer, 0, bytesRead)
                    totalRead += bytesRead
                }
                if (fileSize > SAMPLE_SIZE * 2L) {
                    input.skip(fileSize - SAMPLE_SIZE * 2L - totalRead.toLong())
                    var readFromEnd = 0
                    while (readFromEnd < SAMPLE_SIZE) {
                        val toRead = SAMPLE_SIZE - readFromEnd
                        val bytesRead = input.read(buffer, 0, toRead)
                        if (bytesRead == -1) break
                        digest.update(buffer, 0, bytesRead)
                        readFromEnd += bytesRead
                    }
                }
                digest.update(fileSize.toString().toByteArray(Charsets.UTF_8))
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            val fallbackBytes = "${file.length()}-${file.lastModified()}".toByteArray(Charsets.UTF_8)
            MessageDigest.getInstance("SHA-256").digest(fallbackBytes)
                .joinToString("") { "%02x".format(it) }
        }
    }
}