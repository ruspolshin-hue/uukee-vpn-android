package com.v2ray.ang.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

private val ColorConnected = Color(0xFFA855F7)
private const val BOT_URL = "https://t.me/uukee_bot"

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
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, cardShape)
            .padding(horizontal = 20.dp, vertical = 24.dp),
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
                    .size(132.dp)
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
                    modifier = Modifier.size(56.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = if (isRunning) "Подключено" else "Не подключено",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
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
