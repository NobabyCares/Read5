// 文件路径: app/src/main/java/com/example/read4/screens/StoreHouseInputDialog.kt

package com.example.read5.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.read5.utils.hasAllFilesPermission
import com.example.read5.utils.requestAllFilesPermission
import com.example.read5.viewmodel.ImportViewModel
import kotlinx.coroutines.launch

@Composable
fun StoreHouseInputDialog(
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val importViewModel: ImportViewModel = hiltViewModel()
    // ✅ 用 coroutineScope 启动后台任务
    val scope = rememberCoroutineScope()


    var uriPath: Uri? by remember { mutableStateOf(null) }
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }


    // SAF 目录选择器
    // ✅ 正确方式：使用 OpenDocumentTree contract（无需手动加 flags！）
    val directoryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                // 👇 关键：持久化权限（这里才加 flags！）
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                try {
                    context.contentResolver.takePersistableUriPermission(uri, flags)
                    uriPath = uri
                } catch (e: SecurityException) {
                    // 权限被拒绝（理论上不该发生，但防御性编程）
                    e.printStackTrace()
                }
            }
        }
    )


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加书库") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // === 操作按钮区域 ===
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            directoryPicker.launch(null) // 弹出系统目录选择器
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("选择目录")
                    }

                    OutlinedButton(
                        onClick = {
                            // 根据 Android 版本决定权限策略
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                                // Android 11+：跳转到“所有文件访问”设置页
                                if (!hasAllFilesPermission(context)) {
                                    requestAllFilesPermission(context)
                                } else {
                                    // 已有权限，可直接扫描文件
                                }
                            } else {

                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("授予权限")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // === 输入框 ===
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("书库名称") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("类型（如：pdf,epub,mobi,awz3）") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        scope.launch {
                            // 🚀 在后台执行导入
                            importViewModel.importStoreHouse(context, name, type, uriPath)
                            // 导入完成后显示 Snackbar
//                            scaffoldState.snackbarHostState.showSnackbar("书库导入完成！")
                            onDismiss()
                        }
                    }
                },
                enabled = name.isNotBlank()
            ){
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}