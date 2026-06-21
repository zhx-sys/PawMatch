package com.pawmatch.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pawmatch.app.data.model.Conversation
import com.pawmatch.app.data.model.Message
import com.pawmatch.app.ui.theme.*
import com.pawmatch.app.viewmodel.MessageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    onBack: () -> Unit,
    viewModel: MessageViewModel = viewModel()
) {
    val conversations by viewModel.conversations.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val currentChat by viewModel.currentChat.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadConversations() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentChat?.displayName ?: "消息") },
                navigationIcon = {
                    if (currentChat != null) {
                        IconButton(onClick = { viewModel.closeChat() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                        }
                    } else {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (currentChat != null) {
            // 聊天详情
            ChatDetailView(
                messages = messages,
                myId = viewModel.myId,
                onSend = { viewModel.sendMessage(it) },
                modifier = Modifier.padding(padding)
            )
        } else {
            // 会话列表
            LazyColumn(modifier = Modifier.padding(padding)) {
                if (conversations.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) {
                            Text("暂无消息", color = TextSecondary)
                        }
                    }
                }
                items(conversations) { conv ->
                    ConversationItem(
                        conv = conv,
                        onClick = { viewModel.openChat(conv.otherUserId, conv.otherUserType, conv.nickname) }
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationItem(conv: Conversation, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = if (conv.unread) PrimaryLight.copy(alpha = 0.3f) else Surface
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (conv.otherUserType == 1) Accent else Primary,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        (conv.nickname ?: "U").take(1),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(conv.nickname ?: "用户${conv.otherUserId}", fontWeight = FontWeight.Medium)
                    if (conv.unread) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(color = Accent, shape = MaterialTheme.shapes.extraLarge) {
                            Box(modifier = Modifier.size(8.dp))
                        }
                    }
                }
                conv.lastContent?.let {
                    Text(it, fontSize = 13.sp, color = TextSecondary, maxLines = 1)
                }
            }
        }
    }
    HorizontalDivider()
}

@Composable
fun ChatDetailView(
    messages: List<Message>,
    myId: Long,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).padding(8.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { msg ->
                val isSelf = msg.fromUserId == myId
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        color = if (isSelf) Primary else Color(0xFFF0F0F0),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Text(
                            msg.content,
                            modifier = Modifier.padding(10.dp),
                            color = if (isSelf) Color.White else TextPrimary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
        // 输入栏
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("输入消息...") },
                modifier = Modifier.weight(1f),
                singleLine = false,
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSend(inputText.trim())
                        inputText = ""
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "发送", tint = Primary)
            }
        }
    }
}
