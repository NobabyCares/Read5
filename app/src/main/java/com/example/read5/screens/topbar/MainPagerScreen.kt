// MainPagerScreen.kt
package com.example.read5.screens.topbar

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.read5.global.GlobalSettings
import com.example.read5.screens.BookShelfScreen
import com.example.read5.viewmodel.iteminfo.SearchItemInfo
import com.example.read5.viewmodel.storehouse.StoreHouseViewModel

