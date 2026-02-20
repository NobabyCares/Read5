package com.example.read5.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.read5.global.SimplePasswordManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SimplePasswordScreen(
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var verificationJob by remember { mutableStateOf<Job?>(null) }

    // 获取正确的密码
    val correctPassword = remember {
        SimplePasswordManager.getConfig(context).password
    }
    val correctPasswordLength = correctPassword.length

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 图标
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "请输入密码",
            fontSize = 20.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )

        // 显示密码位数指示器（用圆点表示）
        Row(
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(correctPasswordLength) { index ->
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .padding(horizontal = 4.dp)
                        .background(
                            color = if (index < password.length)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                )
            }
        }

        // 密码输入框（隐藏实际输入）
        OutlinedTextField(
            value = password,
            onValueChange = { newPassword ->
                // 限制输入长度不超过正确密码长度
                if (newPassword.length <= correctPasswordLength) {
                    password = newPassword
                    error = null

                    // 当输入长度达到正确密码长度时，自动验证
                    if (newPassword.length == correctPasswordLength) {
                        verificationJob?.cancel()
                        verificationJob = coroutineScope.launch {
                            delay(50)
                            if (SimplePasswordManager.verifyPassword(context, newPassword)) {
                                SimplePasswordManager.recordUnlock(context)
                                onSuccess()
                            } else {
                                error = "密码错误"
                                // 密码错误时清空输入
                                password = ""
                            }
                        }
                    }
                }
            },
            label = { Text("点击输入密码") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            isError = error != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onCancel) {
            Text("退出应用")
        }
    }
}