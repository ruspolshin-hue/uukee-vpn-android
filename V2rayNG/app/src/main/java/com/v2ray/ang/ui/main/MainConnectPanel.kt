package com.v2ray.ang.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R
import com.v2ray.ang.dto.entities.SubscriptionUsageInfo
import com.v2ray.ang.handler.MmkvManager
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val ColorConnected = Color(0xFFA855F7)
private const val BOT_URL = "https://t.me/uukee_bot"
private const val BATTERY_ASKED_KEY = "uukee_battery_asked"
private const val TWO_PI = 6.2831855f
private val ConstellationColor = Color(0xFFD946EF)

/** Главный экран: одна кнопка, сервер выбирается автоматически. */
@Composable
fun MainHome(
    isRunning: Boolean,
    displayText: String,
    serverName: String,
    hasSubscription: Boolean,
    manualServer: Boolean,
    usageInfo: SubscriptionUsageInfo?,
    groupTitle: String,
    onAction: (MainAction) -> Unit,
    onShowServers: () -> Unit,
    onRefresh: () -> Unit,
) {
    BatteryPermissionDialog(enabled = hasSubscription)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PromoCard(
            modifier = Modifier
                .weight(1f)
                .padding(top = 4.dp, bottom = 8.dp),
            onInvite = { onAction(MainAction.OpenSubscriptionLink(BOT_URL)) }
        )
        if (!hasSubscription) {
            OutlinedButton(
                onClick = { onAction(MainAction.ImportClipboard) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Вставить скопированное")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { onAction(MainAction.OpenSubscriptionLink(BOT_URL)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Получить подписку")
            }
        } else {
            if (usageInfo != null) {
                SubscriptionInfoCard(
                    info = usageInfo,
                    fallbackTitle = groupTitle,
                    onOpenLink = { onAction(MainAction.OpenSubscriptionLink(it)) },
                    onRefresh = onRefresh,
                )
            }
            Spacer(Modifier.height(4.dp))
            StatusPill(isRunning)
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (isRunning && serverName.isNotEmpty()) serverName
                else if (manualServer) "Выбран сервер вручную" else "Сервер выберется автоматически",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isRunning) {
                Text(
                    text = displayText,
                    modifier = Modifier.clickable { onAction(MainAction.TestCurrentServer) },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
            TextButton(onClick = onShowServers) {
                Text("Выбрать сервер вручную")
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { onAction(MainAction.ToggleService) },
            enabled = hasSubscription,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRunning) Color(0xFF7E22CE) else ColorConnected,
                contentColor = Color.White
            )
        ) {
            Text(
                text = when {
                    !hasSubscription -> "Добавьте подписку"
                    isRunning -> "Отключиться"
                    else -> "Подключиться"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun PromoCard(modifier: Modifier, onInvite: () -> Unit) {
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.72f))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), shape)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Приглашай друзей",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Получай 50% с каждой покупки",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )
        Image(
            painter = painterResource(R.mipmap.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
        OutlinedButton(
            onClick = onInvite,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Пригласить друзей")
        }
    }
}

/** Один раз просит снять ограничения батареи, иначе Android отключает VPN в фоне. */
@Composable
private fun BatteryPermissionDialog(enabled: Boolean) {
    val context = LocalContext.current
    var visible by remember(enabled) {
        val power = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        mutableStateOf(
            enabled &&
                !MmkvManager.decodeSettingsBool(BATTERY_ASKED_KEY, false) &&
                !power.isIgnoringBatteryOptimizations(context.packageName)
        )
    }
    if (!visible) return
    val close = {
        MmkvManager.encodeSettings(BATTERY_ASKED_KEY, true)
        visible = false
    }
    AlertDialog(
        onDismissRequest = close,
        title = { Text("Аккумулятор") },
        text = {
            Text("Чтобы VPN не отключался в фоне, снимите ограничения батареи для приложения. Нажмите «Разрешить».")
        },
        dismissButton = { TextButton(onClick = close) { Text("Отмена") } },
        confirmButton = {
            TextButton(
                onClick = {
                    close()
                    runCatching {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                Uri.parse("package:${context.packageName}")
                            )
                        )
                    }
                }
            ) {
                Text("Разрешить")
            }
        }
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
