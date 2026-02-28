package com.example.read5.screens

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.read5.screens.bottombar.BottomBarScreen
import com.example.read5.screens.route.MainNavGraph
import com.example.read5.screens.topbar.TopBarContent
import com.example.read5.viewmodel.iteminfo.UpdateItemInfo
import com.example.read5.viewmodel.storehouse.StoreHouseViewModel






// ——————— App 入口 ———————
@Composable
fun MainBookApp () {
    val TAG = "MainBookApp"
    val navHostController = rememberNavController()
    MainNavGraph(navHostController,
        "bookshelf/bookdesk",
       )
}



