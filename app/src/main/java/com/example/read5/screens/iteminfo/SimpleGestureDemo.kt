package com.example.read5.screens.iteminfo

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier

@Composable
fun SimpleGestureDemo() {
    val TAG = "SimpleGestureDemo"

    var message by remember { mutableStateOf("Tap me") }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var scale by remember { mutableStateOf(1f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val firstDown = awaitFirstDown()
                    do {
                        val pointerEvent = awaitPointerEvent()

                        val changeList = pointerEvent.changes
                        Log.d(TAG, "EachGesture changeList size: ${changeList.size}")
                        when (changeList.size) {
                            // 单指
                            1 -> {
                                val singleChange = changeList[0]
                                val position = singleChange.position
                                Log.d(TAG, "EachGesture Single Point: $position")
                                firstDown.consume()
                            }
                        }
                    } while (!firstDown.isConsumed)
                }
            }

    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Color.LightGray)
            drawCircle(
                color = Color.Blue,
                radius = 100f * scale,
                center = Offset(size.width / 2 + offsetX, size.height / 2 + offsetY)
            )
        }
        Text(text = message, modifier = Modifier.align(androidx.compose.ui.Alignment.TopStart))
    }
}

