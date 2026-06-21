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
import com.pawmatch.app.data.model.*
import com.pawmatch.app.ui.theme.*
import com.pawmatch.app.viewmodel.ShelterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelterFosterScreen(
    onBack: () -> Unit,
    viewModel: ShelterViewModel = viewModel()
) {
    val services by viewModel.fosterServices.collectAsState()
    val orders by viewModel.fosterOrders.collectAsState()
    val isLoading by viewModel.isFosterLoading.collectAsState()
    val fosterResult by viewModel.fosterResult.collectAsState()
    val isSaving by viewModel.fosterServiceSaving.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showServiceDialog by remember { mutableStateOf(false) }
    var editingService by remember { mutableStateOf<FosterServiceItem?>(null) }

    // 服务表单
    var formTitle by remember { mutableStateOf("") }
    var formDescription by remember { mutableStateOf("") }
    var formPetType by remember { mutableStateOf("狗") }
    var formPricePerDay by remember { mutableStateOf("") }
    var formMaxCapacity by remember { mutableStateOf("") }

    // 确认弹窗状态
    var confirmOrderTarget by remember { mutableStateOf<Long?>(null) }
    var completeOrderTarget by remember { mutableStateOf<Long?>(null) }
    var deleteServiceTarget by remember { mutableStateOf<FosterServiceItem?>(null) }

    LaunchedEffect(Unit) { viewModel.loadFosterData() }

    LaunchedEffect(fosterResult) {
        if (fosterResult != null) {
            viewModel.clearFosterResult()
        }
    }

    fun resetForm() {
        formTitle = ""
        formDescription = ""
        formPetType = "狗"
        formPricePerDay = ""
        formMaxCapacity = ""
    }

    fun openCreateDialog() {
        editingService = null
        resetForm()
        showServiceDialog = true
    }

    fun openEditDialog(service: FosterServiceItem) {
        editingService = service
        formTitle = service.title ?: ""
        formDescription = service.description ?: ""
        formPetType = service.petType ?: "狗"
        formPricePerDay = service.pricePerDay.toInt().toString()
        formMaxCapacity = service.maxCapacity.toString()
        showServiceDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("寄养管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Tab 切换
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("我的寄养服务") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("收到订单") }
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTab) {
                    0 -> FosterServicesTab(
                        services = services,
                        onPublishNew = { openCreateDialog() },
                        onEdit = { openEditDialog(it) },
                        onDelete = { deleteServiceTarget = it }
                    )
                    1 -> FosterOrdersTab(
                        orders = orders,
                        onConfirm = { confirmOrderTarget = it.id },
                        onComplete = { completeOrderTarget = it.id }
                    )
                }
            }
        }
    }

    // 发布/编辑服务弹窗
    if (showServiceDialog) {
        AlertDialog(
            onDismissRequest = { showServiceDialog = false },
            title = { Text(if (editingService != null) "编辑服务" else "发布新服务") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = formTitle,
                        onValueChange = { formTitle = it },
                        label = { Text("标题") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = formDescription,
                        onValueChange = { formDescription = it },
                        label = { Text("描述") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    // 宠物类型选择
                    var typeExpanded by remember { mutableStateOf(false) }
                    val petTypes = listOf("狗", "猫", "猫狗均可")
                    ExposedDropdownMenuBox(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = formPetType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("宠物类型") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = typeExpanded,
                            onDismissRequest = { typeExpanded = false }
                        ) {
                            petTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        formPetType = type
                                        typeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = formPricePerDay,
                        onValueChange = { formPricePerDay = it.filter { c -> c.isDigit() } },
                        label = { Text("每日价格（元）") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = formMaxCapacity,
                        onValueChange = { formMaxCapacity = it.filter { c -> c.isDigit() } },
                        label = { Text("最大容量") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (formTitle.isBlank()) return@Button
                        val price = formPricePerDay.toDoubleOrNull() ?: 0.0
                        val capacity = formMaxCapacity.toIntOrNull() ?: 1
                        val editing = editingService
                        if (editing != null) {
                            viewModel.updateFosterService(
                                editing.id,
                                UpdateFosterServiceRequest(formTitle, formDescription, formPetType, price, capacity)
                            )
                        } else {
                            viewModel.createFosterService(
                                AddFosterServiceRequest(formTitle, formDescription, formPetType, price, capacity)
                            )
                        }
                        showServiceDialog = false
                    },
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showServiceDialog = false }) { Text("取消") }
            }
        )
    }

    // 删除服务确认
    deleteServiceTarget?.let { service ->
        AlertDialog(
            onDismissRequest = { deleteServiceTarget = null },
            title = { Text("确认下架") },
            text = { Text("确定下架「${service.title}」？") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFosterService(service.id)
                        deleteServiceTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) { Text("确认下架") }
            },
            dismissButton = {
                TextButton(onClick = { deleteServiceTarget = null }) { Text("取消") }
            }
        )
    }

    // 确认接单
    confirmOrderTarget?.let { id ->
        AlertDialog(
            onDismissRequest = { confirmOrderTarget = null },
            title = { Text("确认接单") },
            text = { Text("确定接受该寄养预约？") },
            confirmButton = {
                Button(onClick = {
                    viewModel.confirmFosterOrder(id)
                    confirmOrderTarget = null
                }) { Text("确认") }
            },
            dismissButton = {
                TextButton(onClick = { confirmOrderTarget = null }) { Text("取消") }
            }
        )
    }

    // 完成订单
    completeOrderTarget?.let { id ->
        AlertDialog(
            onDismissRequest = { completeOrderTarget = null },
            title = { Text("确认完成") },
            text = { Text("确认该寄养已完成？") },
            confirmButton = {
                Button(onClick = {
                    viewModel.completeFosterOrder(id)
                    completeOrderTarget = null
                }) { Text("确认") }
            },
            dismissButton = {
                TextButton(onClick = { completeOrderTarget = null }) { Text("取消") }
            }
        )
    }
}

