package com.example.read5.screens.storehouse

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.read5.bean.ItemKey
import com.example.read5.bean.StoreHouse
import com.example.read5.viewmodel.iteminfo.UpdateItemInfo
import com.example.read5.viewmodel.storehouse.StoreHouseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreHouseMoreInfoDialog(
    storeHouse: StoreHouse,
    onDismiss: () -> Unit
) {
    val storeHouseViewModel: StoreHouseViewModel = hiltViewModel()
    //主键
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Button(
                onClick = {
                    storeHouseViewModel.deleteStoreHouse(storeHouse.id) // ✅ 放在 onClick 里！
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("删除仓库") // 或其他 UI 内容
            }
        }
    }
}
