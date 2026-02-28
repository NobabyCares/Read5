// ComicTypeEditDialog.kt

package com.example.read5.screens.editdialog

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.read5.bean.ComicType
import com.example.read5.bean.ItemInfo
import com.example.read5.viewmodel.comictype.ComicTypeSearchViewModel

@Composable
fun ComicTypeEditDialog(
    item: ItemInfo,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier,
    comicTypeSearchViewModel: ComicTypeSearchViewModel = hiltViewModel()
) {
    val comicTypes by comicTypeSearchViewModel.allTypes.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    Log.d("ComicTypeDebug", "===== Dialog Recomposed =====")
    Log.d("ComicTypeDebug", "Item ID: ${item.id}, Item Name: ${item.name}")

    // 异步加载已选分类 ID（仅用于初始化）
    val initialSelectedIds by produceState<Set<Int>>(initialValue = emptySet()) {
        value = try {
            val ids = comicTypeSearchViewModel.getTypeIdByItemId(item.id).toSet()
            Log.d("ComicTypeDebug", "Initial selected IDs loaded: $ids")
            ids
        } catch (e: Exception) {
            Log.e("ComicTypeDebug", "Error loading initial IDs", e)
            e.printStackTrace()
            emptySet()
        }
    }

    // ✅ 使用 Set 来管理选中的 ID（支持多选）
    var selectedIds by remember { mutableStateOf(initialSelectedIds) }

    // 当初始数据加载完成后更新选中状态
    LaunchedEffect(initialSelectedIds) {
        Log.d("ComicTypeDebug", "LaunchedEffect - initialSelectedIds changed: $initialSelectedIds")
        Log.d("ComicTypeDebug", "LaunchedEffect - current selectedIds: $selectedIds")
        if (initialSelectedIds != selectedIds) {
            Log.d("ComicTypeDebug", "Updating selectedIds from $selectedIds to $initialSelectedIds")
            selectedIds = initialSelectedIds
        }
    }

    // ✅ 调试日志 - 监听 selectedIds 变化
    LaunchedEffect(selectedIds) {
        Log.d("ComicTypeDebug", "selectedIds CHANGED: $selectedIds")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // ===== 标题栏 =====
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("选择分类", style = MaterialTheme.typography.titleMedium)

                // 显示已选数量
                if (selectedIds.isNotEmpty()) {
                    Badge(
                        modifier = Modifier.padding(start = 8.dp),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = selectedIds.size.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Button(
                onClick = {
                    Log.d("ComicTypeDebug", "Create new category button clicked")
                    showCreateDialog = true
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("新建", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(Modifier.height(12.dp))

        // ===== 分类网格 =====
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
        ) {
            items(comicTypes) { type ->
                val isCurrentlySelected = type.id in selectedIds

                Log.d("ComicTypeDebug", "Rendering card - Type: ${type.name}, ID: ${type.id}, isSelected: $isCurrentlySelected")

                ComciTypeCardScreen(
                    comicType = type,
                    isSelected = isCurrentlySelected,
                    onSelectedChange = {
                        Log.d("ComicTypeDebug", "Card clicked - Type: ${type.name}, ID: ${type.id}")
                        Log.d("ComicTypeDebug", "Before click - selectedIds: $selectedIds")
                        Log.d("ComicTypeDebug", "isCurrentlySelected: $isCurrentlySelected")

                        // ✅ 点击时切换选中状态
                        selectedIds = if (isCurrentlySelected) {
                            // 如果当前是选中状态，则移除（取消选中）
                            Log.d("ComicTypeDebug", "REMOVING type ${type.id} from selectedIds")
                            selectedIds - type.id
                        } else {
                            // 如果当前是未选中状态，则添加（选中）
                            Log.d("ComicTypeDebug", "ADDING type ${type.id} to selectedIds")
                            selectedIds + type.id
                        }

                        Log.d("ComicTypeDebug", "After click - selectedIds: $selectedIds")
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ===== 底部按钮 =====
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = {
                    Log.d("ComicTypeDebug", "Cancel button clicked")
                    onDismiss()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("取消")
            }

            Button(
                onClick = {
                    Log.d("ComicTypeDebug", "Save button clicked")
                    Log.d("ComicTypeDebug", "Final selected IDs to save: $selectedIds")

                    // ✅ 将最终选中的所有 ID 保存
                    comicTypeSearchViewModel.updateComicTypeCover(selectedIds.toList(), item.hash)
                    comicTypeSearchViewModel.updateItemTypes(item.id, selectedIds.toList())

                    Log.d("ComicTypeDebug", "Save completed, dismissing dialog")
                    onDismiss()
                },
                enabled = true,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (selectedIds.isEmpty()) "清除所有分类" else "保存 (${selectedIds.size})")
            }
        }
    }

    // 新建分类弹窗
    if (showCreateDialog) {
        Log.d("ComicTypeDebug", "Showing create dialog")
        var newTypeName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = {
                Log.d("ComicTypeDebug", "Create dialog dismissed")
                showCreateDialog = false
            },
            title = { Text("新建分类") },
            text = {
                OutlinedTextField(
                    value = newTypeName,
                    onValueChange = {
                        newTypeName = it
                        Log.d("ComicTypeDebug", "New type name: $it")
                    },
                    label = { Text("分类名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTypeName.isNotBlank()) {
                            Log.d("ComicTypeDebug", "Creating new type: $newTypeName")
                            comicTypeSearchViewModel.insertType(ComicType(name = newTypeName))
                            showCreateDialog = false
                            newTypeName = ""
                        }
                    },
                    enabled = newTypeName.isNotBlank()
                ) {
                    Text("添加")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    Log.d("ComicTypeDebug", "Create dialog cancelled")
                    showCreateDialog = false
                }) {
                    Text("取消")
                }
            }
        )
    }
}