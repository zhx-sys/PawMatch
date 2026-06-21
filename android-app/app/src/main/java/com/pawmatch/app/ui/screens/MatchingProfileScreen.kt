package com.pawmatch.app.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pawmatch.app.data.model.MatchingProfileData
import com.pawmatch.app.data.model.MatchingProfileRequest
import com.pawmatch.app.data.repository.PawMatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchingProfileScreen(
    onBack: () -> Unit,
    viewModel: MatchingProfileViewModel = viewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val resultMsg by viewModel.resultMsg.collectAsState()
    val editMode by viewModel.editMode.collectAsState()

    val livingSpaceOptions = listOf("公寓", "普通住宅", "大房子", "带院子")
    val experienceOptions = listOf("新手", "有经验", "资深")
    val scheduleOptions = listOf("朝九晚五", "自由职业", "居家")
    val budgetOptions = listOf("低（<300/月）", "中（300-800/月）", "高（>800/月）")
    val preferenceTags = listOf("温顺", "活泼", "独立", "小型犬", "大型犬", "猫咪", "其他")

    val livingSpace by viewModel.draftLivingSpace.collectAsState()
    val hasChildren by viewModel.draftHasChildren.collectAsState()
    val hasOtherPets by viewModel.draftHasOtherPets.collectAsState()
    val petExperience by viewModel.draftPetExperience.collectAsState()
    val dailyRoutine by viewModel.draftDailyRoutine.collectAsState()
    val budgetRange by viewModel.draftBudgetRange.collectAsState()
    val petPreference by viewModel.draftPetPreference.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("匹配画像") },
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
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFE67E22))
            }
        } else if (!editMode && profile != null && profile!!.matchingProfileComplete) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("当前匹配画像", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFE67E22))
                        HorizontalDivider()
                        ProfileItem("居住空间", profile?.livingSpace)
                        ProfileItem("家中有无儿童", if (profile?.hasChildren == true) "有" else "无")
                        ProfileItem("家中已有宠物", if (profile?.hasOtherPets == true) "有" else "无")
                        ProfileItem("养宠经验", profile?.petExperience)
                        ProfileItem("作息规律", profile?.dailyRoutine)
                        ProfileItem("月度预算", profile?.budgetRange)
                        ProfileItem("宠物偏好", profile?.petPreference?.replace(",", ", "))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.enterEditMode() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE67E22)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("重新填写", color = Color.White, fontSize = 16.sp)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("请填写以下信息，帮助系统为您精准匹配宠物", fontSize = 14.sp, color = Color.Gray)

                // 居住空间
                DropdownField("居住空间", livingSpace, livingSpaceOptions) { viewModel.setLivingSpace(it) }

                // 家中有无儿童
                Text("家中有无儿童", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterChip(
                        selected = !hasChildren,
                        onClick = { viewModel.setHasChildren(false) },
                        label = { Text("无") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE67E22).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFFE67E22)
                        )
                    )
                    FilterChip(
                        selected = hasChildren,
                        onClick = { viewModel.setHasChildren(true) },
                        label = { Text("有") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE67E22).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFFE67E22)
                        )
                    )
                }

                // 家中已有宠物
                Text("家中已有宠物", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterChip(
                        selected = !hasOtherPets,
                        onClick = { viewModel.setHasOtherPets(false) },
                        label = { Text("无") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE67E22).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFFE67E22)
                        )
                    )
                    FilterChip(
                        selected = hasOtherPets,
                        onClick = { viewModel.setHasOtherPets(true) },
                        label = { Text("有") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE67E22).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFFE67E22)
                        )
                    )
                }

                // 养宠经验
                DropdownField("养宠经验", petExperience, experienceOptions) { viewModel.setPetExperience(it) }

                // 作息规律
                DropdownField("作息规律", dailyRoutine, scheduleOptions) { viewModel.setDailyRoutine(it) }

                // 月度预算
                DropdownField("月度预算", budgetRange, budgetOptions) { viewModel.setBudgetRange(it) }

                // 宠物偏好
                Text("宠物偏好", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Column {
                    val rows = preferenceTags.chunked(3)
                    rows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { tag ->
                                val selected = tag in petPreference
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        viewModel.setPetPreference(if (selected) petPreference - tag else petPreference + tag)
                                    },
                                    label = { Text(tag, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFE67E22).copy(alpha = 0.2f),
                                        selectedLabelColor = Color(0xFFE67E22)
                                    )
                                )
                            }
                            // fill remaining space
                            repeat(3 - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.saveProfile(
                            MatchingProfileRequest(
                                livingSpace = livingSpace.ifEmpty { null },
                                hasChildren = hasChildren,
                                hasOtherPets = hasOtherPets,
                                petExperience = petExperience.ifEmpty { null },
                                dailyRoutine = dailyRoutine.ifEmpty { null },
                                budgetRange = budgetRange.ifEmpty { null },
                                petPreference = petPreference.joinToString(",").ifEmpty { null }
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE67E22)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("提交匹配画像", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ProfileItem(label: String, value: String?) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text("$label：", fontSize = 14.sp, color = Color.Gray)
        Text(value ?: "未填写", fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    currentValue: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = currentValue,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                placeholder = { Text("请选择$label") }
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

class MatchingProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PawMatchRepository()

    private val _profile = MutableStateFlow<MatchingProfileData?>(null)
    val profile: StateFlow<MatchingProfileData?> = _profile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _resultMsg = MutableStateFlow("")
    val resultMsg: StateFlow<String> = _resultMsg.asStateFlow()

    private val _editMode = MutableStateFlow(false)
    val editMode: StateFlow<Boolean> = _editMode.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getMatchingProfile().fold(
                onSuccess = {
                    _profile.value = it
                    _editMode.value = !(it?.matchingProfileComplete ?: false)
                    if (it != null && it.matchingProfileComplete) {
                        _draftLivingSpace.value = it.livingSpace ?: ""
                        _draftHasChildren.value = it.hasChildren
                        _draftHasOtherPets.value = it.hasOtherPets
                        _draftPetExperience.value = it.petExperience ?: ""
                        _draftDailyRoutine.value = it.dailyRoutine ?: ""
                        _draftBudgetRange.value = it.budgetRange ?: ""
                        _draftPetPreference.value = it.petPreference?.split(",")?.map { s -> s.trim() }?.toSet() ?: emptySet()
                    }
                    _isLoading.value = false
                },
                onFailure = {
                    _editMode.value = true
                    _isLoading.value = false
                }
            )
        }
    }

    fun enterEditMode() { _editMode.value = true }

    fun saveProfile(request: MatchingProfileRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.saveMatchingProfile(request).fold(
                onSuccess = {
                    _resultMsg.value = "保存成功"
                    _editMode.value = false
                    _isLoading.value = false
                    loadProfile()
                },
                onFailure = {
                    _resultMsg.value = it.message ?: "保存失败"
                    _isLoading.value = false
                }
            )
        }
    }

    fun clearResultMsg() { _resultMsg.value = "" }

    // ===== 草稿状态（跨页面保持） =====
    private val _draftLivingSpace = MutableStateFlow("")
    val draftLivingSpace: StateFlow<String> = _draftLivingSpace.asStateFlow()
    private val _draftHasChildren = MutableStateFlow(false)
    val draftHasChildren: StateFlow<Boolean> = _draftHasChildren.asStateFlow()
    private val _draftHasOtherPets = MutableStateFlow(false)
    val draftHasOtherPets: StateFlow<Boolean> = _draftHasOtherPets.asStateFlow()
    private val _draftPetExperience = MutableStateFlow("")
    val draftPetExperience: StateFlow<String> = _draftPetExperience.asStateFlow()
    private val _draftDailyRoutine = MutableStateFlow("")
    val draftDailyRoutine: StateFlow<String> = _draftDailyRoutine.asStateFlow()
    private val _draftBudgetRange = MutableStateFlow("")
    val draftBudgetRange: StateFlow<String> = _draftBudgetRange.asStateFlow()
    private val _draftPetPreference = MutableStateFlow<Set<String>>(emptySet())
    val draftPetPreference: StateFlow<Set<String>> = _draftPetPreference.asStateFlow()

    fun setLivingSpace(v: String) { _draftLivingSpace.value = v }
    fun setHasChildren(v: Boolean) { _draftHasChildren.value = v }
    fun setHasOtherPets(v: Boolean) { _draftHasOtherPets.value = v }
    fun setPetExperience(v: String) { _draftPetExperience.value = v }
    fun setDailyRoutine(v: String) { _draftDailyRoutine.value = v }
    fun setBudgetRange(v: String) { _draftBudgetRange.value = v }
    fun setPetPreference(v: Set<String>) { _draftPetPreference.value = v }
}
