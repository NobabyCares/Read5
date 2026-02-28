package com.example.read5.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.read5.bean.ItemInfo
import com.example.read5.global.DeviceIdentification
import com.example.read5.global.GlobalSettings
import com.example.read5.utils.HashCalculate.calculateContentBasedHash
import java.io.File
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Locale

object FileScanner {
    val TAG = "FileScanner"

    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

//    总路径
    private const val ROOT_PATH = "storage/emulated/0/"
//图片类型
    private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "bmp", "gif")

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadFile(extensions: Set<String>, category: Long, content: List<String>): List<ItemInfo>{
        var result = mutableListOf<ItemInfo>()
        if(extensions.isNotEmpty()){
             result = findFileByExtensions(extensions, category).toMutableList()
        }
        for (itemInfo in content){
            val temp = findComicBooksByFolder(path = itemInfo, category = category)
            result = result.plus(temp).toMutableList()
        }
        return result
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun findFileByExtensions(
        extensions: Set<String> = setOf("epub", "mobi", "pdf", "azw", "azw3"),
        category: Long,
        path : String = ROOT_PATH
    ): List<ItemInfo> {
        val result = mutableListOf<ItemInfo>()
        val queue = ArrayDeque<File>()
        val root = File(path)

        if(!root.exists() || !root.isDirectory) return  result
        root.listFiles()?.forEach { queue.add(it) }

        while(queue.isNotEmpty()){
            val current  = queue.removeFirst()
            if(current.isDirectory){
                current.listFiles()?.forEach {
                    queue.add(it)
                }
            }else{
                if(isFileExtension(current.name, extensions)){
                    val hash = calculateContentBasedHash(current)
                    val itemInfo = ItemInfo(
                        name = current.name,
                        baseCode = Base64.getEncoder().encodeToString(current.name.toByteArray()),
                        path = current.absolutePath,
                        hash = hash,
                        androidId = DeviceIdentification.androidId,
                        createTime = current.lastModified(),
                        fileSize = current.length(),
                        fileType = current.name.substringAfterLast('.', "").lowercase(),
                        category = category,
                    )
                    result.add(itemInfo)
                }
            }
        }
        return result
    }

//    通过文件夹查找漫画文件
@RequiresApi(Build.VERSION_CODES.O)
fun findComicBooksByFolder(path: String, category: Long): List<ItemInfo>{
        val result = mutableListOf<ItemInfo>()
        val root = File(path)

//    如果文件夹本身就是漫画文件夹,就不要玩后面遍历, 因为只有一个
        if(root.exists() && root.isDirectory){
            val item = analyzeFolder(root, category)
            if(item != null){
                result.add(item)
                return result
            }
        }

//    开始循环遍历出来
        val queue = ArrayDeque<File>()
        root.listFiles()?.forEach { queue.add(it) }

        while (queue.isNotEmpty()){
            val item = queue.removeFirst()
            if(item.isDirectory){
                val item = analyzeFolder(item, category)
                if(item != null){
                    result.add(item)
                }
            }
        }
        return result
    }




//    截取后缀
    private fun isFileExtension(fileName: String, extensions: Set<String> = IMAGE_EXTENSIONS): Boolean {
        val tempExtension =  fileName.substringAfterLast('.', "").lowercase()
        return extensions.contains(tempExtension)
    }

    // 分析单个文件夹的函数
    @RequiresApi(Build.VERSION_CODES.O)
    private fun analyzeFolder(folder: File, category: Long): ItemInfo? {
        val files = folder.listFiles() ?: return null
        if (files.isEmpty()) return null // 空文件夹不算

        var hasImage = false
        var nonImageFileFound = false

        for (file in files) {
            if (file.isDirectory) {
                nonImageFileFound = true
                break
            }

            val isImage = isFileExtension(file.name) // 你的扩展名判断函数
            if (isImage) {
                hasImage = true
            } else {
                // ⚡ 关键优化：发现非图片，立刻标记并退出！
                nonImageFileFound = true
                break // 不再检查后续文件
            }
        }

        // 第二步：符合条件，现在找“按名称排序的第一个图片文件”来计算 hash
        val firstImageFile = files
            .sortedBy { it.name.lowercase() } // 按文件名排序（忽略大小写）
            .firstOrNull() ?: return null // 理论上不会为 null，但安全起见


        // 判定：必须有图片，且没有非图片文件
        if (hasImage && !nonImageFileFound) {
            val hash = calculateContentBasedHash(firstImageFile)
            return  ItemInfo(
                name = folder.name,
                baseCode = Base64.getEncoder().encodeToString(folder.name.toByteArray()),
                path = folder.absolutePath,
                hash = hash,
                androidId = DeviceIdentification.androidId,
                createTime = folder.lastModified(),
                fileSize = folder.length(),
                fileType = "folder",
                category = category,
            )
        }
        return null
    }


}


