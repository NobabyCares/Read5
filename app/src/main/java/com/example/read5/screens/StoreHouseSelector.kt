package com.example.read5.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.read5.bean.StoreHouse
import com.example.read5.viewmodel.iteminfo.GetBaseItemInfoViewModel
//仓库选择组件
@Composable
fun StoreHouseSelector(
    storeHouses: List<StoreHouse>,
    itemInfoViewModel: GetBaseItemInfoViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(storeHouses, key = { it.id }) { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable {
                        itemInfoViewModel.setCategory(item.id)
                        itemInfoViewModel.toggleType(item.id)
                    }
                    .padding(8.dp)
                    .wrapContentWidth()
            ) {
                Text(
                    text = item.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // 只有当前项展开才显示 type
                if (itemInfoViewModel.expandedStoreId.value == item.id) {
                    val tags = (item.type ?: "")
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    if (tags.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            tags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            itemInfoViewModel.setFileTypeFilter(tag)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(text = tag)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}