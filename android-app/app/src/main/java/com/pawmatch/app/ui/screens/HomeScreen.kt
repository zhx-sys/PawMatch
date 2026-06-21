package com.pawmatch.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.pawmatch.app.data.api.ServerConfigManager
import com.pawmatch.app.data.model.MatchedPet
import com.pawmatch.app.data.model.Pet
import com.pawmatch.app.ui.theme.*
import com.pawmatch.app.viewmodel.PetViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPetClick: (Long) -> Unit,
    onNotificationsClick: () -> Unit,
    onMatchingProfile: () -> Unit,
    viewModel: PetViewModel = viewModel()
) {
    val pets by viewModel.pets.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(true) }
    var selectedSpecies by remember { mutableStateOf("") }
    var selectedSize by remember { mutableStateOf("") }
    var selectedAgeRange by remember { mutableStateOf("") }
    var selectedPersonality by remember { mutableStateOf("") }

    var selectedTab by remember { mutableIntStateOf(0) }
    val matchedPets by viewModel.matchedPets.collectAsState()
    val hasProfile by viewModel.hasProfile.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPets(refresh = true)
        viewModel.loadFavoriteIds()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PawMatch", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White),
                actions = {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(Icons.Default.Notifications, contentDescription = "通知", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // 搜索栏
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("搜索宠物...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                viewModel.searchPets(searchQuery)
                            }) { Icon(Icons.Default.Send, contentDescription = null) }
                        }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(Icons.Default.FilterList, contentDescription = "筛选")
                }
            }

            // 智能推荐 tab
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFFE67E22)
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("全部宠物", modifier = Modifier.padding(12.dp))
                }
                Tab(selected = selectedTab == 1, onClick = {
                    selectedTab = 1
                    viewModel.loadRecommendations()
                }) {
                    Text("智能推荐", modifier = Modifier.padding(12.dp))
                }
            }

            // 筛选栏
            if (showFilters && selectedTab == 0) {
                val doFilter: () -> Unit = {
                    viewModel.loadPets(
                        species = selectedSpecies.ifEmpty { null },
                        sizeLevel = selectedSize.ifEmpty { null },
                        activityLevel = selectedPersonality.ifEmpty { null },
                        minAge = when (selectedAgeRange) {
                            "young" -> 1; "adult" -> 3; "senior" -> 7; else -> null
                        },
                        maxAge = when (selectedAgeRange) {
                            "baby" -> 0; "young" -> 3; "adult" -> 7; else -> null
                        },
                        refresh = true
                    )
                }
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    Text("物种", style = MaterialTheme.typography.labelMedium)
                    Row {
                        listOf("" to "全部", "猫" to "猫", "狗" to "狗", "其他" to "其他").forEach { (value, label) ->
                            FilterChip(
                                selected = selectedSpecies == value,
                                onClick = { selectedSpecies = value; doFilter() },
                                label = { Text(label) },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("体型", style = MaterialTheme.typography.labelMedium)
                    Row {
                        listOf("" to "不限", "小型" to "小型", "中型" to "中型", "大型" to "大型").forEach { (value, label) ->
                            FilterChip(
                                selected = selectedSize == value,
                                onClick = { selectedSize = value; doFilter() },
                                label = { Text(label) },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("年龄", style = MaterialTheme.typography.labelMedium)
                    Row {
                        listOf(
                            "" to "不限", "baby" to "<1岁", "young" to "1~3岁",
                            "adult" to "3~7岁", "senior" to "7岁+"
                        ).forEach { (value, label) ->
                            FilterChip(
                                selected = selectedAgeRange == value,
                                onClick = { selectedAgeRange = value; doFilter() },
                                label = { Text(label, fontSize = 11.sp) },
                                modifier = Modifier.padding(end = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("性格", style = MaterialTheme.typography.labelMedium)
                    Row {
                        listOf(
                            "" to "不限", "活泼好动" to "活泼", "温顺安静" to "温顺",
                            "粘人精" to "粘人", "独立自主" to "独立"
                        ).forEach { (value, label) ->
                            FilterChip(
                                selected = selectedPersonality == value,
                                onClick = { selectedPersonality = value; doFilter() },
                                label = { Text(label, fontSize = 11.sp) },
                                modifier = Modifier.padding(end = 2.dp)
                            )
                        }
                    }
                }
            }

            // 内容区
            if (selectedTab == 0) {
                // 宠物网格
                if (isLoading && pets.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(pets) { pet ->
                            PetCard(
                                pet = pet,
                                isFavorite = favoriteIds.contains(pet.id),
                                onFavorite = { viewModel.toggleFavorite(pet.id) },
                                onClick = { onPetClick(pet.id) }
                            )
                        }
                    }
                }
            } else {
                // 智能推荐
                when {
                    hasProfile == null -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFFE67E22))
                        }
                    }
                    hasProfile == false -> {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("完善匹配画像", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFE67E22))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "填写您的居住空间、养宠经验、偏好等信息，系统将为您智能推荐最合适的毛孩子",
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onMatchingProfile,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE67E22))
                                ) {
                                    Text("填写匹配画像", color = Color.White)
                                }
                            }
                        }
                    }
                    matchedPets.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("暂无可匹配宠物", fontSize = 16.sp, color = TextSecondary)
                        }
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(matchedPets) { item ->
                                MatchedPetCard(item = item, onPetClick = onPetClick)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchedPetCard(item: MatchedPet, onPetClick: (Long) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp).clickable { onPetClick(item.id) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = matchedPetFirstImage(item),
                contentDescription = null,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name ?: "未知", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(
                    buildString {
                        append(item.type ?: "")
                        if (!item.breed.isNullOrBlank()) {
                            if (isNotEmpty()) append(" · ")
                            append(item.breed)
                        }
                        if (!item.gender.isNullOrBlank()) {
                            if (isNotEmpty()) append(" · ")
                            append(item.gender)
                        }
                    }.ifBlank { null } ?: "",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                matchedPetMatchReason(item)?.let { reason ->
                    Text(reason, fontSize = 12.sp, color = Color(0xFFE67E22), maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFE67E22).copy(alpha = 0.1f)
            ) {
                Text(
                    "${item.matchScore.roundToInt()}%",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = Color(0xFFE67E22),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

private fun matchedPetFirstImage(item: MatchedPet): Any? {
    val raw = item.images?.trim() ?: return null
    if (raw.isEmpty()) return null
    val firstUrl = if (raw.startsWith("[")) {
        raw.trim('[', ']', '"', ' ')
            .split(",")
            .firstOrNull { it.isNotBlank() }
            ?.trim('"', ' ')
    } else {
        raw.split(",").firstOrNull { it.isNotBlank() }?.trim()
    } ?: return null
    val baseUrl = ServerConfigManager.serverUrl.removeSuffix("/api/").removeSuffix("/api")
    return "$baseUrl$firstUrl"
}

private fun matchedPetMatchReason(item: MatchedPet): String? {
    val details = item.matchDetails ?: return null
    if (details.isEmpty()) return null
    val topKey = details.maxByOrNull { it.value }?.key ?: return null
    return when (topKey) {
        "breed" -> "品种偏好高度匹配"
        "size" -> "体型要求高度匹配"
        "activity" -> "活动量需求高度匹配"
        "experience" -> "经验要求高度匹配"
        "budget" -> "预算匹配度高"
        "schedule" -> "作息习惯匹配"
        "personality" -> "性格匹配度最高"
        else -> "综合匹配度最高"
    }
}

@Composable
fun PetCard(pet: Pet, isFavorite: Boolean, onFavorite: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // 图片
            Box {
                AsyncImage(
                    model = pet.firstImageUrl(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(140.dp).clip(MaterialTheme.shapes.small),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = onFavorite,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = if (isFavorite) FavoriteRed else Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                // 健康状态圆点
                val healthColor = when (pet.healthStatus) {
                    "健康" -> HealthGreen
                    "亚健康" -> HealthYellow
                    "生病" -> HealthRed
                    else -> Color.Transparent
                }
                if (healthColor != Color.Transparent) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(4.dp),
                        shape = MaterialTheme.shapes.small,
                        color = healthColor.copy(alpha = 0.85f)
                    ) {
                        Text(
                            pet.healthStatus ?: "",
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            // 信息
            Column(modifier = Modifier.padding(8.dp)) {
                Text(pet.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${pet.species} · ${pet.breed}", fontSize = 11.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${pet.age}岁 · ${pet.gender}", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.weight(1f))
                    pet.sizeLevel?.let {
                        AssistChip(
                            onClick = {},
                            label = { Text(it, fontSize = 10.sp) },
                            modifier = Modifier.height(20.dp)
                        )
                    }
                }
            }
        }
    }
}

fun Pet.firstImageUrl(): String? {
    val raw = imageUrls?.trim() ?: return null
    if (raw.isEmpty()) return null
    val firstUrl = if (raw.startsWith("[")) {
        raw.trim('[', ']', '"', ' ')
            .split(",")
            .firstOrNull { it.isNotBlank() }
            ?.trim('"', ' ')
    } else {
        raw.split(",").firstOrNull { it.isNotBlank() }?.trim()
    } ?: return null
    val baseUrl = ServerConfigManager.serverUrl.removeSuffix("/api/").removeSuffix("/api")
    return "$baseUrl$firstUrl"
}
