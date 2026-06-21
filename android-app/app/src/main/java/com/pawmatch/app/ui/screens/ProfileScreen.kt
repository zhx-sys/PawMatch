package com.pawmatch.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pawmatch.app.ui.theme.*
import com.pawmatch.app.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onFavorites: () -> Unit = {},
    onMyAdoptions: () -> Unit = {},
    onFoster: () -> Unit = {},
    onMatchingProfile: () -> Unit = {},
    onFollowups: () -> Unit = {},
    onGrowth: () -> Unit = {},
    onCreditScore: () -> Unit = {},
    onSettings: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    val userInfo by viewModel.userInfo.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 头像
            Surface(
                color = Primary,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        userInfo?.nickname?.take(1) ?: "U",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(userInfo?.nickname ?: "用户", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Text(userInfo?.email ?: "", fontSize = 13.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(24.dp))

            // 功能菜单
            ProfileMenuItem(icon = Icons.Default.Favorite, title = "我的收藏", onClick = onFavorites)
            ProfileMenuItem(icon = Icons.Default.Pets, title = "我的领养", onClick = onMyAdoptions)
            ProfileMenuItem(icon = Icons.Default.Home, title = "预约寄养", onClick = onFoster)
            ProfileMenuItem(icon = Icons.Default.PersonSearch, title = "匹配画像", onClick = onMatchingProfile)
            ProfileMenuItem(icon = Icons.Default.RateReview, title = "领养回访", onClick = onFollowups)
            ProfileMenuItem(icon = Icons.Default.EmojiEvents, title = "成长激励", onClick = onGrowth)
            ProfileMenuItem(icon = Icons.Default.Security, title = "信用分", onClick = onCreditScore)
            ProfileMenuItem(icon = Icons.Default.Settings, title = "设置", onClick = onSettings)

            Spacer(modifier = Modifier.weight(1f))

            // 退出登录
            OutlinedButton(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Error)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("退出登录")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileMenuItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Text(title, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
}
