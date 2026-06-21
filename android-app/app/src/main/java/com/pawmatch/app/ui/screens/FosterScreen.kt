package com.pawmatch.app.ui.screens

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.pawmatch.app.data.model.CreateFosterOrderRequest
import com.pawmatch.app.data.model.FosterOrderItem
import com.pawmatch.app.data.model.FosterServiceItem
import com.pawmatch.app.data.model.ReviewRequest
import com.pawmatch.app.data.repository.PawMatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FosterScreen(
    onBack: () -> Unit,
    viewModel: FosterViewModel = viewModel()
) {
    val services by viewModel.services.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val resultMsg by viewModel.resultMsg.collectAsState()
    val selectedTab = remember { mutableIntStateOf(0) }
    val searchKeyword = remember { mutableStateOf("") }
    val petTypeFilter = remember { mutableStateOf("") }
    val showBookDialog = remember { mutableStateOf(false) }
    val selectedService = remember { mutableStateOf<FosterServiceItem?>(null) }
    val showReviewDialog = remember { mutableStateOf(false) }
    val selectedOrder = remember { mutableStateOf<FosterOrderItem?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadServices()
        viewModel.loadOrders()
    }

    if (resultMsg.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { viewModel.clearResultMsg() },
            title = { Text("提示") },
            text = { Text(resultMsg) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearResultMsg()
                    viewModel.loadOrders()
                }) { Text("确定") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("预约寄养") },
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
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = selectedTab.intValue) {
                Tab(selected = selectedTab.intValue == 0, onClick = { selectedTab.intValue = 0 }, text = { Text("寄养服务") })
                Tab(selected = selectedTab.intValue == 1, onClick = { selectedTab.intValue = 1 }, text = { Text("我的订单") })
            }
            when (selectedTab.intValue) {
                0 -> ServicesTab(
                    services = services,
                    isLoading = isLoading,
                    error = error,
                    searchKeyword = searchKeyword,
                    petTypeFilter = petTypeFilter,
                    onSearch = { viewModel.loadServices(searchKeyword.value, petTypeFilter.value) },
                    onBook = {
                        selectedService.value = it
                        showBookDialog.value = true
                    }
                )
                1 -> OrdersTab(
                    orders = orders,
                    isLoading = isLoading,
                    onCancel = { viewModel.cancelOrder(it.id) },
                    onReview = {
                        selectedOrder.value = it
                        showReviewDialog.value = true
                    }
                )
            }
        }
    }

    if (showBookDialog.value && selectedService.value != null) {
        BookDialog(
            service = selectedService.value!!,
            onDismiss = { showBookDialog.value = false },
            onSubmit = { request ->
                viewModel.createOrder(request)
                showBookDialog.value = false
            }
        )
    }

    if (showReviewDialog.value && selectedOrder.value != null) {
        ReviewDialog(
            order = selectedOrder.value!!,
            onDismiss = { showReviewDialog.value = false },
            onSubmit = { rating, comment ->
                viewModel.reviewOrder(selectedOrder.value!!.id, rating, comment)
                showReviewDialog.value = false
            }
        )
    }
}

@Composable
fun ServicesTab(
    services: List<FosterServiceItem>,
    isLoading: Boolean,
    error: String?,
    searchKeyword: MutableState<String>,
    petTypeFilter: MutableState<String>,
    onSearch: () -> Unit,
    onBook: (FosterServiceItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchKeyword.value,
                onValueChange = { searchKeyword.value = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("搜索寄养服务") },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = onSearch) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    }
                }
            )
            var expanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(onClick = { expanded = true }) {
                    Text(if (petTypeFilter.value.isEmpty()) "不限" else petTypeFilter.value)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf("" to "不限", "狗" to "狗", "猫" to "猫").forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                petTypeFilter.value = value
                                expanded = false
                                onSearch()
                            }
                        )
                    }
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFE67E22))
            }
        } else if (error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error, color = Color.Red)
            }
        } else if (services.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无寄养服务", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(services) { service ->
                    ServiceCard(service, onBook = { onBook(service) })
                }
            }
        }
    }
}

@Composable
fun ServiceCard(service: FosterServiceItem, onBook: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            val imageUrl = service.images?.let {
                if (it.contains(",")) it.split(",").first().trim() else it.trim()
            }
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(service.title ?: "", fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(service.description ?: "", fontSize = 13.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("¥${service.pricePerDay}/天", color = Color(0xFFE67E22), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("容量: ${service.maxCapacity}", fontSize = 12.sp, color = Color.Gray)
                }
                Text(service.shelterName ?: "", fontSize = 12.sp, color = Color.Gray)
            }
            Button(
                onClick = onBook,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE67E22)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("预约", color = Color.White)
            }
        }
    }
}

