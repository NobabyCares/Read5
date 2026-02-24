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
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.read5.viewmodel.comictype.ComicTypeSearchViewModel

// ✅ 新增：可复用的分类选择器（无弹窗外壳）
@Composable
fun ComicTypeSelector(
    onAddNewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val comicTypeSearchViewModel: ComicTypeSearchViewModel = hiltViewModel()
    val comicTypes = comicTypeSearchViewModel.items.collectAsLazyPagingItems()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // "+ 新建" 项
        item {
            AddNewComicTypeItem(onClick = onAddNewClick)
        }

        // 已有分类
        items(comicTypes.itemCount) { index ->
            comicTypes[index]?.let { type ->
                ComicTypeItem(name = type.name)
            }
        }
    }
}

// "+ 新建分类" 项（不变）
@Composable
fun AddNewComicTypeItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "新建分类",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

// 已有分类项（不变）
@Composable
fun ComicTypeItem(
    name: String,
    modifier: Modifier = Modifier
) {
    var checked by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { checked = !checked }
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1
        )
    }
}