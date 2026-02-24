package com.example.read5.screens.comic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.read5.screens.CenteredText
import com.example.read5.screens.iteminfo.ItemInfoScreen
import com.example.read5.singledata.DocumentHolder
import com.example.read5.viewmodel.comictype.ComicTypeSearchViewModel

@Composable
fun ComicTypeScreen(
    navController: NavHostController,
) {
    val TAG = "ComicTypeScreen"
    val comicTypeSearchViewModel: ComicTypeSearchViewModel = hiltViewModel()
    val comicTypes = comicTypeSearchViewModel.items.collectAsLazyPagingItems()



    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 96.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(comicTypes.itemCount){index ->
                    comicTypes[index]?.let {
                        ComicTypeItemScreen(
                            navController = navController,
                            comicType = it,)
                    }
                }
            }
        }
    }
}