@Composable
fun OrdersTab(
    orders: List<FosterOrderItem>,
    isLoading: Boolean,
    onCancel: (FosterOrderItem) -> Unit,
    onReview: (FosterOrderItem) -> Unit
) {
    val statusLabels = mapOf(0 to "待确认", 1 to "已确认", 2 to "已完成", 3 to "已取消")
    val statusColors = mapOf(0 to Color(0xFFFFA726), 1 to Color(0xFF42A5F5), 2 to Color(0xFF4CAF50), 3 to Color(0xFF9E9E9E))

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFE67E22))
        }
    } else if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无订单", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders) { order ->
                val status = order.status
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(order.serviceName ?: "", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = statusColors[status]?.copy(alpha = 0.15f) ?: Color.Gray.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    statusLabels[status] ?: "未知",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 12.sp,
                                    color = statusColors[status] ?: Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("宠物: ${order.petName ?: ""}(${order.petType ?: ""})", fontSize = 13.sp)
                        Text("日期: ${order.startDate ?: ""} ~ ${order.endDate ?: ""}", fontSize = 12.sp, color = Color.Gray)
                        Text("总价: ¥${order.totalPrice} (${order.totalDays}天)", fontSize = 13.sp, color = Color(0xFFE67E22), fontWeight = FontWeight.Medium)
                        if (order.specialRequests != null && order.specialRequests.isNotEmpty()) {
                            Text("备注: ${order.specialRequests}", fontSize = 12.sp, color = Color.Gray)
                        }
                        if (status == 0 || status == 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { onCancel(order) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("取消订单")
                            }
                        }
                        if (status == 2 && order.rating == null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onReview(order) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE67E22)),
                                modifier = Modifier.align(Alignment.End),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("评价", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDialog(
    service: FosterServiceItem,
    onDismiss: () -> Unit,
    onSubmit: (CreateFosterOrderRequest) -> Unit
) {
    val petName = remember { mutableStateOf("") }
    val petType = remember { mutableStateOf(service.petType ?: "狗") }
    val startDate = remember { mutableStateOf("") }
    val endDate = remember { mutableStateOf("") }
    val specialRequests = remember { mutableStateOf("") }
    var petTypeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("预约寄养 - ${service.title}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = petName.value,
                    onValueChange = { petName.value = it },
                    label = { Text("宠物名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box {
                    OutlinedTextField(
                        value = petType.value,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("宠物类型") },
                        trailingIcon = {
                            IconButton(onClick = { petTypeExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = petTypeExpanded, onDismissRequest = { petTypeExpanded = false }) {
                        listOf("狗", "猫").forEach { t ->
                            DropdownMenuItem(text = { Text(t) }, onClick = {
                                petType.value = t
                                petTypeExpanded = false
                            })
                        }
                    }
                }
                OutlinedTextField(
                    value = startDate.value,
                    onValueChange = { startDate.value = it },
                    label = { Text("开始日期 (yyyy-MM-dd)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = endDate.value,
                    onValueChange = { endDate.value = it },
                    label = { Text("结束日期 (yyyy-MM-dd)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = specialRequests.value,
                    onValueChange = { specialRequests.value = it },
                    label = { Text("特殊要求") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        CreateFosterOrderRequest(
                            serviceId = service.id,
                            petName = petName.value,
                            petType = petType.value,
                            startDate = startDate.value,
                            endDate = endDate.value,
                            specialRequests = specialRequests.value.ifEmpty { null }
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE67E22))
            ) { Text("提交预约", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
fun ReviewDialog(
    order: FosterOrderItem,
    onDismiss: () -> Unit,
    onSubmit: (Int, String?) -> Unit
) {
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("评价寄养服务") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("请给服务打分", fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in 1..5) {
                        IconButton(onClick = { rating = i }) {
                            Icon(
                                if (i <= rating) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = null,
                                tint = if (i <= rating) Color(0xFFFFB800) else Color.Gray
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("评价内容") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (rating > 0) onSubmit(rating, comment.ifEmpty { null })
                },
                enabled = rating > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE67E22))
            ) { Text("提交", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

class FosterViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PawMatchRepository()

    private val _services = MutableStateFlow<List<FosterServiceItem>>(emptyList())
    val services: StateFlow<List<FosterServiceItem>> = _services.asStateFlow()

    private val _orders = MutableStateFlow<List<FosterOrderItem>>(emptyList())
    val orders: StateFlow<List<FosterOrderItem>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _resultMsg = MutableStateFlow("")
    val resultMsg: StateFlow<String> = _resultMsg.asStateFlow()

    fun loadServices(keyword: String? = null, petType: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.searchFosterServices(keyword = keyword, petType = petType).fold(
                onSuccess = { _services.value = it; _isLoading.value = false },
                onFailure = { _error.value = it.message; _isLoading.value = false }
            )
        }
    }

    fun loadOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.myFosterOrders().fold(
                onSuccess = { _orders.value = it; _isLoading.value = false },
                onFailure = { _isLoading.value = false }
            )
        }
    }

    fun createOrder(request: CreateFosterOrderRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.createFosterOrder(request).fold(
                onSuccess = { _resultMsg.value = "预约成功"; _isLoading.value = false },
                onFailure = { _resultMsg.value = it.message ?: "预约失败"; _isLoading.value = false }
            )
        }
    }

    fun cancelOrder(orderId: Long) {
        viewModelScope.launch {
            repo.cancelFosterOrder(orderId).fold(
                onSuccess = { _resultMsg.value = "取消成功"; loadOrders() },
                onFailure = { _resultMsg.value = it.message ?: "取消失败" }
            )
        }
    }

    fun reviewOrder(orderId: Long, rating: Int, comment: String?) {
        viewModelScope.launch {
            repo.reviewFosterOrder(orderId, ReviewRequest(rating, comment)).fold(
                onSuccess = { _resultMsg.value = "评价成功"; loadOrders() },
                onFailure = { _resultMsg.value = it.message ?: "评价失败" }
            )
        }
    }

    fun clearResultMsg() { _resultMsg.value = "" }
}
