package com.example.read5.screens.myview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.read5.bean.ItemInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.Job

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseQueryScreen(
    modifier: Modifier = Modifier,
    onItemClicked: (ItemInfo) -> Unit = {},
    debounceTimeMs: Long = 500 // 防抖时间，避免频繁查询
) {
    var dbPath by remember { mutableStateOf("/storage/emulated/0/Download/PC.db") }
    var keyword by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<ItemInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 创建搜索关键词的 StateFlow
    val searchQuery = remember { MutableStateFlow("") }

    // 监听搜索关键词变化，自动执行查询
    LaunchedEffect(searchQuery) {
        searchQuery
            .debounce(debounceTimeMs) // 防抖，避免每次按键都查询
            .collectLatest { query ->
                if (dbPath.isNotBlank()) {
                    isLoading = true
                    val trimmedQuery = query.trim().ifBlank { null }
                    val result = withContext(Dispatchers.IO) {
                        DbHelper.queryItemsByName(dbPath, trimmedQuery)
                    }
                    items = result
                    isLoading = false
                }
            }
    }

    // 当 dbPath 变化时，重新查询
    LaunchedEffect(dbPath) {
        if (dbPath.isNotBlank() && keyword.isNotBlank()) {
            searchQuery.value = keyword // 触发重新查询
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "按书名模糊查询（自动更新）",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = dbPath,
            onValueChange = { dbPath = it },
            label = { Text("数据库路径 (.db)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = keyword,
            onValueChange = {
                keyword = it
                searchQuery.value = it // 触发自动查询
            },
            label = { Text("书名关键词") },
            placeholder = { Text("例如：Rust、第1话、漫画名...") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onDone = { /* 键盘关闭时不需要额外操作 */ }
            ),
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                if (keyword.isNotBlank()) {
                    IconButton(onClick = {
                        keyword = ""
                        searchQuery.value = ""
                    }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Clear,
                            contentDescription = "清空"
                        )
                    }
                }
            }
        )

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (items.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (keyword.isBlank())
                        "输入关键词开始搜索"
                    else
                        "未找到匹配的记录",
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else if (items.isNotEmpty()) {
            // 显示结果数量
            Text(
                text = "找到 ${items.size} 条记录",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(items) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClicked(item) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = item.path,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = item.hash.take(8) + "...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}