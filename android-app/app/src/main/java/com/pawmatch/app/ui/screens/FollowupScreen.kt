package com.pawmatch.app.ui.screens

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.pawmatch.app.data.api.ServerConfigManager
import com.pawmatch.app.data.api.TokenManager
import com.pawmatch.app.data.model.FollowupItem
import com.pawmatch.app.data.model.AdoptionApplication
import com.pawmatch.app.data.model.FollowupRequest
import com.pawmatch.app.data.repository.PawMatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowupScreen(
    onBack: () -> Unit,
    viewModel: FollowupViewModel = viewModel()
) {
    val context = LocalContext.current
    val followups by viewModel.followups.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val resultMsg by viewModel.resultMsg.collectAsState()
    val completedAdoptions by viewModel.completedAdoptions.collectAsState()
    val isShelter = viewModel.isShelter

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedAdoption by remember { mutableStateOf<AdoptionApplication?>(null) }
    var contentText by remember { mutableStateOf("") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImageUris = (selectedImageUris + uris).take(9)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadFollowups()
    }

    if (resultMsg.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { viewModel.clearResultMsg() },
            title = { Text("提示") },
            text = { Text(resultMsg) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearResultMsg() }) { Text("确定") }
            }
        )
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isUploading) {
                    showCreateDialog = false
                    selectedAdoption = null
                    contentText = ""
                    selectedImageUris = emptyList()
                }
            },
            title = { Text("发布回访") },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("选择领养记录", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    if (completedAdoptions.isEmpty()) {
                        Text("暂无可回访的已领养记录", fontSize = 13.sp, color = Color.Gray)
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 120.dp)
                        ) {
                            items(completedAdoptions) { adoption ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedAdoption = adoption },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedAdoption?.id == adoption.id,
                                        onClick = { selectedAdoption = adoption }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "${adoption.petName ?: "宠物"} (#${adoption.id})",
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = contentText,
                        onValueChange = { contentText = it },
                        label = { Text("回访内容") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )

                    // 图片选择
                    Text("上传照片（最多9张）", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedImageUris.forEachIndexed { index, uri ->
                            Box(modifier = Modifier.size(72.dp)) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "图片 ${index + 1}",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = {
                                        selectedImageUris = selectedImageUris.toMutableList().also { it.removeAt(index) }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(20.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(bottomStart = 8.dp))
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "删除",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                        if (selectedImageUris.size < 9) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "添加图片",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    if (isUploading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFFE67E22)
                            )
                            Text("正在上传图片...", fontSize = 12.sp, color = Color(0xFFE67E22))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val adoption = selectedAdoption
                        if (adoption != null && contentText.isNotBlank()) {
                            viewModel.createFollowupWithImages(
                                context = context,
                                adoptionId = adoption.id,
                                content = contentText,
                                imageUris = selectedImageUris,
                                onSuccess = {
                                    showCreateDialog = false
                                    selectedAdoption = null
                                    contentText = ""
                                    selectedImageUris = emptyList()
                                }
                            )
                        }
                    },
                    enabled = selectedAdoption != null && contentText.isNotBlank() && !isUploading
                ) {
                    Text(if (isUploading) "上传中..." else "发布")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!isUploading) {
                            showCreateDialog = false
                            selectedAdoption = null
                            contentText = ""
                            selectedImageUris = emptyList()
                        }
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isShelter) "回访记录" else "领养回访") },
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
        },
        floatingActionButton = {
            // 救助站只读，不显示发布按钮
            if (!isShelter) {
                FloatingActionButton(
                    onClick = {
                        viewModel.loadCompletedAdoptions()
                        showCreateDialog = true
                    },
                    containerColor = Color(0xFFE67E22)
                ) {
                    Text("发布", color = Color.White, fontSize = 13.sp)
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFE67E22))
            }
        } else if (followups.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无回访记录", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(followups) { followup ->
                    FollowupCard(followup)
                }
            }
        }
    }
}

