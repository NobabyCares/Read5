package com.example.read5.screens.topbar

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.read5.global.GlobalSettings
import com.example.read5.screens.storehouse.StoreHouseInputDialog
import com.example.read5.viewmodel.iteminfo.SearchItemInfo
import com.example.read5.viewmodel.storehouse.StoreHouseViewModel

/*
* 这里有个小问题,就是在阅读返回后跳转页面的UI更新不对,
* 比如从历史记录返回,那高亮的就是历史记录,但是是书卓,
* 先不解决,无上大雅
* */
@Composable
fun TopBarContent(navController: NavController,
        searchItemInfo: SearchItemInfo,
        displayMode: String,
        onModeChange: (String) -> Unit // 新增
) {
    // 用于控制菜单展开状态
    var expanded by remember { mutableStateOf(false) }
    //用于控制搜索框的展开状态
    var isSearchExpanded by remember { mutableStateOf(false) }


    val tabs = listOf("书桌", "历史记录", "书架") // ✅ 推荐
    //选择标签高亮
    var selectedTab by remember { mutableStateOf(0) }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(isSearchExpanded){
            // ✅ 放在 BookShelfScreen 或 TopBar 的 Composable 内部
            BackHandler(enabled = isSearchExpanded) {
                // 退出搜索模式
                isSearchExpanded = false
                // 恢复默认分类（可选）
                searchItemInfo.searchByCategory(GlobalSettings.getRecentStoreHouse())
            }
            // 搜索框 + 背景遮罩（可选）
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SearchBarScreen(
                    searchItemInfo = searchItemInfo,
                    onDismiss = {
                        isSearchExpanded = false
                    },
                )
            }
        }else{
            // 左侧标签
            // 当前选中的 tab 索引（可选，用于高亮）
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                tabs.forEachIndexed { index, tabName ->
                    Text(
                        text = tabName,
                        modifier = Modifier
                            .clickable {
                                // 👇 点击逻辑：你可以用 index 或 tabName
                                selectedTab = index
                                when (tabName){
                                    "历史记录" -> {
                                        onModeChange("history")
                                    }
                                    "书桌" -> onModeChange("bookdesk")
                                    "书架" -> onModeChange("bookshelf")
                                }

                            }
                            .padding(vertical = 8.dp), // 可选：增加点击区域
                        style = if (index == selectedTab) {
                            MaterialTheme.typography.titleMedium // 高亮样式
                        } else {
                            MaterialTheme.typography.bodyLarge
                        }
                    )
                }
            }
            // 自动撑开
            Spacer(modifier = Modifier.weight(1f))

            // 右侧更多按钮
            IconButton(onClick = { isSearchExpanded = true }) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "搜索"
                )
            }
            Box {
                // 右侧更多按钮
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "更多"
                    )
                }

                // 下拉菜单
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {

                    DropdownMenuItem(
                        text = { Text("选项 2") },
                        onClick = {
                            // 处理选项 2 的点击事件
                            expanded = false
                        }
                    )
                }
            }
        }



    }
}



