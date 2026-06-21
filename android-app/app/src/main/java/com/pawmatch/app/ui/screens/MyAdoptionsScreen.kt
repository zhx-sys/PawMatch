package com.pawmatch.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.pawmatch.app.data.model.AdoptionApplication
import com.pawmatch.app.ui.theme.*
import com.pawmatch.app.viewmodel.PetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAdoptionsScreen(
    onBack: () -> Unit,
    viewModel: PetViewModel = viewModel()
) {
    val adoptions by viewModel.myAdoptions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadMyAdoptions() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的领养申请") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        if (isLoading && adoptions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (adoptions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Pets, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("暂无领养申请", color = TextSecondary, fontSize = 15.sp)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(adoptions) { app ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = Surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Pets, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(app.petName ?: "宠物 #${app.petId}", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.weight(1f))
                                AdoptionStatusBadge(app.status)
                            }
                            app.message?.let { msg ->
                                if (msg.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("留言: $msg", fontSize = 13.sp, color = TextSecondary)
                                }
                            }
                            app.applyTime?.let {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("申请时间: ${it.take(16).replace("T", " ")}", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdoptionStatusBadge(status: Int) {
    val (text, color) = when (status) {
        0 -> "待审核" to Accent
        1 -> "已通过" to Color(0xFF4CAF50)
        2 -> "已拒绝" to Error
        3 -> "已领养" to Color(0xFF4CAF50)
        else -> "未知" to TextSecondary
    }
    Surface(color = color.copy(alpha = 0.12f), shape = MaterialTheme.shapes.small) {
        Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
    }
}
