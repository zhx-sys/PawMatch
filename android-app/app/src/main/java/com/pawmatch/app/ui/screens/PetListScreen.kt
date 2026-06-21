package com.pawmatch.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pawmatch.app.data.model.Pet
import com.pawmatch.app.viewmodel.PetListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetListScreen(onBack: () -> Unit, onPetClick: (Long) -> Unit) {
    val vm: PetListViewModel = viewModel()
    val pets by vm.pets.collectAsState()
    val favoriteIds by vm.favoriteIds.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()

    var typeFilter by remember { mutableStateOf("") }
    var ageFilter by remember { mutableStateOf("") }
    var sizeFilter by remember { mutableStateOf("") }
    var personalityFilter by remember { mutableStateOf("") }
    var keyword by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadFavoriteIds(); vm.loadPets(refresh = true) }

    fun applyFilters() {
        vm.loadPets(type = typeFilter.ifBlank { null },
            ageRange = ageFilter.ifBlank { null },
            sizeLevel = sizeFilter.ifBlank { null },
            activityLevel = personalityFilter.ifBlank { null },
            keyword = keyword.ifBlank { null },
            refresh = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("宠物列表") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFFDF8F0)
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // 筛选栏
            Card(Modifier.fillMaxWidth().padding(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = MaterialTheme.shapes.medium) {
                Column(Modifier.padding(12.dp)) {
                    // 搜索框
                    OutlinedTextField(value = keyword, onValueChange = { keyword = it },
                        placeholder = { Text("搜索宠物...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.large
                    )
                    Spacer(Modifier.height(8.dp))
                    // 类型
                    FilterRow("种类", listOf("" to "全部", "狗" to "狗狗", "猫" to "猫咪", "其他" to "其他"), typeFilter) { typeFilter = it; applyFilters() }
                    Spacer(Modifier.height(4.dp))
                    // 年龄
                    FilterRow("年龄", listOf("" to "全部", "baby" to "幼年<1岁", "young" to "青年1-3岁", "adult" to "成年3-7岁", "senior" to "老年>7岁"), ageFilter) { ageFilter = it; applyFilters() }
                    Spacer(Modifier.height(4.dp))
                    // 体型
                    FilterRow("体型", listOf("" to "全部", "小型" to "小型", "中型" to "中型", "大型" to "大型"), sizeFilter) { sizeFilter = it; applyFilters() }
                    Spacer(Modifier.height(4.dp))
                    // 性格
                    FilterRow("性格", listOf("" to "全部", "活泼好动" to "活泼好动", "温顺安静" to "温顺安静", "粘人" to "粘人", "独立自主" to "独立自主"), personalityFilter) { personalityFilter = it; applyFilters() }
                }
            }

            if (isLoading && pets.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFFE67E22)) }
                return@Column
            }

            error?.let {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(it, color = Color.Red) }
                return@Column
            }

            // 宠物网格
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(pets, key = { it.id }) { pet ->
                    PetGridCard(pet = pet, isFavorited = pet.id in favoriteIds,
                        onToggleFavorite = { vm.toggleFavorite(pet.id) },
                        onClick = { onPetClick(pet.id) })
                }
            }
        }
    }
}

@Composable
fun FilterRow(label: String, options: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$label：", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.width(44.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            options.forEach { (value, display) ->
                FilterChip(
                    selected = selected == value,
                    onClick = { onSelect(value) },
                    label = { Text(display, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFF5A623),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun PetGridCard(pet: Pet, isFavorited: Boolean, onToggleFavorite: () -> Unit, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            val imageUrl = pet.imageUrls?.split(",")?.firstOrNull()?.trim()
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(imageUrl).crossfade(true).build(),
                contentDescription = pet.name,
                modifier = Modifier.fillMaxWidth().aspectRatio(4f / 5f).clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
            // 收藏按钮
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(32.dp)
                    .background(Color.White.copy(alpha = 0.7f), shape = MaterialTheme.shapes.small)
            ) {
                Icon(
                    if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFavorited) Color(0xFFE53935) else Color(0xFFE67E22),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column(Modifier.padding(12.dp)) {
            Text(pet.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(pet.species, fontSize = 12.sp, color = Color(0xFFE8496C),
                    modifier = Modifier.background(Color(0xFFFFE0E6), MaterialTheme.shapes.small).padding(horizontal = 6.dp, vertical = 2.dp))
                Text(pet.sizeLevel ?: "", fontSize = 12.sp, color = Color(0xFFE67E22),
                    modifier = Modifier.background(Color(0xFFFFF3E0), MaterialTheme.shapes.small).padding(horizontal = 6.dp, vertical = 2.dp))
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val healthy = pet.healthStatus == "健康"
                Box(Modifier.size(8.dp).background(if (healthy) Color(0xFF4CAF50) else Color(0xFFFFC107), shape = MaterialTheme.shapes.small))
                Spacer(Modifier.width(4.dp))
                Text(pet.healthStatus ?: "未知", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
