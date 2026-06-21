package com.pawmatch.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.pawmatch.app.data.model.Pet
import com.pawmatch.app.ui.theme.*
import com.pawmatch.app.viewmodel.PetGameViewModel
import kotlinx.coroutines.*
import kotlin.math.*

// 宠物动画状态
class PetSprite(
    val pet: Pet,
    var x: Float = 0f,
    var y: Float = 0f,
    var dx: Float = 0f,
    var dy: Float = 0f,
    val size: Float = 60f,
    val bobPhase: Float = (Math.random() * Math.PI * 2).toFloat()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetGameScreen(
    onBack: () -> Unit,
    onPetClick: (Long) -> Unit,
    viewModel: PetGameViewModel = viewModel()
) {
    val pets by viewModel.pets.collectAsState()
    val selectedPet by viewModel.selectedPet.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val applying by viewModel.applying.collectAsState()
    val applySuccess by viewModel.applySuccess.collectAsState()

    val context = LocalContext.current

    // 动画状态
    val sprites = remember { mutableStateListOf<PetSprite>() }
    val petBitmapCache = remember { mutableMapOf<Long, ImageBitmap>() }
    val pendingLoads = remember { mutableSetOf<Long>() }
    var showApplyDialog by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var housingCondition by remember { mutableStateOf("") }
    val textMeasurer = rememberTextMeasurer()

    // 加载宠物
    LaunchedEffect(Unit) { viewModel.loadPets() }

    // 初始化精灵
    LaunchedEffect(pets) {
        if (pets.isNotEmpty() && sprites.isEmpty()) {
            sprites.clear()
            pets.forEach { p -> sprites.add(PetSprite(p)) }
            spreadSprites(sprites)
        }
    }

    // 预加载宠物位图（在 Canvas 外部触发）
    LaunchedEffect(sprites.toList()) {
        for (sprite in sprites) {
            val id = sprite.pet.id
            if (petBitmapCache.containsKey(id) || pendingLoads.contains(id)) continue
            pendingLoads.add(id)
            launch {
                val url = sprite.pet.firstImageUrl() ?: return@launch
                try {
                    val loader = context.imageLoader
                    val request = ImageRequest.Builder(context)
                        .data(url)
                        .size(180)
                        .build()
                    val result = loader.execute(request)
                    val drawable = result.drawable
                    if (drawable is android.graphics.drawable.BitmapDrawable) {
                        petBitmapCache[id] = drawable.bitmap.asImageBitmap()
                    }
                } catch (_: Exception) {}
                pendingLoads.remove(id)
            }
        }
    }

    // 动画循环
    val animFrame = remember { mutableStateOf(0L) }
    LaunchedEffect(sprites.size) {
        if (sprites.isEmpty()) return@LaunchedEffect
        while (isActive) {
            withFrameMillis { frameTimeMillis ->
                animFrame.value = frameTimeMillis
                updateSprites(sprites)
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // 成功提示
    LaunchedEffect(applySuccess) {
        if (applySuccess) {
            showApplyDialog = false
            reason = ""; experience = ""; housingCondition = ""
            snackbarHostState.showSnackbar("申请已提交，请等待审核")
            viewModel.clearSelection()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("领养乐园", fontWeight = FontWeight.Bold) },
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 标题
            Text(
                "领养乐园",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE67E22),
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                "点击小动物查看信息，给它们一个家",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (isLoading && sprites.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (sprites.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("暂无待领养宠物", color = TextSecondary)
                }
            } else {
                // Canvas 游戏区域
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .pointerInput(sprites.size) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val position = event.changes.firstOrNull()?.position ?: continue
                                    if (event.changes.firstOrNull()?.pressed != true) continue

                                    for (i in sprites.indices.reversed()) {
                                        val s = sprites[i]
                                        if (position.x >= s.x && position.x <= s.x + s.size &&
                                            position.y >= s.y && position.y <= s.y + s.size + 20f
                                        ) {
                                            viewModel.selectPet(s.pet)
                                            break
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val pad = 16f
                    val marginY = 16f
                    val areaLeft = pad
                    val areaRight = w - pad
                    val areaTop = marginY
                    val areaBottom = h - pad

                    // 草地背景
                    drawRoundRect(
                        color = Color(0xFFA8D5A2),
                        topLeft = Offset(areaLeft, areaTop),
                        size = Size(areaRight - areaLeft, areaBottom - areaTop),
                        cornerRadius = CornerRadius(4f)
                    )

                    // 围栏边框
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = Offset(areaLeft, areaTop),
                        size = Size(areaRight - areaLeft, areaBottom - areaTop),
                        cornerRadius = CornerRadius(4f),
                        style = Stroke(width = 3f)
                    )

                    // 围栏柱子
                    val postCount = ((areaRight - areaLeft) / 50f).toInt().coerceAtLeast(4)
                    for (i in 0..postCount) {
                        val px = areaLeft + i * ((areaRight - areaLeft) / postCount)
                        // 柱身
                        drawRect(
                            color = Color(0xFFA1887F),
                            topLeft = Offset(px - 3f, areaTop - 6f),
                            size = Size(6f, areaBottom - areaTop + 12f)
                        )
                        // 柱顶球
                        drawCircle(
                            color = Color(0xFF8D6E63),
                            radius = 5f,
                            center = Offset(px, areaTop - 4f)
                        )
                    }

                    // 绘制宠物
                    val currentTime = animFrame.value
                    for (sprite in sprites) {
                        val bobY = sin(currentTime * 0.002f + sprite.bobPhase) * 3f
                        val cx = sprite.x + sprite.size / 2f
                        val cy = sprite.y + sprite.size / 2f + bobY
                        val radius = sprite.size / 2f

                        // 宠物图片（圆形裁剪）
                        val bitmap = petBitmapCache[sprite.pet.id]
                        if (bitmap != null) {
                            val clipPath = Path().apply {
                                addOval(
                                    androidx.compose.ui.geometry.Rect(
                                        cx - radius, cy - radius,
                                        cx + radius, cy + radius
                                    )
                                )
                            }
                            clipPath(clipPath) {
                                drawImage(
                                    image = bitmap,
                                    dstSize = IntSize(sprite.size.toInt(), sprite.size.toInt()),
                                    dstOffset = IntOffset(sprite.x.toInt(), (sprite.y + bobY).toInt()),
                                    srcOffset = IntOffset.Zero,
                                    srcSize = IntSize(bitmap.width, bitmap.height)
                                )
                            }
                        } else {
                            // 占位圆
                            drawCircle(
                                color = Color(0xFFFFE0B2),
                                radius = radius,
                                center = Offset(cx, cy)
                            )
                            drawCircle(
                                color = Color(0xFFE0E0E0),
                                radius = radius,
                                center = Offset(cx, cy),
                                style = Stroke(width = 2f)
                            )
                        }

                        // 名字标签
                        val nameText = sprite.pet.name
                        val textLayout = textMeasurer.measure(
                            text = AnnotatedString(nameText),
                            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        )
                        val nw = textLayout.size.width.toFloat() + 12f
                        val nh = 22f
                        val nameX = sprite.x + sprite.size / 2f - nw / 2f
                        val nameY = sprite.y + bobY + sprite.size + 4f

                        // 名字背景
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.85f),
                            topLeft = Offset(nameX, nameY),
                            size = Size(nw, nh),
                            cornerRadius = CornerRadius(8f)
                        )
                        drawRoundRect(
                            color = Color.Black.copy(alpha = 0.1f),
                            topLeft = Offset(nameX, nameY),
                            size = Size(nw, nh),
                            cornerRadius = CornerRadius(8f),
                            style = Stroke(width = 1f)
                        )
                        // 名字文字
                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = Offset(
                                nameX + 6f,
                                nameY + (nh - textLayout.size.height) / 2f
                            ),
                            color = Color(0xFF333333)
                        )
                    }
                }
            }
        }
    }

    // 宠物详情 BottomSheet
    if (selectedPet != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.clearSelection() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            val pet = selectedPet!!
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 宠物大图
                AsyncImage(
                    model = pet.firstImageUrl(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F5F5), CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(pet.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                // 信息表格
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        InfoRow("物种", pet.species)
                        InfoRow("品种", pet.breed)
                        InfoRow("性别", pet.gender)
                        InfoRow("年龄", "${pet.age}岁")
                        pet.sizeLevel?.let { InfoRow("体型", it) }
                        pet.healthStatus?.let { InfoRow("健康", it) }
                        pet.description?.let { desc ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(desc, fontSize = 13.sp, color = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onPetClick(pet.id) },
                        modifier = Modifier.weight(1f)
                    ) { Text("查看详情") }
                    Button(
                        onClick = { showApplyDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) { Text("申请领养") }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // 领养申请对话框
    if (showApplyDialog && selectedPet != null) {
        AlertDialog(
            onDismissRequest = {
                showApplyDialog = false
                reason = ""; experience = ""; housingCondition = ""
            },
            title = { Text("申请领养 ${selectedPet?.name ?: ""}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("申请理由") },
                        placeholder = { Text("请简述您想领养这只宠物的原因") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    OutlinedTextField(
                        value = experience,
                        onValueChange = { experience = it },
                        label = { Text("养宠经验") },
                        placeholder = { Text("请描述您过往的养宠经验") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    OutlinedTextField(
                        value = housingCondition,
                        onValueChange = { housingCondition = it },
                        label = { Text("住房条件") },
                        placeholder = { Text("请描述您的住房条件") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.applyAdoption(reason, experience, housingCondition)
                    },
                    enabled = reason.isNotBlank() && experience.isNotBlank() && housingCondition.isNotBlank() && !applying
                ) {
                    if (applying) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("提交申请")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showApplyDialog = false
                    reason = ""; experience = ""; housingCondition = ""
                }) { Text("取消") }
            }
        )
    }

    }

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

// 散布宠物
private fun spreadSprites(sprites: List<PetSprite>, width: Float = 800f, height: Float = 500f) {
    val pad = 60f
    val marginY = 80f
    val areaW = width - pad * 2
    val areaH = height - marginY - pad

    sprites.forEach { p ->
        p.x = pad + (Math.random().toFloat() * (areaW - p.size))
        p.y = marginY + (Math.random().toFloat() * (areaH - p.size - 20f))
        val angle = Math.random().toFloat() * PI.toFloat() * 2f
        val speed = 0.2f + Math.random().toFloat() * 0.4f
        p.dx = cos(angle) * speed
        p.dy = sin(angle) * speed
    }
}

// 更新精灵位置
private fun updateSprites(sprites: MutableList<PetSprite>, width: Float = 800f, height: Float = 500f) {
    val pad = 16f
    val marginY = 16f
    val areaLeft = pad
    val areaRight = width - pad
    val areaTop = marginY
    val areaBottom = height - pad

    for (p in sprites) {
        p.x += p.dx
        p.y += p.dy

        val spriteBottom = p.y + p.size + 20f
        val spriteRight = p.x + p.size

        if (p.x < areaLeft) { p.x = areaLeft; p.dx = abs(p.dx) }
        if (spriteRight > areaRight) { p.x = areaRight - p.size; p.dx = -abs(p.dx) }
        if (p.y < areaTop) { p.y = areaTop; p.dy = abs(p.dy) }
        if (spriteBottom > areaBottom) { p.y = areaBottom - p.size - 20f; p.dy = -abs(p.dy) }

        // 碰撞检测
        for (other in sprites) {
            if (other === p) continue
            val ox = p.x + p.size / 2f; val oy = p.y + p.size / 2f
            val tx = other.x + other.size / 2f; val ty = other.y + other.size / 2f
            val dx = ox - tx; val dy = oy - ty
            val dist = sqrt(dx * dx + dy * dy)
            val minDist = p.size * 0.9f
            if (dist < minDist && dist > 0f) {
                val nx = dx / dist; val ny = dy / dist
                p.x += nx * 1f; p.y += ny * 1f
                other.x -= nx * 1f; other.y -= ny * 1f
                val dot = (p.dx - other.dx) * nx + (p.dy - other.dy) * ny
                p.dx -= dot * nx * 0.8f; p.dy -= dot * ny * 0.8f
                other.dx += dot * nx * 0.8f; other.dy += dot * ny * 0.8f
            }
        }
    }
}