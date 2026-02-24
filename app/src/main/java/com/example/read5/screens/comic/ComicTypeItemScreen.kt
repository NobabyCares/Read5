package com.example.read5.screens.comic

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.read5.bean.ComicType
import com.example.read5.screens.CenteredText

@Composable
fun ComicTypeItemScreen(
    navController: NavHostController,
    comicType: ComicType,
) {
    val TAG = "ComicTypeScreen"

    Row {
        CenteredText(text = comicType.name)
    }
}