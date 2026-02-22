package com.example.read5.screens.sortbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.read5.viewmodel.iteminfo.SearchItemInfo
import kotlinx.coroutines.flow.MutableStateFlow


@Composable
fun SortBarScreen(
    modifier: Modifier = Modifier,
    currentSortType: SortOption,      // 当前选中的字段
    isAscending: Boolean,               // 当前排序方向（true=升序，false=降序）
    onSortChanged: (SortOption, Boolean) -> Unit, // 字段或方向变化时回调
) {
    var expanded by remember { mutableStateOf(false) }

    // 生成按钮文字：字段标签 + 方向箭头
    val buttonLabel = buildString {
        append(currentSortType.label)
        append(if (isAscending) " ↑" else " ↓")
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "排序: $buttonLabel",
                style = MaterialTheme.typography.labelMedium
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            SortOption.all.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option.label,
                                modifier = Modifier.weight(1f)
                            )
                            // 如果是当前字段，显示方向图标
                            if (option == currentSortType) {
                                Icon(
                                    imageVector = if (isAscending) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        }
                    },
                    onClick = {
                        expanded = false
                        if (option == currentSortType) {
                            // 同一字段：切换方向
                            onSortChanged(option, !isAscending)
                        } else {
                            // 不同字段：默认使用升序（可根据需要改为上次记忆的方向）
                            onSortChanged(option, true)
                        }
                    }
                )
            }
        }
    }
}