package com.example.read5.utils.comic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.positionChange
import kotlin.math.hypot

object GestureUitils {
    // --- 工具函数（不变）---
    private fun Offset.getDistance(): Float = hypot(x, y)

    fun calculatePan(changes: List<PointerInputChange>): Offset {
        return if (changes.size == 1) {
            changes[0].positionChange()
        } else {
            centroid(changes.map { it.position }) - centroid(changes.map { it.previousPosition })
        }
    }

    fun calculateZoom(changes: List<PointerInputChange>): Float {
        if (changes.size < 2) return 1f
        val curr = distance(changes[0].position, changes[1].position)
        val prev = distance(changes[0].previousPosition, changes[1].previousPosition)
        return if (prev > 0) curr / prev else 1f
    }

    private fun centroid(points: List<Offset>): Offset {
        if (points.isEmpty()) return Offset.Zero
        val x = points.sumOf { it.x.toDouble() }.toFloat()
        val y = points.sumOf { it.y.toDouble() }.toFloat()
        return Offset(x / points.size, y / points.size)
    }

    private fun distance(a: Offset, b: Offset): Float = hypot(b.x - a.x, b.y - a.y)

}