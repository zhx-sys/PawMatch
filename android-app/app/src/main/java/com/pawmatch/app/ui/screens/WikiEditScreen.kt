package com.pawmatch.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pawmatch.app.data.model.WikiCategory
import com.pawmatch.app.viewmodel.WikiEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WikiEditScreen(entryId: Long?, onBack: () -> Unit, onSubmitSuccess: (Long?) -> Unit) {
    val vm: WikiEditViewModel = viewModel()
    val title by vm.title.collectAsState()
    val content by vm.content.collectAsState()
    val summary by vm.summary.collectAsState()
    val categoryId by vm.categoryId.collectAsState()
    val editSummary by vm.editSummary.collectAsState()
    val categories by vm.categories.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val isSubmitting by vm.isSubmitting.collectAsState()
    val error by vm.error.collectAsState()
    val submitSuccess by vm.submitSuccess.collectAsState()
    val submittedEntryId by vm.submittedEntryId.collectAsState()

    LaunchedEffect(entryId) { vm.init(entryId) }

    LaunchedEffect(submitSuccess) {
        if (submitSuccess) {
            val id = submittedEntryId
            vm.resetSubmitSuccess()
            onSubmitSuccess(id)
        }
    }

    // 分类级联选择
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var selectedCatIds by remember { mutableStateOf<List<Long>>(emptyList()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (entryId != null) "编辑词条" else "创建词条") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFFDF8F0)
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFE67E22))
            }
            return@Scaffold
        }

        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState())
        ) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = MaterialTheme.shapes.medium) {
                Column(Modifier.padding(20.dp)) {
                    // 标题
                    Text("标题（必填）", fontSize = 14.sp, color = Color.Gray)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(value = title, onValueChange = { vm.updateTitle(it) },
                        placeholder = { Text("词条标题") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { Text("${title.length}/100") })
                    Spacer(Modifier.height(12.dp))

                    // 分类
                    Text("分类", fontSize = 14.sp, color = Color.Gray)
                    Spacer(Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = showCategoryDropdown,
                        onExpandedChange = { showCategoryDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = getCategoryPath(categories, categoryId),
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                            placeholder = { Text("选择分类") }
                        )
                        ExposedDropdownMenu(expanded = showCategoryDropdown, onDismissRequest = { showCategoryDropdown = false }) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = { vm.updateCategoryId(cat.id); showCategoryDropdown = false }
                                )
                                cat.children?.forEach { child ->
                                    DropdownMenuItem(
                                        text = { Text("  ${child.name}") },
                                        onClick = { vm.updateCategoryId(child.id); showCategoryDropdown = false }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // 摘要
                    Text("摘要（选填）", fontSize = 14.sp, color = Color.Gray)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(value = summary, onValueChange = { vm.updateSummary(it) },
                        placeholder = { Text("简要描述") }, modifier = Modifier.fillMaxWidth(),
                        maxLines = 3, supportingText = { Text("${summary.length}/500") })
                    Spacer(Modifier.height(12.dp))

                    // 内容
                    Text("内容（必填）", fontSize = 14.sp, color = Color.Gray)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(value = content, onValueChange = { vm.updateContent(it) },
                        placeholder = { Text("词条详细内容") }, modifier = Modifier.fillMaxWidth(),
                        minLines = 8, maxLines = 16)
                    Spacer(Modifier.height(12.dp))

                    // 编辑模式下显示编辑说明
                    if (entryId != null) {
                        Text("编辑说明（选填）", fontSize = 14.sp, color = Color.Gray)
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(value = editSummary, onValueChange = { vm.updateEditSummary(it) },
                            placeholder = { Text("本次修改说明") }, modifier = Modifier.fillMaxWidth(),
                            singleLine = true, supportingText = { Text("${editSummary.length}/200") })
                        Spacer(Modifier.height(12.dp))
                    }

                    error?.let {
                        Text(it, color = Color.Red, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)) {
                        OutlinedButton(onClick = onBack) { Text("取消") }
                        Button(
                            onClick = { vm.submit() },
                            enabled = title.isNotBlank() && content.isNotBlank() && !isSubmitting,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE67E22))
                        ) {
                            if (isSubmitting) CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            else Text(if (entryId != null) "保存修改" else "提交词条")
                        }
                    }
                }
            }
        }
    }
}

fun getCategoryPath(categories: List<WikiCategory>, targetId: Long?): String {
    if (targetId == null) return ""
    for (cat in categories) {
        if (cat.id == targetId) return cat.name
        cat.children?.forEach { child ->
            if (child.id == targetId) return "${cat.name} > ${child.name}"
        }
    }
    return ""
}
