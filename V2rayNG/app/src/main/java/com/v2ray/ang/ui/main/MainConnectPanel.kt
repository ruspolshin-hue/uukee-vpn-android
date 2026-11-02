package com.v2ray.ang.ui.main

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val ColorConnected = Color(0xFFA855F7)
private const val BOT_URL = "https://t.me/uukee_bot"
private const val TWO_PI = 6.2831855f
private val ConstellationColor = Color(0xFFD946EF)

/** Главный блок UUkee VPN: большая кнопка подключения, статус и выбранный сервер. */
@Composable
fun MainConnectPanel(
    isRunning: Boolean,
    displayText: String,
    serverName: String,
    hasSubscription: Boolean,
    onAction: (MainAction) -> Unit,
) {
    val cardShape = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(cardShape)
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.72f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, cardShape)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!hasSubscription) {
            Text(
                text = "Добавьте подписку",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Откройте бота, выберите подписку и нажмите «Открыть в UUkee VPN». " +
                    "Или скопируйте ссылку подписки и вставьте её здесь.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onAction(MainAction.OpenSubscriptionLink(BOT_URL)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Открыть бота")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { onAction(MainAction.ImportClipboard) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Вставить ссылку подписки")
            }
        } else {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(
                        if (isRunning) Brush.linearGradient(listOf(Color(0xFFA855F7), Color(0xFF7E22CE)))
                        else SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    )
                    .border(
                        width = 3.dp,
                        color = if (isRunning) ColorConnected else MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .clickable { onAction(MainAction.ToggleService) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        if (isRunning) R.drawable.ic_stop_24dp else R.drawable.ic_play_24dp
                    ),
                    contentDescription = stringResource(
                        if (isRunning) R.string.acc_stop else R.string.acc_start
                    ),
                    tint = if (isRunning) Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
                StatusPill(isRunning)
            if (serverName.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = serverName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (isRunning) {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.clickable { onAction(MainAction.TestCurrentServer) },
                    horizontalArrangement = Arrangement.Center
                ) {
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/** Плашка статуса, как «Активна» / «Истекла» в Mini App. */
@Composable
private fun StatusPill(isRunning: Boolean) {
    val color = if (isRunning) Color(0xFF22C55E) else Color(0xFFEF4444)
    val shape = RoundedCornerShape(999.dp)
    Text(
        text = if (isRunning) "Подключено" else "Не подключено",
        modifier = Modifier
            .clip(shape)
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.4f), shape)
            .padding(horizontal = 14.dp, vertical = 4.dp),
        color = color,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold
    )
}

/** Заголовок списка: обновление подписки и четыре варианта проверки серверов. */
@Composable
fun MainServerTools(
    isRunning: Boolean,
    isTesting: Boolean,
    statusText: String,
    onAction: (MainAction) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 12.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Серверы",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            ToolChip(
                label = "Обновить",
                iconRes = R.drawable.ic_refresh_24dp,
                onClick = { onAction(MainAction.UpdateSubscriptions) }
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isTesting) {
                ToolChip("Остановить проверку", onClick = { onAction(MainAction.CancelTesting) })
            } else {
                ToolChip("Реальный пинг", onClick = { onAction(MainAction.TestRealAllServers) })
                ToolChip("TCP-пинг", onClick = { onAction(MainAction.TestAllServers) })
                ToolChip(
                    "Текущий сервер",
                    enabled = isRunning,
                    onClick = { onAction(MainAction.TestCurrentServer) }
                )
                ToolChip("По пингу", onClick = { onAction(MainAction.SortByTestResults) })
            }
        }
        if (isTesting) {
            Text(
                text = statusText,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun ToolChip(
    label: String,
    iconRes: Int? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    val contentColor = if (enabled) MaterialTheme.colorScheme.onSurface
    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.72f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconRes != null) {
            Icon(
                painterResource(iconRes),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(label, style = MaterialTheme.typography.labelLarge, color = contentColor, maxLines = 1)
    }
}

private class BottomNavItem(val iconRes: Int, val label: String, val url: String?)

private val BottomNavItems = listOf(
    BottomNavItem(R.drawable.ic_flash_on_24dp, "Главная", null),
    BottomNavItem(R.drawable.ic_subscriptions_24dp, "Подписка", BOT_URL),
    BottomNavItem(R.drawable.ic_lock_24dp, "Кабинет", "https://kabinet.uukee.xyz"),
    BottomNavItem(R.drawable.ic_feedback_24dp, "Поддержка", "https://t.me/Hypisdec"),
)

/** Плавающее нижнее меню, как в Mini App. */
@Composable
fun UukeeBottomNav(onAction: (MainAction) -> Unit) {
    val barShape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(barShape)
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, barShape)
            .padding(6.dp)
    ) {
        BottomNavItems.forEach { item ->
            val selected = item.url == null
            val itemShape = RoundedCornerShape(16.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(itemShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent
                    )
                    .clickable { item.url?.let { onAction(MainAction.OpenSubscriptionLink(it)) } }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painterResource(item.iconRes),
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        }
    }
}

private class ConstellationNode(val x: Float, val y: Float, val phase: Float, val radius: Float)

/** Фон-созвездие из Mini App. Движется медленно и рисуется только пока экран на виду. */
@Composable
fun ConstellationBackground() {
    val nodes = remember {
        val random = Random(7)
        List(34) {
            ConstellationNode(
                x = random.nextFloat(),
                y = random.nextFloat(),
                phase = random.nextFloat() * TWO_PI,
                radius = 1.5f + random.nextFloat() * 2f
            )
        }
    }
    val time by rememberInfiniteTransition(label = "constellation").animateFloat(
        initialValue = 0f,
        targetValue = TWO_PI,
        animationSpec = infiniteRepeatable(tween(durationMillis = 40_000, easing = LinearEasing)),
        label = "constellationTime"
    )
    val background = MaterialTheme.colorScheme.background
    Canvas(
        Modifier
            .fillMaxSize()
            .background(background)
    ) {
        val linkDistance = size.minDimension * 0.34f
        val drift = size.minDimension * 0.025f
        val points = nodes.map { node ->
            Offset(
                x = node.x * size.width + cos(time + node.phase) * drift,
                y = node.y * size.height + sin(time + node.phase) * drift
            )
        }
        for (i in points.indices) {
            for (j in i + 1 until points.size) {
                val distance = (points[i] - points[j]).getDistance()
                if (distance < linkDistance) {
                    drawLine(
                        color = ConstellationColor.copy(alpha = 0.22f * (1f - distance / linkDistance)),
                        start = points[i],
                        end = points[j],
                        strokeWidth = 1f
                    )
                }
            }
        }
        points.forEachIndexed { index, point ->
            drawCircle(ConstellationColor.copy(alpha = 0.75f), nodes[index].radius * density, point)
        }
    }
}
