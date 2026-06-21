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
import com.pawmatch.app.data.model.AdoptionReview
import com.pawmatch.app.ui.theme.*
import com.pawmatch.app.viewmodel.ShelterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelterAdoptionsScreen(
    onBack: () -> Unit,
    viewModel: ShelterViewModel = viewModel()
) {
    val adoptions by viewModel.adoptions.collectAsState()
    val isLoading by viewModel.isAdoptionsLoading.collectAsState()
    val total by viewModel.adoptionTotal.collectAsState()
    val auditResult by viewModel.auditResult.collectAsState()

    var statusFilter by remember { mutableStateOf<Int?>(null) }
    var rejectDialogTarget by remember { mutableStateOf<Long?>(null) }
    var rejectReason by remember { mutableStateOf("") }
    var completeConfirmTarget by remember { mutableStateOf<Long?>(null) }
    var currentPage by remember { mutableIntStateOf(1) }

    LaunchedEffect(Unit) { viewModel.loadAdoptions() }

    LaunchedEffect(auditResult) {
        if (auditResult != null) {
            viewModel.loadAdoptions(statusFilter, currentPage)
            viewModel.clearAuditResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("领养审核") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // 状态筛选
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = statusFilter == null,
                    onClick = { statusFilter = null; currentPage = 1; viewModel.loadAdoptions(null) },
                    label = { Text("全部") }
                )
                FilterChip(
                    selected = statusFilter == 0,
                    onClick = { statusFilter = 0; currentPage = 1; viewModel.loadAdoptions(0) },
                    label = { Text("待审核") }
                )
                FilterChip(
                    selected = statusFilter == 1,
                    onClick = { statusFilter = 1; currentPage = 1; viewModel.loadAdoptions(1) },
                    label = { Text("已通过") }
                )
                FilterChip(
                    selected = statusFilter == 2,
                    onClick = { statusFilter = 2; currentPage = 1; viewModel.loadAdoptions(2) },
                    label = { Text("已拒绝") }
                )
                FilterChip(
                    selected = statusFilter == 3,
                    onClick = { statusFilter = 3; currentPage = 1; viewModel.loadAdoptions(3) },
                    label = { Text("已完成") }
                )
            }

            // 总数
            Text(
                "共 $total 条记录",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                fontSize = 12.sp,
                color = TextSecondary
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (adoptions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(48.dp), tint = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("暂无相关申请", color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(adoptions) { item ->
                        AdoptionCard(
                            item = item,
                            onApprove = {
                                viewModel.auditAdoption(item.id, 1)
                            },
                            onReject = {
                                rejectDialogTarget = item.id
                                rejectReason = ""
                            },
                            onComplete = {
                                completeConfirmTarget = item.id
                            }
                        )
                    }
                }
            }
        }
    }

    // 拒绝原因弹窗
    rejectDialogTarget?.let { id ->
        AlertDialog(
            onDismissRequest = { rejectDialogTarget = null },
            title = { Text("拒绝申请") },
            text = {
                OutlinedTextField(
                    value = rejectReason,
                    onValueChange = { rejectReason = it },
                    label = { Text("拒绝原因") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.auditAdoption(id, 2, rejectReason)
                        rejectDialogTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) { Text("确认拒绝") }
            },
            dismissButton = {
                TextButton(onClick = { rejectDialogTarget = null }) { Text("取消") }
            }
        )
    }

    // 完成领养确认
    completeConfirmTarget?.let { id ->
        AlertDialog(
            onDismissRequest = { completeConfirmTarget = null },
            title = { Text("确认完成") },
            text = { Text("确认标记该领养已完成？") },
            confirmButton = {
                Button(onClick = {
                    viewModel.completeAdoption(id)
                    completeConfirmTarget = null
                }) { Text("确认") }
            },
            dismissButton = {
                TextButton(onClick = { completeConfirmTarget = null }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun AdoptionCard(
    item: AdoptionReview,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onComplete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // 头部：编号 + 宠物名 + 状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("No.${item.id}", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(item.petName ?: "未知", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        item.petType ?: "",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                StatusTag(item.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 申请人 + 申请时间
            Row {
                Text("申请人：", fontSize = 12.sp, color = TextSecondary)
                Text(item.userName ?: "-", fontSize = 12.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text(item.applyTime ?: "", fontSize = 11.sp, color = TextSecondary)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 领养理由
            if (!item.reason.isNullOrBlank()) {
                Text("领养理由：", fontSize = 12.sp, color = TextSecondary)
                Text(
                    item.reason,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 饲养经验
            if (!item.experience.isNullOrBlank()) {
                Text("饲养经验：", fontSize = 12.sp, color = TextSecondary)
                Text(
                    item.experience,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 住房条件
            if (!item.housingCondition.isNullOrBlank()) {
                Text("住房条件：", fontSize = 12.sp, color = TextSecondary)
                Text(
                    item.housingCondition,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 拒绝原因
            if (item.status == 2 && !item.rejectReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("拒绝原因：${item.rejectReason}", fontSize = 12.sp, color = Error)
            }

            // 操作按钮
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                when (item.status) {
                    0 -> {
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
                    1 -> {
                        Button(
                            onClick = onComplete,
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                        ) { Text("完成领养", fontSize = 13.sp) }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusTag(status: Int) {
    val (text, color) = when (status) {
        0 -> "待审核" to HealthYellow
        1 -> "已通过" to HealthGreen
        2 -> "已拒绝" to Error
        3 -> "已完成" to Primary
        else -> "未知" to TextSecondary
    }
    AssistChip(
        onClick = {},
        label = { Text(text, fontSize = 11.sp) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.12f),
            labelColor = color
        )
    )
}