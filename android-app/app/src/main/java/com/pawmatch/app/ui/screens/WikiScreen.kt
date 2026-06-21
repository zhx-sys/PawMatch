package com.pawmatch.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
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
import com.pawmatch.app.data.model.WikiCategory
import com.pawmatch.app.data.model.WikiEntryItem
import com.pawmatch.app.ui.theme.*
import com.pawmatch.app.viewmodel.WikiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WikiScreen(
    onBack: () -> Unit,
    onEntryClick: (Long) -> Unit,
    onCreateClick: () -> Unit,
    viewModel: WikiViewModel = viewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val entries by viewModel.entries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentCategoryName by viewModel.currentCategoryName.collectAsState()
    val keyword by viewModel.keyword.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()

    var searchText by remember { mutableStateOf(keyword) }
    var showSortMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadCategories()
        viewModel.loadEntries(refresh = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("百科", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "排序",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("最新发布") },
                                onClick = {
                                    viewModel.updateSortBy("newest")
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("最有帮助") },
                                onClick = {
                                    viewModel.updateSortBy("helpful")
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
        ) {
            // 左侧分类树
            CategorySidebar(
                categories = categories,
                currentCategoryId = viewModel.currentCategoryId.collectAsState().value,
                onCategoryClick = { catId, name ->
                    viewModel.selectCategory(catId, name)
                },
                modifier = Modifier
                    .width(140.dp)
                    .fillMaxHeight()
            )

            // 右侧词条列表
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // 搜索栏 + 分类标题
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        currentCategoryName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        viewModel.search(it)
                    },
                    placeholder = { Text("搜索词条...", fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Color(0xFFDDDDDD)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading && entries.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Accent)
                    }
                } else if (entries.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无词条", color = TextSecondary, fontSize = 15.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(entries, key = { it.id }) { entry ->
                            EntryCard(entry = entry, onClick = { onEntryClick(entry.id) })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        FloatingActionButton(
            onClick = onCreateClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Accent,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "创建词条")
        }
        }
    }
}

@Composable
private fun CategorySidebar(
    categories: List<WikiCategory>,
    currentCategoryId: Long?,
    onCategoryClick: (Long?, String) -> Unit,
    modifier: Modifier = Modifier
) {
    data class FlatItem(val id: Long?, val name: String, val depth: Int, val isRoot: Boolean)

    val flatItems = remember(categories) {
        val list = mutableListOf<FlatItem>()
        list.add(FlatItem(null, "全部词条", 0, true))
        fun flatten(cats: List<WikiCategory>, depth: Int) {
            cats.forEach { cat ->
                list.add(FlatItem(cat.id, cat.name, depth, false))
                flatten(cat.children, depth + 1)
            }
        }
        flatten(categories, 0)
        list
    }

    Surface(
        modifier = modifier,
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        LazyColumn(contentPadding = PaddingValues(vertical = 12.dp)) {
            items(flatItems, key = { "${it.id}_${it.depth}" }) { item ->
                CategoryItem(
                    name = item.name,
                    isSelected = currentCategoryId == item.id,
                    isRoot = item.isRoot,
                    onClick = { onCategoryClick(item.id, item.name) },
                    paddingStart = (12 + item.depth * 16).dp
                )
            }
        }
    }
}

@Composable
private fun CategoryItem(
    name: String,
    isSelected: Boolean,
    isRoot: Boolean,
    onClick: () -> Unit,
    paddingStart: androidx.compose.ui.unit.Dp = 12.dp
) {
    val bgColor = if (isSelected) Accent.copy(alpha = 0.1f) else Color.Transparent
    val textColor = if (isSelected) Accent else TextPrimary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(bgColor)
            .padding(start = paddingStart, top = 10.dp, bottom = 10.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            name,
            fontSize = if (isRoot) 14.sp else 13.sp,
            fontWeight = if (isRoot) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
            maxLines = 1
        )
    }
}

@Composable
private fun EntryCard(entry: WikiEntryItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                entry.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (!entry.summary.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    entry.summary,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                entry.categoryName?.let {
                    AssistChip(
                        onClick = {},
                        label = { Text(it, fontSize = 10.sp) },
                        modifier = Modifier.height(22.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Accent.copy(alpha = 0.08f)
                        )
                    )
                }
                Text(
                    entry.authorName ?: "",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Text(
                    "浏览 ${entry.viewCount}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Text(
                    "有帮助 ${entry.helpfulCount}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                entry.createTime?.let {
                    Text(
                        it.take(10),
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}