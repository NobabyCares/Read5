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
import com.example.read5.viewmodel.ImportFileViewModel
import kotlinx.coroutines.launch
//导入细节
@Composable
fun StoreHouseInputDialog(
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val importViewModel: ImportFileViewModel = hiltViewModel()
    // ✅ 用 coroutineScope 启动后台任务
    val scope = rememberCoroutineScope()


    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    // ✅ 正确：带状态、带类型、能触发重组
    var content by remember { mutableStateOf<List<String>>(emptyList()) }

    // ✅ 控制加载状态
    var isImporting by remember { mutableStateOf(false) }


    AlertDialog(
        onDismissRequest = {
            if (!isImporting) {
                onDismiss()
            }
        },
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
                            content = content + ""
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isImporting
                    ) {
                        Text("添加目录")
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
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isImporting
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
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isImporting

                )

                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("类型（如：pdf,epub,mobi,awz3）") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isImporting
                )

                // 动态 TextField 列表
                for (i in content.indices) {
                    OutlinedTextField(
                        value = content[i],
                        onValueChange = { newText ->
                            // ✅ 更新第 i 项
                            content = content.toMutableList().apply { this[i] = newText }.toList()
                        },
                        label = { Text("目录 ${i + 1}") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isImporting
                    )
                }

                // ✅ 可选：在底部加一行加载提示（更明显）
                if (isImporting) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("正在导入书库...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && !isImporting) {
                        isImporting = true
                        scope.launch {
                            try {
                                importViewModel.importStoreHouse(context, name, type, content)
                            } catch (e: Exception) {
                                // 可选：记录日志或后续加 Toast
                            } finally {
                                isImporting = false
                                onDismiss()
                            }
                        }
                    }
                },
                enabled = name.isNotBlank()
            ){
                if (isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("导入")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}


