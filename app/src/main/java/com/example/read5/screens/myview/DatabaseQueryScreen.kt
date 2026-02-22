// screens/myview/DatabaseQueryScreen.kt
package com.example.read5.screens.myview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseQueryScreen(
    modifier: Modifier = Modifier,
    onItemClicked: (ItemInfo) -> Unit = {}
) {
    var dbPath by remember { mutableStateOf("/storage/emulated/0/Download/PC.db") }
    var keyword by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<ItemInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 🔁 提取查询逻辑为函数，避免重复
    fun performQuery() {
        if (dbPath.isBlank()) return
        val trimmedKeyword = keyword.trim().ifBlank { null } // ✅ 去除前后空白
        isLoading = true
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                DbHelper.queryItemsByName(dbPath, trimmedKeyword)
            }
            items = result
            isLoading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "按书名模糊查询",
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
            onValueChange = { keyword = it },
            label = { Text("书名关键词") },
            placeholder = { Text("例如：Rust、第1话、漫画名...") },
            singleLine = true, // ✅ 必须设为单行才能响应 IME
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search, // 显示“搜索”按钮
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onSearch = { performQuery() } // ✅ 回车/搜索键触发
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { performQuery() }, // ✅ 复用函数
            enabled = !isLoading && dbPath.isNotBlank(),
            modifier = Modifier.align(Alignment.End)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("执行查询")
            }
        }

        if (items.isEmpty()) {
            Text(
                text = if (isLoading) "查询中..." else "点击“执行查询”或按回车加载数据",
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(items) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClicked(item) }
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
                                    color = MaterialTheme.colorScheme.outline
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