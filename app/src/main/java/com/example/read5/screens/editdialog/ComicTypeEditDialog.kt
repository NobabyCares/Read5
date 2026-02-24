// ComicTypeEditDialog.kt （改造后）

package com.example.read5.screens.editdialog

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
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.read5.bean.ComicType
import com.example.read5.viewmodel.comictype.ComicTypeSearchViewModel

// ✅ 新增：可复用的分类选择器（无弹窗外壳）

@Composable
fun ComicTypeEditDialog(
    itemId: Long,
    onDismiss: () -> Unit = {},
    onSave: (List<Int>) -> Unit = {},
    modifier: Modifier = Modifier,
    comicTypeSearchviewModel: ComicTypeSearchViewModel = hiltViewModel()
) {
    val comicTypes by comicTypeSearchviewModel.allTypes.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    // 异步加载已选分类 ID（仅用于初始化）
    val initialSelectedIds by produceState<Set<Int>>(initialValue = emptySet()) {
        value = try {
            comicTypeSearchviewModel.getTypeIdByItemId(itemId).toSet()
        } catch (e: Exception) {
            e.printStackTrace()
            emptySet()
        }
    }

    // 构建选择状态
    val selectionStates = remember(comicTypes, initialSelectedIds) {
        comicTypes.associateBy(
            keySelector = { it.id },
            valueTransform = { type ->
                mutableStateOf(type.id in initialSelectedIds)
            }
        )
    }

    // 只在 comicTypes 未加载时显示 loading
    if (comicTypes.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // ✅ 核心：不再用 weight，让内容自然流动
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
            Text("选择分类", style = MaterialTheme.typography.titleMedium)

            Button(
                onClick = { showCreateDialog = true },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("新建", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(Modifier.height(12.dp))

        // ===== 分类网格（自动高度，可滚动）=====
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp) // 可选：限制最大高度，避免太长
        ) {
            items(comicTypes) { type ->
                selectionStates[type.id]?.let { state ->
                    ComciTypeCardScreen(
                        name = type.name,
                        isSelected = state.value,
                        count = 0,
                        onSelectedChange = { newValue -> state.value = newValue }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ===== 底部按钮 =====
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("取消")
            }

            val selectedIds = selectionStates
                .filter { it.value.value }
                .keys
                .toList()

            Button(
                onClick = {
                    comicTypeSearchviewModel.insertItemToTypes(itemId,selectedIds)
                    onDismiss()
                },
                enabled = selectedIds.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Text("保存")
            }
        }
    }

    // 新建分类弹窗
    if (showCreateDialog) {
        var newTypeName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("新建分类") },
            text = {
                OutlinedTextField(
                    value = newTypeName,
                    onValueChange = { newTypeName = it },
                    label = { Text("分类名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTypeName.isNotBlank()) {
                            comicTypeSearchviewModel.insertType(ComicType(name = newTypeName))
                            onDismiss()
                        }
                    },
                    enabled = newTypeName.isNotBlank()
                ) {
                    Text("添加")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
