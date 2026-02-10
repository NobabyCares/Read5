package com.example.read5.viewmodel.comic

import com.example.read5.bean.VirtualCanvas

sealed interface ViewportEvent {
    data class Scroll(val offsetY: Float, val currentCanvasHeight: Int) : ViewportEvent
}
