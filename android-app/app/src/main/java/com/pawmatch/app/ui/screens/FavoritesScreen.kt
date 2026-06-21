package com.pawmatch.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.pawmatch.app.data.model.FavoritePet
import com.pawmatch.app.ui.theme.*
import com.pawmatch.app.viewmodel.PetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBack: () -> Unit,
    onPetClick: (Long) -> Unit,
    viewModel: PetViewModel = viewModel()
) {
    val favoritePets by viewModel.favoritePets.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadFavoriteList() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的收藏") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        if (isLoading && favoritePets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (favoritePets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("还没有收藏宠物", color = TextSecondary)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(favoritePets) { fav ->
                    FavoritePetCard(
                        fav = fav,
                        onClick = { onPetClick(fav.petId) }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoritePetCard(fav: FavoritePet, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column {
            val firstImage = fav.imageUrls?.split(",")?.firstOrNull()?.trim()
            if (!firstImage.isNullOrBlank()) {
                val baseUrl = ServerConfigManager.serverUrl.removeSuffix("/api/").removeSuffix("/api")
                AsyncImage(
                    model = "$baseUrl$firstImage",
                    contentDescription = fav.name,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Pets, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(40.dp))
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(fav.name.ifEmpty { "未命名" }, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(fav.species, fontSize = 12.sp, color = TextSecondary)
                    if (fav.gender.isNotEmpty()) {
                        Text(" · ${fav.gender}", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}
