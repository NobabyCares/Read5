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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        ),
        modifier = Modifier.fillMaxWidth()
    ) {

        ItemEditDialog(item = item, onDismiss = { onDismiss() })

        Spacer(modifier = Modifier.height(16.dp))

        ComicTypeEditDialog(item = item, onDismiss = { onDismiss() })


    }


}