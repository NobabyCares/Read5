package com.example.read5.screens.myview

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.read5.R // 如果你有图标资源，可替换；否则用默认图标
import com.example.read5.bean.Feature
import com.example.read5.bean.ItemInfo
import com.example.read5.screens.auth.PasssWordSettingsScreen
import com.example.read5.screens.editdialog.ManagerEditDialog
import com.example.read5.viewmodel.iteminfo.MyViewOfItemInfoViewModel


@Composable
fun MyViewScreen(
    navHostController: NavHostController,
    displayMode: String = "",
    modifier: Modifier = Modifier,
) {

    val TAG = "MyViewScreen"
    val myViewOfItemInfoViewModel: MyViewOfItemInfoViewModel = hiltViewModel()




    when(displayMode){
        "home" -> {
            HomeScreen(navHostController, myViewOfItemInfoViewModel)
        }
        "item_not_show" -> {
            myViewOfItemInfoViewModel.searchByIsShow()
            ShowScreen(
                navHostController,

                myViewOfItemInfoViewModel
            )
        }
        "is_collect_item" -> {
            myViewOfItemInfoViewModel.searchByIsCollect()
            ShowScreen(navHostController, myViewOfItemInfoViewModel)
        }
        "all_data_search" -> {
            DatabaseQueryScreen()
        }
        "simple_password" -> {
            PasssWordSettingsScreen()
        }
    }



}