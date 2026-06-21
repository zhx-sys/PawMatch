package com.pawmatch.app.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pawmatch.app.ui.theme.*
import com.pawmatch.app.viewmodel.WikiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WikiDetailScreen(
    entryId: Long,
    onBack: () -> Unit,
    viewModel: WikiViewModel = viewModel()
) {
    val entry by viewModel.selectedEntry.collectAsState()

    LaunchedEffect(entryId) {
        viewModel.loadEntryDetail(entryId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("词条详情", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (entry == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Accent)
            }
        } else {
            val e = entry!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // 标题
                Text(
                    e.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 元信息
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("浏览 ${e.viewCount}", fontSize = 12.sp, color = TextSecondary)
                    Text("有帮助 ${e.helpfulCount}", fontSize = 12.sp, color = TextSecondary)
                    e.createTime?.let {
                        Text(it.take(10), fontSize = 12.sp, color = TextSecondary)
                    }
                    e.categoryName?.let {
                        Text("分类：$it", fontSize = 12.sp, color = TextSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(modifier = Modifier.height(16.dp))

                // 摘要
                if (!e.summary.isNullOrBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F5F0)),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            e.summary,
                            fontSize = 14.sp,
                            color = TextPrimary,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 正文内容 - 使用 WebView 渲染 HTML
                if (!e.content.isNullOrBlank()) {
                    Text(
                        "正文",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val htmlContent = buildHtmlContent(e.content)
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                webViewClient = WebViewClient()
                                settings.javaScriptEnabled = false
                                settings.defaultTextEncodingName = "UTF-8"
                                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 200.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private fun buildHtmlContent(content: String): String {
    val escaped = content
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\n", "<br>")
    return """
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<style>
body {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    font-size: 15px;
    line-height: 1.8;
    color: #444444;
    padding: 0;
    margin: 0;
    word-wrap: break-word;
}
</style>
</head>
<body>$escaped</body>
</html>
""".trimIndent()
}