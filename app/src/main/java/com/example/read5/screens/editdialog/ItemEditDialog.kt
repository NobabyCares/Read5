package com.example.read5.screens.editdialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.read5.bean.ItemInfo
import com.example.read5.bean.ItemKey
import com.example.read5.viewmodel.iteminfo.UpdateItemInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEditDialog(
    item: ItemInfo,
    onDismiss: () -> Unit = {}
) {
    val TAG = "ItemEditDialog"
    val updateItemInfoViewModel = hiltViewModel<UpdateItemInfo>()
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
                updateItemInfoViewModel.updateByIsShow(item.id, newIsShow)
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
    }
}