@Composable
fun FollowupCard(followup: FollowupItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 救助站视图额外显示领养记录 ID
            val isShelter = TokenManager.userType == 1
            if (isShelter && followup.adoptionId > 0) {
                Text(
                    "${followup.userName ?: "用户"} · ${followup.petName ?: "宠物"}",
                    fontSize = 12.sp,
                    color = Color(0xFFE67E22),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                followup.content ?: "",
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
            val images = followup.images
            if (!images.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                val imageList = images.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                if (imageList.isNotEmpty()) {
                    val baseUrl = ServerConfigManager.serverUrl
                        .removeSuffix("/").removeSuffix("/api")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        imageList.take(3).forEach { url ->
                            AsyncImage(
                                model = if (url.startsWith("/")) "$baseUrl$url" else url,
                                contentDescription = "回访图片",
                                modifier = Modifier.size(80.dp).padding(end = 4.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                followup.createTime ?: "",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

class FollowupViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PawMatchRepository()
    val isShelter: Boolean = TokenManager.userType == 1

    private val _followups = MutableStateFlow<List<FollowupItem>>(emptyList())
    val followups: StateFlow<List<FollowupItem>> = _followups.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _resultMsg = MutableStateFlow("")
    val resultMsg: StateFlow<String> = _resultMsg.asStateFlow()

    private val _completedAdoptions = MutableStateFlow<List<AdoptionApplication>>(emptyList())
    val completedAdoptions: StateFlow<List<AdoptionApplication>> = _completedAdoptions.asStateFlow()

    fun loadFollowups() {
        viewModelScope.launch {
            _isLoading.value = true
            if (isShelter) {
                // 救助站：通过 /followup/shelter/{shelterId} 加载
                repo.getFollowupsByShelter(TokenManager.userId).fold(
                    onSuccess = { list ->
                        _followups.value = list.sortedByDescending { it.createTime }
                        _isLoading.value = false
                    },
                    onFailure = {
                        _isLoading.value = false
                    }
                )
            } else {
                // 普通用户：遍历已完成的领养记录
                repo.myAdoptions().fold(
                    onSuccess = { adoptions ->
                        val completed = adoptions.filter { it.status == 3 }
                        _completedAdoptions.value = completed
                        val allFollowups = mutableListOf<FollowupItem>()
                        for (adoption in completed) {
                            repo.getFollowupsByAdoption(adoption.id).fold(
                                onSuccess = { list -> allFollowups.addAll(list) },
                                onFailure = { }
                            )
                        }
                        _followups.value = allFollowups.sortedByDescending { it.createTime }
                        _isLoading.value = false
                    },
                    onFailure = {
                        _isLoading.value = false
                    }
                )
            }
        }
    }

    fun loadCompletedAdoptions() {
        viewModelScope.launch {
            repo.myAdoptions().fold(
                onSuccess = { adoptions ->
                    _completedAdoptions.value = adoptions.filter { it.status == 3 }
                },
                onFailure = { }
            )
        }
    }

    fun createFollowupWithImages(
        context: android.content.Context,
        adoptionId: Long,
        content: String,
        imageUris: List<Uri>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            val uploadedUrls = mutableListOf<String>()

            for (uri in imageUris) {
                try {
                    val file = uriToFile(context, uri)
                    repo.uploadImage(file).fold(
                        onSuccess = { url -> uploadedUrls.add(url) },
                        onFailure = {
                            _resultMsg.value = "图片上传失败: ${it.message}"
                            _isUploading.value = false
                            return@launch
                        }
                    )
                } catch (e: Exception) {
                    _resultMsg.value = "图片处理失败: ${e.message}"
                    _isUploading.value = false
                    return@launch
                }
            }

            val request = FollowupRequest(
                adoptionId = adoptionId,
                userId = TokenManager.userId,
                content = content,
                images = if (uploadedUrls.isNotEmpty()) uploadedUrls.joinToString(",") else null
            )

            repo.createFollowup(request).fold(
                onSuccess = {
                    _resultMsg.value = "发布成功"
                    _isUploading.value = false
                    onSuccess()
                    loadFollowups()
                },
                onFailure = {
                    _resultMsg.value = it.message ?: "发布失败"
                    _isUploading.value = false
                }
            )
        }
    }

    private fun uriToFile(context: android.content.Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("无法读取文件")
        val fileName = runCatching {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                if (nameIndex >= 0) cursor.getString(nameIndex) else "upload_${System.currentTimeMillis()}.jpg"
            }
        }.getOrNull() ?: "upload_${System.currentTimeMillis()}.jpg"

        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { output ->
            inputStream.copyTo(output)
        }
        inputStream.close()
        return file
    }

    fun clearResultMsg() { _resultMsg.value = "" }
}
