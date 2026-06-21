package com.pawmatch.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.pawmatch.app.data.model.CreatePetRequest
import com.pawmatch.app.data.model.Pet
import com.pawmatch.app.ui.theme.*
import com.pawmatch.app.viewmodel.ShelterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelterPetsManageScreen(
    onBack: () -> Unit,
    onPetClick: (Long) -> Unit,
    viewModel: ShelterViewModel = viewModel()
) {
    val pets by viewModel.shelterPets.collectAsState()
    val isLoading by viewModel.isPetsLoading.collectAsState()
    val createResult by viewModel.createPetResult.collectAsState()
    val deleteResult by viewModel.deletePetResult.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<Pet?>(null) }

    // 表单字段
    var name by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("狗") }
    var breed by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("公") }
    var age by remember { mutableStateOf("1") }
    var color by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("5.0") }
    var healthStatus by remember { mutableStateOf("健康") }
    var vaccinated by remember { mutableStateOf(true) }
    var sterilized by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var sizeLevel by remember { mutableStateOf("中型") }
    var activityLevel by remember { mutableStateOf("温顺安静") }
    var beginnerFriendly by remember { mutableStateOf(false) }
    var goodWithKids by remember { mutableStateOf(false) }
    var goodWithPets by remember { mutableStateOf(false) }

    // 图片选择相关状态
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var sizeLevelExpanded by remember { mutableStateOf(false) }
    var activityLevelExpanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedImageUri = it }
    }

    LaunchedEffect(Unit) { viewModel.loadShelterPets() }

    LaunchedEffect(createResult) {
        if (createResult == "success") {
            showAddDialog = false
            isSubmitting = false
            resetForm()
            viewModel.clearCreatePetResult()
        }
    }

    LaunchedEffect(deleteResult) {
        if (deleteResult == "success") {
            showDeleteConfirm = null
            viewModel.clearDeletePetResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("宠物管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("发布新宠物")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            if (pets.isEmpty() && !isLoading) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Pets, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("还没有发布宠物", color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Text("发布第一只宠物")
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pets) { pet ->
                        PetManageCard(
                            pet = pet,
                            onClick = { onPetClick(pet.id) },
                            onDelete = { showDeleteConfirm = pet }
                        )
                    }
                }
            }
        }
    }

    // 发布新宠物弹窗
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("发布新宠物", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier.height(450.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 图片选择区域
                    item {
                        Text("宠物照片（仅限一张）", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 已选图片缩略图预览
                            if (selectedImageUri != null) {
                                Box {
                                    AsyncImage(
                                        model = selectedImageUri,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { selectedImageUri = null },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(20.dp)
                                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(topEnd = 8.dp))
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "删除",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            } else {
                                // 添加图片按钮（未选择时显示）
                                Surface(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clickable { imagePickerLauncher.launch("image/*") },
                                    shape = RoundedCornerShape(8.dp),
                                    color = Surface,
                                    border = ButtonDefaults.outlinedButtonBorder
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Add, contentDescription = "添加照片", tint = Accent)
                                    }
                                }
                            }
                        }
                    }

                    item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("名称") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PetTypeChip("狗", petType) { petType = it }
                            PetTypeChip("猫", petType) { petType = it }
                            PetTypeChip("其他", petType) { petType = it }
                        }
                    }
                    item { OutlinedTextField(value = breed, onValueChange = { breed = it }, label = { Text("品种") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FilterChip(selected = gender == "公", onClick = { gender = "公" }, label = { Text("公") }, modifier = Modifier.weight(1f))
                            FilterChip(selected = gender == "母", onClick = { gender = "母" }, label = { Text("母") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item { OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("年龄（岁）") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true) }
                    item { OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text("颜色") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                    item { OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("体重(kg)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true) }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FilterChip(selected = healthStatus == "健康", onClick = { healthStatus = "健康" }, label = { Text("健康") })
                            FilterChip(selected = healthStatus == "亚健康", onClick = { healthStatus = "亚健康" }, label = { Text("亚健康") })
                            FilterChip(selected = healthStatus == "生病", onClick = { healthStatus = "生病" }, label = { Text("生病") })
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) { Text("疫苗", fontSize = 13.sp); Spacer(Modifier.width(8.dp)); Switch(checked = vaccinated, onCheckedChange = { vaccinated = it }) }
                            Row(verticalAlignment = Alignment.CenterVertically) { Text("绝育", fontSize = 13.sp); Spacer(Modifier.width(8.dp)); Switch(checked = sterilized, onCheckedChange = { sterilized = it }) }
                        }
                    }
                    item {
                        ExposedDropdownMenuBox(
                            expanded = sizeLevelExpanded,
                            onExpandedChange = { sizeLevelExpanded = !sizeLevelExpanded }
                        ) {
                            OutlinedTextField(
                                value = sizeLevel,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("体型") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sizeLevelExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = sizeLevelExpanded,
                                onDismissRequest = { sizeLevelExpanded = false }
                            ) {
                                listOf("小型", "中型", "大型").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            sizeLevel = option
                                            sizeLevelExpanded = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }
                    }
                    item {
                        ExposedDropdownMenuBox(
                            expanded = activityLevelExpanded,
                            onExpandedChange = { activityLevelExpanded = !activityLevelExpanded }
                        ) {
                            OutlinedTextField(
                                value = activityLevel,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("活跃程度") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = activityLevelExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = activityLevelExpanded,
                                onDismissRequest = { activityLevelExpanded = false }
                            ) {
                                listOf("活泼好动", "温顺安静", "粘人精", "独立自主").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            activityLevel = option
                                            activityLevelExpanded = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) { Text("适合新手", fontSize = 13.sp); Spacer(Modifier.width(8.dp)); Switch(checked = beginnerFriendly, onCheckedChange = { beginnerFriendly = it }) }
                            Row(verticalAlignment = Alignment.CenterVertically) { Text("适合儿童", fontSize = 13.sp); Spacer(Modifier.width(8.dp)); Switch(checked = goodWithKids, onCheckedChange = { goodWithKids = it }) }
                            Row(verticalAlignment = Alignment.CenterVertically) { Text("适合多宠", fontSize = 13.sp); Spacer(Modifier.width(8.dp)); Switch(checked = goodWithPets, onCheckedChange = { goodWithPets = it }) }
                        }
                    }
                    item { OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("描述") }, modifier = Modifier.fillMaxWidth(), maxLines = 3) }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isSubmitting = true
                        val request = CreatePetRequest(
                            name = name,
                            type = petType,
                            breed = breed,
                            gender = gender,
                            age = age.toIntOrNull() ?: 1,
                            color = color,
                            weight = weight.toDoubleOrNull() ?: 5.0,
                            healthStatus = healthStatus,
                            vaccinated = vaccinated,
                            sterilized = sterilized,
                            description = description,
                            sizeLevel = sizeLevel,
                            activityLevel = activityLevel,
                            beginnerFriendly = beginnerFriendly,
                            goodWithKids = goodWithKids,
                            goodWithPets = goodWithPets,
                            images = emptyList()
                        )
                        if (selectedImageUri != null) {
                            viewModel.uploadImage(
                                uri = selectedImageUri!!,
                                onSuccess = { url ->
                                    viewModel.createPet(request.copy(images = listOf(url)))
                                },
                                onError = { _ ->
                                    isSubmitting = false
                                }
                            )
                        } else {
                            viewModel.createPet(request)
                        }
                    },
                    enabled = name.isNotBlank() && !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("发布")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; resetForm() }) { Text("取消") }
            }
        )
    }

    // 删除确认弹窗
    showDeleteConfirm?.let { pet ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除「${pet.name}」吗？此操作不可撤销。") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deletePet(pet.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("取消") }
            }
        )
    }

    // 错误提示
    LaunchedEffect(createResult) {
        val result = createResult
        if (result != null && result != "success") {
            isSubmitting = false
            snackbarHostState.showSnackbar(result, duration = SnackbarDuration.Short)
            viewModel.clearCreatePetResult()
        }
    }
}

@Composable
private fun PetTypeChip(label: String, selected: String, onClick: (String) -> Unit) {
    FilterChip(
        selected = selected == label,
        onClick = { onClick(label) },
        label = { Text(label) }
    )
}

@Composable
private fun PetManageCard(pet: Pet, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 宠物照片区域
            val firstImageUrl = pet.firstImageUrl()
            if (firstImageUrl != null) {
                AsyncImage(
                    model = firstImageUrl,
                    contentDescription = "${pet.name}的照片",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = Accent.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Pets,
                            contentDescription = null,
                            tint = Accent,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(pet.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "${pet.species} · ${pet.breed} · ${pet.age}岁 · ${pet.gender}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            if (pet.status == 0) "待领养" else "已领养",
                            fontSize = 11.sp
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (pet.status == 0) HealthYellow.copy(alpha = 0.15f) else HealthGreen.copy(alpha = 0.1f)
                    )
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = Error)
            }
        }
    }
}

private fun resetForm() {
    // 由 remember 管理的状态在 Dialog 关闭后重新打开时会重置
}