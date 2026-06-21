package com.pawmatch.app.ui.screens

import androidx.compose.foundation.clickable
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
import com.pawmatch.app.ui.theme.*
import com.pawmatch.app.viewmodel.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    viewModel: NotificationViewModel = viewModel()
) {
    val notifications by viewModel.notifications.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadNotifications() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("通知") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } },
                actions = {
                    TextButton(onClick = { viewModel.markAllRead() }) {
                        Text("全部已读", fontSize = 13.sp)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            if (notifications.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) {
                        Text("暂无通知", color = TextSecondary)
                    }
                }
            }
            items(notifications) { notif ->
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.markAsRead(notif.id) },
                    color = if (notif.read || notif.isRead) Surface else PrimaryLight.copy(alpha = 0.15f)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        Icon(
                            when (notif.type) {
                                "ADOPTION" -> Icons.Default.Pets
                                "FRIEND" -> Icons.Default.PersonAdd
                                "MESSAGE" -> Icons.Default.Chat
                                else -> Icons.Default.Notifications
                            },
                            contentDescription = null,
                            tint = Accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(notif.title, fontWeight = if (notif.read) FontWeight.Normal else FontWeight.SemiBold, fontSize = 14.sp)
                            notif.content?.let {
                                Text(it, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                        notif.createTime?.let {
                            Text(it.take(10), fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }
}
