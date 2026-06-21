package com.pawmatch.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pawmatch.app.data.model.WikiReviewItem
import com.pawmatch.app.ui.theme.*
import com.pawmatch.app.viewmodel.ShelterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelterWikiReviewScreen(
    onBack: () -> Unit,
    viewModel: ShelterViewModel = viewModel()
) {
    val entries by viewModel.wikiReviews.collectAsState()
    val isLoading by viewModel.isWikiReviewsLoading.collectAsState()
    val total by viewModel.wikiReviewTotal.collectAsState()
    val reviewResult by viewModel.wikiReviewResult.collectAsState()

    var currentPage by remember { mutableIntStateOf(1) }
    var rejectTargetId by remember { mutableStateOf<Long?>(null) }
    var confirmApproveTarget by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) { viewModel.loadWikiReviews() }

    LaunchedEffect(reviewResult) {
        if (reviewResult != null) {
            viewModel.loadWikiReviews(currentPage)
            viewModel.clearWikiReviewResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("百科审核") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // 统计信息
            Text(
                "共 $total 条待审核词条",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 12.sp,
                color = TextSecondary
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (entries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("暂无待审核词条", color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(entries) { item ->
                        WikiReviewCard(
                            item = item,
                            onApprove = { confirmApproveTarget = item.id },
                            onReject = { rejectTargetId = item.id }
                        )
                    }
                }
            }
        }
    }

    // 通过确认弹窗
    confirmApproveTarget?.let { id ->
        AlertDialog(
            onDismissRequest = { confirmApproveTarget = null },
            title = { Text("确认通过") },
            text = { Text("确定通过该词条的审核？") },
            confirmButton = {
                Button(onClick = {
                    viewModel.reviewWikiEntry(id, true)
                    confirmApproveTarget = null
                }) { Text("确认通过") }
            },
            dismissButton = {
                TextButton(onClick = { confirmApproveTarget = null }) { Text("取消") }
            }
        )
    }

    // 拒绝确认弹窗
    rejectTargetId?.let { id ->
        AlertDialog(
            onDismissRequest = { rejectTargetId = null },
            title = { Text("确认拒绝") },
            text = { Text("确定拒绝该词条？") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reviewWikiEntry(id, false)
                        rejectTargetId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) { Text("确认拒绝") }
            },
            dismissButton = {
                TextButton(onClick = { rejectTargetId = null }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun WikiReviewCard(
    item: WikiReviewItem,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // 头部：编号 + 标题 + 分类标签
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("No.${item.id}", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        item.title ?: "无标题",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                if (!item.categoryName.isNullOrBlank()) {
                    AssistChip(
                        onClick = {},
                        label = { Text(item.categoryName, fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Primary.copy(alpha = 0.12f),
                            labelColor = Primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 作者 + 提交时间
            Row {
                if (!item.authorName.isNullOrBlank()) {
                    Text("作者：", fontSize = 12.sp, color = TextSecondary)
                    Text(item.authorName, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(item.createTime ?: "", fontSize = 11.sp, color = TextSecondary)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 摘要
            if (!item.summary.isNullOrBlank()) {
                Text(
                    item.summary,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 操作按钮
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) { Text("拒绝", fontSize = 13.sp) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = HealthGreen),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) { Text("通过", fontSize = 13.sp) }
            }
        }
    }
}