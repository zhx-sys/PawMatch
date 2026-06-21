package com.pawmatch.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pawmatch.app.data.model.Comment
import com.pawmatch.app.viewmodel.PostDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(postId: Long, onBack: () -> Unit) {
    val vm: PostDetailViewModel = viewModel()
    val detail by vm.postDetail.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val isLiking by vm.isLiking.collectAsState()
    val isSending by vm.isSending.collectAsState()
    val error by vm.error.collectAsState()
    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(postId) { vm.loadPostDetail(postId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail?.title ?: "帖子详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFFDF8F0)
    ) { padding ->
        if (isLoading && detail == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFE67E22))
            }
            return@Scaffold
        }

        error?.let { err ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(err, color = Color.Red)
            }
            return@Scaffold
        }

        val post = detail ?: return@Scaffold

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // 帖子内容区
            item {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = MaterialTheme.shapes.medium) {
                    Column(Modifier.padding(16.dp)) {
                        if (post.title.isNotBlank()) {
                            Text(post.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                            Spacer(Modifier.height(8.dp))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(post.nickname ?: "匿名用户", fontSize = 13.sp, color = Color.Gray)
                            post.createTime?.let { Text(it.take(16).replace("T", " "), fontSize = 13.sp, color = Color.Gray) }
                            Text("浏览 ${post.viewCount}", fontSize = 13.sp, color = Color.Gray)
                            Text("评论 ${post.commentCount}", fontSize = 13.sp, color = Color.Gray)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(post.content, fontSize = 15.sp, lineHeight = 24.sp, color = Color(0xFF555555))

                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { vm.likePost(postId) },
                            enabled = !isLiking,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (post.liked) Color(0xFFE53935) else Color(0xFFE67E22)
                            )
                        ) {
                            Icon(
                                if (post.liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(if (post.liked) "已赞" else "点赞")
                            Spacer(Modifier.width(4.dp))
                            Text("${post.likeCount}")
                        }
                    }
                }
            }

            // 分隔
            item { Spacer(Modifier.height(16.dp)) }

            // 评论标题
            item {
                Text("评论 (${post.commentCount})", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF333333))
                Spacer(Modifier.height(12.dp))
            }

            // 评论列表
            val comments = post.comments
            if (comments.isEmpty()) {
                item { Text("暂无评论，快来抢沙发", color = Color.Gray, fontSize = 14.sp) }
            }
            items(comments) { comment -> CommentItem(comment) }

            // 底部占位
            item { Spacer(Modifier.height(80.dp)) }
        }

        // 底部评论输入
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("写评论...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        shape = MaterialTheme.shapes.large
                    )
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                vm.submitComment(postId, commentText.trim())
                                commentText = ""
                            }
                        },
                        enabled = !isSending && commentText.isNotBlank()
                    ) {
                        if (isSending) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFFE67E22))
                        else Icon(Icons.Default.Send, contentDescription = "发送", tint = Color(0xFFE67E22))
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment, isReply: Boolean = false) {
    val indent = if (isReply) 24.dp else 0.dp
    Column(
        Modifier
            .padding(start = indent)
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(comment.nickname ?: "匿名用户", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
            Text(comment.createTime ?: "", fontSize = 12.sp, color = Color(0xFFBBBBBB))
        }
        Spacer(Modifier.height(4.dp))
        Text(comment.content, fontSize = 14.sp, color = Color(0xFF555555), lineHeight = 20.sp)
        // 子回复
        comment.replies?.forEach { reply ->
            Spacer(Modifier.height(4.dp))
            CommentItem(comment = reply, isReply = true)
        }
        HorizontalDivider(Modifier.padding(top = 8.dp), color = Color(0xFFF0F0F0))
    }
}
