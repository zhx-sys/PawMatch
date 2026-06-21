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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pawmatch.app.data.api.TokenManager
import com.pawmatch.app.data.model.Post
import com.pawmatch.app.ui.theme.*
import com.pawmatch.app.viewmodel.CommunityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    onBack: () -> Unit,
    onPostClick: (Long) -> Unit,
    viewModel: CommunityViewModel = viewModel()
) {
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val reportError by viewModel.reportError.collectAsState()
    val friendIds by viewModel.friendIds.collectAsState()
    val isShelter = TokenManager.userType == 1

    LaunchedEffect(Unit) { viewModel.loadPosts() }

    var reportTargetPost by remember { mutableStateOf<Post?>(null) }
    var showReportSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("社区") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } }
            )
        },
        snackbarHost = {
            reportError?.let { error ->
                Snackbar(modifier = Modifier.padding(16.dp)) {
                    Text(error)
                }
                LaunchedEffect(error) { viewModel.clearReportError() }
            }
        }
    ) { padding ->
        if (isLoading && posts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(posts) { post ->
                    PostItem(
                        post = post,
                        onClick = { onPostClick(post.id) },
                        isShelter = isShelter,
                        onReport = { reportTargetPost = post; showReportSheet = true },
                        onTakeDown = { viewModel.takeDownPost(post.id) },
                        friendIds = friendIds,
                        onAddFriend = { viewModel.sendFriendRequest(post) }
                    )
                }
            }
        }
    }

    // 举报原因选择
    if (showReportSheet && reportTargetPost != null) {
        ReportReasonSheet(
            onDismiss = { showReportSheet = false; reportTargetPost = null },
            onConfirm = { reason ->
                viewModel.reportPost(TokenManager.userId, reportTargetPost!!.id, reason)
                showReportSheet = false
                reportTargetPost = null
            }
        )
    }
}

@Composable
private fun ReportReasonSheet(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val reasons = listOf("垃圾广告", "色情低俗", "虚假信息", "人身攻击", "侵权内容", "其他")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("举报帖子") },
        text = {
            Column {
                Text("请选择举报原因：", fontSize = 14.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                reasons.forEach { reason ->
                    TextButton(
                        onClick = { onConfirm(reason) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(reason, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
fun PostItem(
    post: Post,
    onClick: () -> Unit,
    isShelter: Boolean = false,
    onReport: () -> Unit = {},
    onTakeDown: () -> Unit = {},
    friendIds: Set<Long> = emptySet(),
    onAddFriend: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = Surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Accent, shape = MaterialTheme.shapes.extraLarge, modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text((post.nickname ?: "U").take(1), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(post.nickname ?: "用户", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    post.createTime?.let {
                        Text(it.take(10), fontSize = 11.sp, color = TextSecondary)
                    }
                }
                if (post.userId != TokenManager.userId && !friendIds.contains(post.userId)) {
                    TextButton(
                        onClick = onAddFriend,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("加好友", fontSize = 12.sp, color = Primary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (post.title.isNotBlank()) {
                Text(post.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(post.content, maxLines = 4, overflow = TextOverflow.Ellipsis, fontSize = 14.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = if (post.liked) FavoriteRed else TextSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${post.likeCount}", fontSize = 12.sp, color = TextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${post.commentCount}", fontSize = 12.sp, color = TextSecondary)
                    }
                }
                // 动作按钮：普通用户举报，救助站下架
                if (isShelter) {
                    TextButton(
                        onClick = onTakeDown,
                        colors = ButtonDefaults.textButtonColors(contentColor = Error),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("下架", fontSize = 12.sp)
                    }
                } else {
                    TextButton(
                        onClick = onReport,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFF0A04B)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("举报", fontSize = 12.sp)
                    }
                }
            }
        }
    }
    HorizontalDivider()
}
