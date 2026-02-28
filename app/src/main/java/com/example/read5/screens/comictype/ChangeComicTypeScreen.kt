package com.example.read5.screens.comictype

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.read5.bean.ComicType
import com.example.read5.viewmodel.comictype.ComicTypeSearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeComicTypeScreen(
    comicType: ComicType,
    onDismiss: () -> Unit = {}
) {
    val TAG = "ChangeComicTypeScreen"
    val comicTypeSearchViewModel = hiltViewModel<ComicTypeSearchViewModel>()
    val sheetState = rememberModalBottomSheetState()

    // 编辑状态
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(comicType.name) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        // 使用 Column 来组织内容
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (!isEditing) {
                // ===== 查看模式 =====
                // 项目信息卡片
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
                            text = comicType.name,
                            style = MaterialTheme.typography.titleMedium
                        )

                        // 显示类型名称
                        Text(
                            text = "类型名称: ${comicType.name}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ===== 编辑按钮 =====
                Button(
                    onClick = {
                        isEditing = true
                        editedName = comicType.name
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("编辑类型")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ===== 删除按钮 =====
                Button(
                    onClick = {
                        comicTypeSearchViewModel.deleteById(comicType.id)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除类型")
                }
            } else {
                // ===== 编辑模式 =====
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
                            text = "编辑类型",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // 编辑输入框
                        OutlinedTextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            label = { Text("类型名称") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ===== 保存按钮 =====
                Button(
                    onClick = {
                        // 更新类型名称
                        if (editedName.isNotBlank() && editedName != comicType.name) {
                            comicTypeSearchViewModel.updateComicTypeName(comicType.id, editedName)
                        }
                        isEditing = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("保存修改")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ===== 取消按钮 =====
                Button(
                    onClick = {
                        isEditing = false
                        editedName = comicType.name
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("取消")
                }
            }
        }
    }
}