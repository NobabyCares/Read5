package com.example.read5.screens.storehouse

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.read5.bean.StoreHouse
import com.example.read5.viewmodel.ImportFileViewModel
import com.example.read5.viewmodel.storehouse.StoreHouseViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreHouseMoreInfoDialog(
    storeHouse: StoreHouse,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val storeHouseViewModel: StoreHouseViewModel = hiltViewModel()
    val importFileViewModel: ImportFileViewModel = hiltViewModel()

    // 加载状态
    var isUpdating by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    // 错误提示状态
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // 显示错误Toast
    if (showError) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        showError = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 更新仓库按钮
            Button(
                onClick = {
                    isUpdating = true
                    scope.launch {
                        try {
                            importFileViewModel.updateStoreHouse(context, storeHouse)
                            // 更新成功提示
                            Toast.makeText(context, "仓库更新成功", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } catch (e: Exception) {
                            errorMessage = "更新失败: ${e.message}"
                            showError = true
                            isUpdating = false
                        }
                    }
                },
                enabled = !isUpdating && !isDeleting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("更新仓库")
                }
            }

            // 删除仓库按钮
            Button(
                onClick = {
                    isDeleting = true
                    scope.launch {
                        try {
                            storeHouseViewModel.deleteStoreHouse(storeHouse.id)
                            Toast.makeText(context, "仓库删除成功", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } catch (e: Exception) {
                            errorMessage = "删除失败: ${e.message}"
                            showError = true
                            isDeleting = false
                        }
                    }
                },
                enabled = !isUpdating && !isDeleting,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("删除仓库")
                }
            }
        }
    }
}