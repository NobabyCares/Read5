// ManagerEditDialog.kt

package com.example.read5.screens.editdialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.read5.bean.ItemInfo
import com.example.read5.viewmodel.iteminfo.UpdateItemInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerEditDialog(
    item: ItemInfo,
    onDismiss: () -> Unit
) {
    val updateItemInfoViewModel: UpdateItemInfo = hiltViewModel()
    var showCreateDialog by remember { mutableStateOf(false) }

    // 假设从 ViewModel 获取分类列表，这里先用模拟数据
    val comicTypes = remember {
        mutableStateListOf(
            "热血", "搞笑", "恋爱", "奇幻", "科幻",
            "悬疑", "恐怖", "日常", "励志", "战斗"
        )
    }

    // 当前项目已选中的分类 - 使用 Set 来存储选中的分类
    val selectedTypes by remember { mutableStateOf<String>("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        // 使用 Column 来组织内容
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // ===== 项目信息卡片 =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = item.name ?: "未命名",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "作者: ${item.author ?: "未知"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    // 显示当前状态
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(if (item.isShow) "已显示" else "已隐藏")
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== 隐藏/显示按钮 =====
            Button(
                onClick = {
                    val newIsShow = !item.isShow
                    val key = com.example.read5.bean.ItemKey(
                        path = item.path,
                        hash = item.hash,
                        androidId = item.androidId
                    )
                    updateItemInfoViewModel.updateByIsShow(key, newIsShow)
                    updateItemInfoViewModel.updateByCount(item.id, newIsShow)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (item.isShow)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (item.isShow) "隐藏项目" else "取消隐藏")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== 分类管理标题和新增按钮在同一行 =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "选择分类",
                    style = MaterialTheme.typography.titleMedium
                )

                // 新增分类按钮（改为小按钮）
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "新增分类",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("新建")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ===== 分类网格（三列） =====
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp), // 限制最大高度
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(comicTypes) { type ->
                    val isSelected = selectedTypes.contains(type)
                    CategoryCard(
                        type = type,
                        isSelected = isSelected,
                        count = 12, // 示例数量
                        onSelectedChange = { checked ->
                            if (checked) {
                            } else {
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== 底部确认按钮 =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消")
                }

                Button(
                    onClick = {
                        // 保存选中的分类
                        // TODO: 调用保存方法
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("保存")
                }
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
                            comicTypes.add(0, newTypeName) // 添加到列表开头
                            showCreateDialog = false
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

// 分类卡片组件
@Composable
fun CategoryCard(
    type: String,
    isSelected: Boolean,
    count: Int,
    onSelectedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectedChange(!isSelected) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 复选框在顶部
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectedChange,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 分类名称
            Text(
                text = type,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 数量徽章
            Badge(
                containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary
                },
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

// 辅助函数：创建一个可变的 Set State
@Composable
fun <T> rememberMutableStateSetOf(): MutableState<Set<T>> {
    return remember { mutableStateOf(setOf()) }
}