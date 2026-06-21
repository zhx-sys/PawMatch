package com.pawmatch.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.pawmatch.app.data.api.ServerConfigManager
import com.pawmatch.app.ui.theme.*
import com.pawmatch.app.viewmodel.PetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(
    petId: Long,
    onBack: () -> Unit,
    onContactShelter: (Long, String) -> Unit,
    viewModel: PetViewModel = viewModel()
) {
    val petDetail by viewModel.selectedPet.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val adoptionResult by viewModel.adoptionResult.collectAsState()

    var showAdoptDialog by remember { mutableStateOf(false) }
    var adoptReason by remember { mutableStateOf("") }
    var adoptExperience by remember { mutableStateOf("") }
    var adoptHousing by remember { mutableStateOf("") }
    var adoptConfirmFlood by remember { mutableStateOf(false) }

    LaunchedEffect(petId) {
        viewModel.loadPetDetail(petId)
        viewModel.loadFavoriteIds()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(petDetail?.name ?: "宠物详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
                },
                actions = {
                    petDetail?.let {
                        IconButton(onClick = { viewModel.toggleFavorite(it.id) }) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = if (favoriteIds.contains(it.id)) FavoriteRed else Color.Gray
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        petDetail?.let { pet ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // 图片轮播
                val images = pet.imageUrls?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
                if (images.isNotEmpty()) {
                    LazyRow(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                        items(images.size) { index ->
                            val baseUrl = ServerConfigManager.serverUrl.removeSuffix("/api/").removeSuffix("/api")
                            AsyncImage(
                                model = "$baseUrl${images[index]}",
                                contentDescription = null,
                                modifier = Modifier.fillMaxHeight().width(360.dp).clip(MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(pet.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        pet.healthStatus?.let { status ->
                            val (bgColor, text) = when (status) {
                                "健康" -> HealthGreen to "健康"
                                "亚健康" -> HealthYellow to "亚健康"
                                "生病" -> HealthRed to "生病"
                                else -> Color.Gray to status
                            }
                            Surface(color = bgColor, shape = MaterialTheme.shapes.small) {
                                Text(text, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row {
                        InfoChip("${pet.species} · ${pet.breed}")
                        pet.gender.let { InfoChip(it) }
                        pet.age.let { InfoChip("${it}岁") }
                        pet.sizeLevel?.let { InfoChip(it) }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    pet.activityLevel?.let {
                        Row {
                            InfoChip(
                                when (it) {
                                    "活泼好动" -> "活泼好动"
                                    "温顺安静" -> "温顺安静"
                                    "粘人精" -> "粘人精"
                                    "独立自主" -> "独立自主"
                                    else -> it
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("关于我", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(pet.description ?: "暂无描述", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

                    // 匹配画像
                    val hasProfile = pet.livingSpace != null || pet.experience != null || pet.schedule != null
                    if (hasProfile) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("匹配画像", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            pet.livingSpace?.let { ProfileTag("居住空间: $it") }
                            pet.experience?.let { ProfileTag("经验: $it") }
                            pet.schedule?.let { ProfileTag("作息: $it") }
                        }
                    }

                    // 救助站信息
                    pet.shelterName?.let { shelterName ->
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Accent,
                                shape = MaterialTheme.shapes.extraLarge,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(shelterName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(shelterName, fontWeight = FontWeight.SemiBold)
                                pet.shelterInfo?.address?.let { Text(it, fontSize = 12.sp, color = TextSecondary) }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        petDetail?.shelterId?.let { shelterId ->
                            OutlinedButton(
                                onClick = { onContactShelter(shelterId, pet.shelterName ?: "救助站") },
                                modifier = Modifier.weight(1f)
                            ) { Icon(Icons.Default.Chat, contentDescription = null); Spacer(Modifier.width(4.dp)); Text("联系救助站") }
                            Button(
                                onClick = { showAdoptDialog = true },
                                modifier = Modifier.weight(1f)
                            ) { Text("申请领养") }
                        }
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    // 领养申请对话框
    if (showAdoptDialog) {
        AlertDialog(
            onDismissRequest = {
                showAdoptDialog = false
                adoptReason = ""; adoptExperience = ""; adoptHousing = ""; adoptConfirmFlood = false
            },
            title = { Text("申请领养") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = adoptReason,
                        onValueChange = { adoptReason = it },
                        label = { Text("申请理由") },
                        placeholder = { Text("为什么想领养这只宠物") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        maxLines = 3
                    )
                    OutlinedTextField(
                        value = adoptExperience,
                        onValueChange = { adoptExperience = it },
                        label = { Text("养宠经验") },
                        placeholder = { Text("是否有过养宠经历") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        maxLines = 3
                    )
                    OutlinedTextField(
                        value = adoptHousing,
                        onValueChange = { adoptHousing = it },
                        label = { Text("住房条件") },
                        placeholder = { Text("是否适合养宠，如住房面积/有无阳台等") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        maxLines = 3
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = adoptConfirmFlood,
                            onCheckedChange = { adoptConfirmFlood = it }
                        )
                        Text(
                            "我已了解24小时内申请超过3次将扣除信用分",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    petDetail?.let {
                        viewModel.applyAdoption(petId, adoptReason, adoptExperience, adoptHousing, adoptConfirmFlood)
                        showAdoptDialog = false
                        adoptReason = ""; adoptExperience = ""; adoptHousing = ""; adoptConfirmFlood = false
                    }
                }) { Text("提交") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAdoptDialog = false
                    adoptReason = ""; adoptExperience = ""; adoptHousing = ""; adoptConfirmFlood = false
                }) { Text("取消") }
            }
        )
    }

    // 领养结果提示
    adoptionResult?.let { result ->
        LaunchedEffect(result) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearAdoptionResult()
        }
        AlertDialog(
            onDismissRequest = { viewModel.clearAdoptionResult() },
            title = { Text("提示") },
            text = { Text(result) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearAdoptionResult() }) { Text("知道了") }
            }
        )
    }
}

@Composable
fun InfoChip(text: String) {
    AssistChip(
        onClick = {},
        label = { Text(text, fontSize = 11.sp) },
        modifier = Modifier.height(26.dp).padding(end = 4.dp)
    )
}

@Composable
fun ProfileTag(text: String) {
    Surface(color = PrimaryLight, shape = MaterialTheme.shapes.small) {
        Text(text, fontSize = 11.sp, color = PrimaryDark, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}
