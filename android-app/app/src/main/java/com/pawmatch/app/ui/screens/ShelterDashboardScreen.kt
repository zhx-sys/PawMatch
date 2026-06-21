package com.pawmatch.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import coil.compose.AsyncImage
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pawmatch.app.data.api.TokenManager
import com.pawmatch.app.data.model.Pet
import com.pawmatch.app.ui.theme.*
import com.pawmatch.app.viewmodel.PetViewModel
import com.pawmatch.app.viewmodel.ShelterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelterDashboardScreen(
    onPetManagement: () -> Unit,
    onAdoptionReview: () -> Unit,
    onFosterManagement: () -> Unit,
    onPostReview: () -> Unit,
    onReportReview: () -> Unit = {},
    onWikiReview: () -> Unit,
    onFollowups: () -> Unit = {},
    onNotifications: () -> Unit,
    onLogout: () -> Unit,
    shelterViewModel: ShelterViewModel = viewModel(),
    petViewModel: PetViewModel = viewModel()
) {
    val shelterProfile by shelterViewModel.shelterProfile.collectAsState()
    val shelterPets by shelterViewModel.shelterPets.collectAsState()
    val shelterName = shelterProfile?.shelterInfo?.nickname ?: "救助站${TokenManager.userId}"

    LaunchedEffect(Unit) {
        shelterViewModel.loadShelterProfile()
        shelterViewModel.loadShelterPets()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("救助站管理中心") },
                actions = {
                    IconButton(onClick = onNotifications) {
                        Icon(Icons.Default.Notifications, contentDescription = "通知")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 救助站名片
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(listOf(Accent, Color(0xFFF0A04B)))
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            shelterName.take(1),
                                            color = Color.White,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(shelterName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Text("救助站", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem("在住宠物", shelterPets.size.toString())
                                StatItem("等待审核", "-")
                                StatItem("今日访客", "-")
                            }
                        }
                    }
                }
            }

            // 快捷功能入口
            item {
                Text("快捷管理", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShelterActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Pets,
                        title = "宠物管理",
                        subtitle = "发布/编辑/下架",
                        onClick = onPetManagement
                    )
                    ShelterActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.FactCheck,
                        title = "领养审核",
                        subtitle = "审批领养申请",
                        onClick = onAdoptionReview
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShelterActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Home,
                        title = "寄养管理",
                        subtitle = "服务与订单",
                        onClick = onFosterManagement
                    )
                    ShelterActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Forum,
                        title = "帖子审核",
                        subtitle = "社区内容管控",
                        onClick = onPostReview
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShelterActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.MenuBook,
                        title = "百科审核",
                        subtitle = "知识词条审核",
                        onClick = onWikiReview
                    )
                    ShelterActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Flag,
                        title = "举报审核",
                        subtitle = "用户举报处理",
                        onClick = onReportReview
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShelterActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.RateReview,
                        title = "回访记录",
                        subtitle = "查看领养回访",
                        onClick = onFollowups
                    )
                }
            }

            // 退出登录
            item {
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Error)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("退出登录")
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
    }
}

@Composable
private fun ShelterActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = Accent, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
            Text(
                subtitle,
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PetQuickCard(pet: Pet) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Pets, contentDescription = null, tint = Accent, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(pet.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(
                    "${pet.species} · ${pet.breed} · ${pet.age}岁",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            AssistChip(
                onClick = {},
                label = {
                    Text(
                        if (pet.status == 0) "待领养" else "已领养",
                        fontSize = 11.sp
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (pet.status == 0)
                        HealthYellow.copy(alpha = 0.15f)
                    else
                        HealthGreen.copy(alpha = 0.1f)
                )
            )
        }
    }
}