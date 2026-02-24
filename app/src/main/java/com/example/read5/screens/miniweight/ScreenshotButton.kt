package com.example.read5.screens.miniweight



import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.example.read5.utils.ScreenshotUtils
import kotlinx.coroutines.launch

/**
 * 安全截图按钮（无废弃 API）
 */
@Composable
fun ScreenshotButton(
    modifier: Modifier = Modifier,
    onComplete: (success: Boolean, message: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }

    Button(
        onClick = {
            if (!isProcessing) {
                isProcessing = true
                scope.launch {
                    val (success, message) = ScreenshotUtils.tryCaptureScreenshot(context, view)
                    onComplete(success, message)
                    isProcessing = false
                }
            }
        },
        modifier = modifier,
        enabled = !isProcessing
    ) {
        if (isProcessing) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 8.dp),
                    strokeWidth = 2.dp
                )
                Text("处理中...")
            }
        } else {
            Text("截图")
        }
    }
}
