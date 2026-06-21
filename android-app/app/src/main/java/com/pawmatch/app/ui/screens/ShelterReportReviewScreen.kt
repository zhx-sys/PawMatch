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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pawmatch.app.data.model.ReportItem
import com.pawmatch.app.ui.theme.*
import com.pawmatch.app.viewmodel.ShelterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelterReportReviewScreen(
    onBack: () -> Unit,
    viewModel: ShelterViewModel = viewModel()
) {
    val reports by viewModel.reportItems.collectAsState()
    val isLoading by viewModel.isReportsLoading.collectAsState()
    val reviewResult by viewModel.reportReviewResult.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadReports() }

    LaunchedEffect(reviewResult) {
        if (reviewResult != null) {
            viewModel.loadReports()
            viewModel.clearReportReviewResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("举报审核") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                "共 ${reports.size} 条待处理举报",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 12.sp,
                color = TextSecondary
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (reports.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("暂无待处理举报", color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(reports) { item ->
                        ReportReviewCard(
                            item = item,
                            onApprove = { viewModel.reviewReport(item.id, 1) },
                            onReject = { viewModel.reviewReport(item.id, 2) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportReviewCard(
    item: ReportItem,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // 头部：举报人 + 类型标签 + 时间
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.reporterName ?: "用户${item.reporterId}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                if (item.targetType == "POST") "帖子" else "评论",
                                fontSize = 11.sp
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Accent.copy(alpha = 0.12f),
                            labelColor = Accent
                        )
                    )
                }
                Text(item.createTime ?: "", fontSize = 11.sp, color = TextSecondary)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 被举报内容
            if (!item.targetTitle.isNullOrBlank()) {
                Text(
                    item.targetTitle,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            if (!item.targetContent.isNullOrBlank()) {
                Text(
                    item.targetContent,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 举报原因
            if (!item.reason.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("举报原因：", fontSize = 12.sp, color = TextSecondary)
                    Text(
                        item.reason,
                        fontSize = 12.sp,
                        color = Color(0xFFF0A04B),
                        fontWeight = FontWeight.Medium
                    )
                }
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
                ) { Text("驳回", fontSize = 13.sp) }
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
