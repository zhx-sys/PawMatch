package com.pawmatch.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pawmatch.app.viewmodel.ShelterProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelterProfileScreen(shelterId: Long, onBack: () -> Unit, onStoryClick: (Long) -> Unit) {
    val vm: ShelterProfileViewModel = viewModel()
    val shelterInfo by vm.shelterInfo.collectAsState()
    val stats by vm.stats.collectAsState()
    val stories by vm.stories.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()

    LaunchedEffect(shelterId) { vm.loadProfile(shelterId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(shelterInfo?.nickname ?: "救助站主页") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFFDF8F0)
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFE67E22))
            }
            return@Scaffold
        }
        error?.let {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(it, color = Color.Red)
            }
            return@Scaffold
        }

        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())
        ) {
            // 名片区
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = MaterialTheme.shapes.medium) {
                Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    val initial = shelterInfo?.nickname?.firstOrNull()?.toString() ?: "S"
                    Box(
                        Modifier.size(72.dp)
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFFE67E22), Color(0xFFF39C12)),
                                        start = Offset.Zero,
                                        end = Offset.Infinite
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initial, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(20.dp))
                    Column {
                        Text(shelterInfo?.nickname ?: "救助站", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(onClick = {}, label = { Text("救助站") })
                            if (stats.successRate >= 0.8) AssistChip(onClick = {}, label = { Text("高成功率") })
                            if (stats.totalAdopted >= 10) AssistChip(onClick = {}, label = { Text("经验丰富") })
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 数据卡片行
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("成功领养", "${stats.totalAdopted}", Modifier.weight(1f))
                StatCard("在住宠物", "${stats.currentPets}", Modifier.weight(1f))
                StatCard("成功率", "${String.format("%.1f", stats.successRate * 100)}%", Modifier.weight(1f))
                StatCard("平均响应", "${stats.avgResponseHours}h", Modifier.weight(1f))
            }

            // 领养故事
            if (stories.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text("领养故事", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF333333))
                Spacer(Modifier.height(12.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    stories.forEach { story ->
                        Card(
                            Modifier.width(200.dp).clickable { onStoryClick(story.id) },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(story.title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF333333))
                                Spacer(Modifier.height(6.dp))
                                Text("浏览 ${story.viewCount} · ${story.createTime}", fontSize = 12.sp, color = Color(0xFFBBBBBB))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            TextButton(onClick = { /* navigate to shelter ranking */ }) {
                Text("查看救助站排行榜", color = Color(0xFFE67E22))
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(Modifier.padding(vertical = 16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE67E22))
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 13.sp, color = Color.Gray)
        }
    }
}
