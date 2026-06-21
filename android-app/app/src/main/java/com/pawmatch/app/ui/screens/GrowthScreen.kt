package com.pawmatch.app.ui.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.pawmatch.app.data.model.BadgeInfo
import com.pawmatch.app.data.model.PointsLogItem
import com.pawmatch.app.data.repository.PawMatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthScreen(
    onBack: () -> Unit,
    viewModel: GrowthViewModel = viewModel()
) {
    val pointsData by viewModel.pointsData.collectAsState()
    val badges by viewModel.badges.collectAsState()
    val pointsLog by viewModel.pointsLog.collectAsState()
    val isCheckingIn by viewModel.isCheckingIn.collectAsState()
    val checkinMsg by viewModel.checkinMsg.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    if (checkinMsg.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { viewModel.clearCheckinMsg() },
            title = { Text("签到") },
            text = { Text(checkinMsg) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearCheckinMsg() }) { Text("确定") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("成长激励") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFE67E22))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Check-in section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE67E22).copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "每日签到",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE67E22)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.checkin() },
                                enabled = !isCheckingIn,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE67E22)
                                ),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.fillMaxWidth(0.6f)
                            ) {
                                if (isCheckingIn) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("签到 +5积分", color = Color.White, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }

                // Points display
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE67E22).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "P",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE67E22)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "当前积分",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    "${pointsData["currentPoints"] ?: 0}",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE67E22)
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "等级",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    "Lv.${pointsData["level"] ?: 1}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "连续签到 ${pointsData["consecutiveDays"] ?: 0} 天",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                // Badges section
                item {
                    Text(
                        "我的徽章",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (badges.isEmpty()) {
                        Text("暂无徽章", fontSize = 13.sp, color = Color.Gray)
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.height((((badges.size + 3) / 4) * 110).dp)
                        ) {
                            items(badges) { badge ->
                                BadgeItem(badge)
                            }
                        }
                    }
                }

                // Points log section
                item {
                    Text(
                        "积分记录",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (pointsLog.isEmpty()) {
                    item {
                        Text(
                            "暂无记录",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(pointsLog) { log ->
                        PointsLogCard(log)
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeItem(badge: BadgeInfo) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val alpha = if (badge.earned) 1f else 0.35f
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFE67E22).copy(alpha = if (badge.earned) 0.15f else 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            if (badge.icon != null) {
                AsyncImage(
                    model = badge.icon,
                    contentDescription = badge.name,
                    modifier = Modifier
                        .size(40.dp)
                        .then(
                            if (!badge.earned) Modifier else Modifier
                        )
                        .then(Modifier.graphicsLayer(alpha = alpha))
                )
            } else {
                Text(
                    badge.name.take(1),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE67E22).copy(alpha = alpha)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            badge.name,
            fontSize = 10.sp,
            color = Color.Gray.copy(alpha = alpha),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun PointsLogCard(log: PointsLogItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    log.description ?: "积分变动",
                    fontSize = 14.sp
                )
                Text(
                    log.createTime ?: "",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Text(
                "+${log.points}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
        }
    }
}

class GrowthViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PawMatchRepository()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isCheckingIn = MutableStateFlow(false)
    val isCheckingIn: StateFlow<Boolean> = _isCheckingIn.asStateFlow()

    private val _checkinMsg = MutableStateFlow("")
    val checkinMsg: StateFlow<String> = _checkinMsg.asStateFlow()

    private val _pointsData = MutableStateFlow<Map<String, Any>>(emptyMap())
    val pointsData: StateFlow<Map<String, Any>> = _pointsData.asStateFlow()

    private val _badges = MutableStateFlow<List<BadgeInfo>>(emptyList())
    val badges: StateFlow<List<BadgeInfo>> = _badges.asStateFlow()

    private val _pointsLog = MutableStateFlow<List<PointsLogItem>>(emptyList())
    val pointsLog: StateFlow<List<PointsLogItem>> = _pointsLog.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            // load points and badges in parallel
            repo.getMyPoints().fold(
                onSuccess = { _pointsData.value = it },
                onFailure = { }
            )
            repo.getMyBadges().fold(
                onSuccess = { _badges.value = it },
                onFailure = { }
            )
            repo.getPointsLog().fold(
                onSuccess = { page -> _pointsLog.value = page?.records ?: emptyList() },
                onFailure = { }
            )
            _isLoading.value = false
        }
    }

    fun checkin() {
        viewModelScope.launch {
            _isCheckingIn.value = true
            repo.dailyCheckin().fold(
                onSuccess = { data ->
                    _checkinMsg.value = "签到成功，+5积分"
                    _pointsData.value = data
                    _isCheckingIn.value = false
                    loadData()
                },
                onFailure = { e ->
                    _checkinMsg.value = e.message ?: "今日已签到"
                    _isCheckingIn.value = false
                }
            )
        }
    }

    fun clearCheckinMsg() { _checkinMsg.value = "" }
}
