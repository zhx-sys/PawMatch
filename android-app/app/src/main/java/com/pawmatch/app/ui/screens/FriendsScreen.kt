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
import com.pawmatch.app.data.model.FriendResponse
import com.pawmatch.app.data.model.SearchUser
import com.pawmatch.app.ui.theme.*
import com.pawmatch.app.viewmodel.FriendViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    onBack: () -> Unit,
    onChatWithFriend: (Long, Int, String) -> Unit,
    viewModel: FriendViewModel = viewModel()
) {
    val friends by viewModel.friends.collectAsState()
    val pendingRequests by viewModel.pendingRequests.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var searchKeyword by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf<FriendResponse?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadFriends()
        viewModel.loadPendingRequests()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("好友") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "添加好友")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            // 待处理请求
            if (pendingRequests.isNotEmpty()) {
                item {
                    Text(
                        "新的好友请求 (${pendingRequests.size})",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
                items(pendingRequests) { req ->
                    PendingRequestItem(
                        req = req,
                        onAccept = { viewModel.acceptFriend(req.id) },
                        onReject = { viewModel.rejectFriend(req.id) }
                    )
                }
                item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }
            }

            // 好友列表
            if (friends.isEmpty() && pendingRequests.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) {
                        Text("还没有好友，点击右上角添加", color = TextSecondary)
                    }
                }
            }
            items(friends) { friend ->
                FriendItem(
                    friend = friend,
                    onClick = { onChatWithFriend(friend.friendId, friend.friendUserType, friend.nickname ?: "") },
                    onDelete = { showDeleteConfirm = friend }
                )
            }
        }
    }

    // 添加好友对话框
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; viewModel.clearError() },
            title = { Text("添加好友") },
            text = {
                Column {
                    OutlinedTextField(
                        value = searchKeyword,
                        onValueChange = { searchKeyword = it },
                        placeholder = { Text("输入用户昵称搜索") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { viewModel.searchUsers(searchKeyword) }) {
                                Icon(Icons.Default.Search, contentDescription = null)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (searchResults.isEmpty() && searchKeyword.isNotEmpty()) {
                        Text("未找到用户", color = TextSecondary, fontSize = 13.sp)
                    }
                    searchResults.forEach { user ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(user.nickname ?: "用户${user.id}", modifier = Modifier.weight(1f))
                            Button(
                                onClick = { viewModel.sendFriendRequest(user) },
                                enabled = !user.added
                            ) {
                                Text(if (user.added) "已发送" else "加好友", fontSize = 12.sp)
                            }
                        }
                        HorizontalDivider()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("关闭") }
            }
        )
    }

    // 删除好友确认
    showDeleteConfirm?.let { friend ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("删除好友") },
            text = { Text("确定要删除好友「${friend.nickname ?: "用户${friend.friendId}"}」吗？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteFriend(friend.friendId)
                    showDeleteConfirm = null
                }) { Text("删除", color = Error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("取消") }
            }
        )
    }
}

@Composable
fun FriendItem(friend: FriendResponse, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = Primary,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text((friend.nickname ?: "U").take(1), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(friend.nickname ?: "用户${friend.friendId}", modifier = Modifier.weight(1f))
        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "删除好友", tint = Color(0xFFCCCCCC), modifier = Modifier.size(20.dp))
        }
        IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Chat, contentDescription = "发消息", tint = Primary, modifier = Modifier.size(20.dp))
        }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 68.dp))
}

@Composable
fun PendingRequestItem(req: FriendResponse, onAccept: () -> Unit, onReject: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(req.nickname ?: "用户${req.friendId}", modifier = Modifier.weight(1f))
        TextButton(onClick = onAccept, colors = ButtonDefaults.textButtonColors(contentColor = Primary)) {
            Text("接受", fontSize = 12.sp)
        }
        TextButton(onClick = onReject, colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)) {
            Text("拒绝", fontSize = 12.sp)
        }
    }
}
