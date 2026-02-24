package com.example.read5.screens.iteminfo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
fun ItemMoreInfoDialog(
    item: ItemInfo,
    onDismiss: () -> Unit = {}
) {
    val updatedItem: UpdateItemInfo = hiltViewModel()

    //主键
    val key = ItemKey(path = item.path, hash = item.hash, androidId =  item.androidId)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "${item.path}")
            Button(
                onClick = {
                    val temp = !item.isShow
                    updatedItem.updateByIsShow(item.id, temp) // ✅ 放在 onClick 里！
                    updatedItem.updateByCount(item.id, temp)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("隐藏") // 或其他 UI 内容
            }
        }
    }
}