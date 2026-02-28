package com.example.read5.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.read5.global.SimplePasswordManager

@Composable
fun PasssWordSettingsScreen() {
    val context = LocalContext.current
    var showSetPasswordDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDisableDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    // 读取当前配置
    val config = remember { SimplePasswordManager.getConfig(context) }
    var isPasswordEnabled by remember { mutableStateOf(config.isEnabled) }
    var autoLockMinutes by remember { mutableStateOf(config.autoLockMinutes) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "安全设置",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 启用密码开关
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("启用应用锁")
                    Switch(
                        checked = isPasswordEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                // 如果当前是禁用状态，显示设置密码对话框
                                showSetPasswordDialog = true
                            } else {
                                // 如果当前是启用状态，显示确认禁用对话框
                                showDisableDialog = true
                            }
                        }
                    )
                }

                if (isPasswordEnabled) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // 密码管理区域
                    Text(
                        text = "密码管理",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // 修改密码按钮
                    Button(
                        onClick = { showChangePasswordDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("修改密码")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 重置为默认密码按钮
                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("重置为默认密码 (123)")
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // 自动锁定时间
                    Text(
                        text = "自动锁定设置",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("后台锁定时间")
                        Text(
                            text = "$autoLockMinutes 分钟",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = autoLockMinutes.toFloat(),
                        onValueChange = {
                            autoLockMinutes = it.toInt()
                            SimplePasswordManager.setAutoLockMinutes(context, it.toInt())
                        },
                        valueRange = 1f..30f,
                        steps = 28,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }

    // 设置密码对话框（首次启用）
    if (showSetPasswordDialog) {
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showSetPasswordDialog = false },
            title = { Text("设置密码") },
            text = {
                Column {
                    Text("请设置应用锁密码")
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            error = null
                        },
                        label = { Text("输入密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            error = null
                        },
                        label = { Text("确认密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (error != null) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        when {
                            password.isEmpty() || confirmPassword.isEmpty() -> {
                                error = "密码不能为空"
                            }
                            password != confirmPassword -> {
                                error = "两次输入的密码不一致"
                            }
                            else -> {
                                SimplePasswordManager.setPassword(context, password)
                                isPasswordEnabled = true
                                showSetPasswordDialog = false
                            }
                        }
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetPasswordDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 修改密码对话框
    if (showChangePasswordDialog) {
        var oldPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("修改密码") },
            text = {
                Column {
                    Text("请输入原密码和新密码")
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = {
                            oldPassword = it
                            error = null
                        },
                        label = { Text("原密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            error = null
                        },
                        label = { Text("新密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            error = null
                        },
                        label = { Text("确认新密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (error != null) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        when {
                            oldPassword.isEmpty() -> {
                                error = "请输入原密码"
                            }
                            !SimplePasswordManager.verifyPassword(context, oldPassword) -> {
                                error = "原密码错误"
                            }
                            newPassword.isEmpty() || confirmPassword.isEmpty() -> {
                                error = "新密码不能为空"
                            }
                            newPassword != confirmPassword -> {
                                error = "两次输入的新密码不一致"
                            }
                            else -> {
                                SimplePasswordManager.setPassword(context, newPassword)
                                showChangePasswordDialog = false
                            }
                        }
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 确认禁用密码对话框
    if (showDisableDialog) {
        var password by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showDisableDialog = false },
            title = { Text("禁用应用锁") },
            text = {
                Column {
                    Text("请输入密码以确认禁用应用锁")
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            error = null
                        },
                        label = { Text("密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (error != null) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (SimplePasswordManager.verifyPassword(context, password)) {
                            SimplePasswordManager.disablePassword(context)
                            isPasswordEnabled = false
                            showDisableDialog = false
                        } else {
                            error = "密码错误"
                        }
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisableDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 重置为默认密码对话框
    if (showResetDialog) {
        var password by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("重置为默认密码") },
            text = {
                Column {
                    Text("请输入当前密码以确认重置为默认密码 (123)")
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            error = null
                        },
                        label = { Text("当前密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (error != null) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (SimplePasswordManager.verifyPassword(context, password)) {
                            SimplePasswordManager.resetToDefaultPassword(context)
                            showResetDialog = false
                        } else {
                            error = "密码错误"
                        }
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}