// ===== 寄养服务Tab =====
@Composable
private fun FosterServicesTab(
    services: List<FosterServiceItem>,
    onPublishNew: () -> Unit,
    onEdit: (FosterServiceItem) -> Unit,
    onDelete: (FosterServiceItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部操作栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "已发布服务",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = TextPrimary
            )
            Button(
                onClick = onPublishNew,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("发布新服务", fontSize = 13.sp)
            }
        }

        if (services.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("还没有发布寄养服务", color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(services) { service ->
                    FosterServiceCard(
                        service = service,
                        onEdit = { onEdit(service) },
                        onDelete = { onDelete(service) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FosterServiceCard(
    service: FosterServiceItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    service.title ?: "",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                StatusTag(service.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column {
                    Text("类型", fontSize = 11.sp, color = TextSecondary)
                    Text(service.petType ?: "-", fontSize = 13.sp)
                }
                Column {
                    Text("价格", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        "${service.pricePerDay.toInt()}元/天",
                        fontSize = 13.sp,
                        color = Accent,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column {
                    Text("容量", fontSize = 11.sp, color = TextSecondary)
                    Text("${service.maxCapacity}只", fontSize = 13.sp)
                }
            }

            if (!service.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    service.description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                ) { Text("编辑", fontSize = 13.sp) }
                Spacer(modifier = Modifier.width(8.dp))
                if (service.status == 1) {
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = Error),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                    ) { Text("下架", fontSize = 13.sp) }
                }
            }
        }
    }
}

// ===== 寄养订单Tab =====
@Composable
private fun FosterOrdersTab(
    orders: List<FosterOrderItem>,
    onConfirm: (FosterOrderItem) -> Unit,
    onComplete: (FosterOrderItem) -> Unit
) {
    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Inbox,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("暂无订单", color = TextSecondary)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(orders) { order ->
                FosterOrderCard(
                    order = order,
                    onConfirm = { onConfirm(order) },
                    onComplete = { onComplete(order) }
                )
            }
        }
    }
}

@Composable
private fun FosterOrderCard(
    order: FosterOrderItem,
    onConfirm: () -> Unit,
    onComplete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // 头部：编号 + 服务名 + 状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("No.${order.id}", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        order.serviceName ?: "未知服务",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                FosterOrderStatusTag(order.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 宠物 + 类型 + 日期
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text("宠物", fontSize = 11.sp, color = TextSecondary)
                    Text(order.petName ?: "-", fontSize = 13.sp)
                }
                Column {
                    Text("类型", fontSize = 11.sp, color = TextSecondary)
                    Text(order.petType ?: "-", fontSize = 13.sp)
                }
                Column {
                    Text("金额", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        "${order.totalPrice.toInt()}元",
                        fontSize = 13.sp,
                        color = Accent,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 日期范围
            Text(
                "${order.startDate ?: ""} ~ ${order.endDate ?: ""}",
                fontSize = 12.sp,
                color = TextSecondary
            )

            // 操作按钮
            if (order.status == 0 || order.status == 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (order.status == 0) {
                        Button(
                            onClick = onConfirm,
                            colors = ButtonDefaults.buttonColors(containerColor = HealthGreen),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) { Text("确认接单", fontSize = 13.sp) }
                    }
                    if (order.status == 1) {
                        Button(
                            onClick = onComplete,
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) { Text("完成", fontSize = 13.sp) }
                    }
                }
            }
        }
    }
}

// ===== 共用组件 =====

@Composable
private fun StatusTag(status: Int) {
    val (text, color) = when (status) {
        0 -> "待审核" to HealthYellow
        1 -> "上架" to HealthGreen
        else -> "下架" to TextSecondary
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

@Composable
private fun FosterOrderStatusTag(status: Int) {
    val (text, color) = when (status) {
        0 -> "待确认" to HealthYellow
        1 -> "已确认" to Primary
        2 -> "-" to TextSecondary
        3 -> "已完成" to HealthGreen
        4 -> "已取消" to Error
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