package com.v2ray.ang.ui.tools

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.v2ray.ang.extension.toTrafficString
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.handler.UukeeTrafficStats
import com.v2ray.ang.ui.base.BaseComponentActivity
import com.v2ray.ang.ui.compose.ThemeManager
import com.v2ray.ang.ui.main.ConstellationBackground
import com.v2ray.ang.ui.main.MainActivity
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.util.Date

/** Небольшие экраны UUkee: статистика, сброс настроек и схемы URL-адресов. */
class UukeeToolsActivity : BaseComponentActivity() {
    @Composable
    override fun ScreenContent() {
        val mode = intent.getStringExtra(EXTRA_MODE)
        Box(Modifier.fillMaxSize()) {
            ConstellationBackground()
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when (mode) {
                            MODE_STATS -> "Статистика"
                            MODE_RESET -> "Сброс настроек"
                            else -> "Схемы URL-адресов"
                        },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    TextButton(onClick = { finish() }) { Text("Назад") }
                }
                Spacer(Modifier.height(8.dp))
                when (mode) {
                    MODE_STATS -> StatsContent()
                    MODE_RESET -> ResetContent(onReset = ::resetAndRestart, onCancel = { finish() })
                    else -> SchemesContent()
                }
            }
        }
    }

    private fun resetAndRestart() {
        MmkvManager.clearSettings()
        ThemeManager.refresh()
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }

    companion object {
        private const val EXTRA_MODE = "mode"
        const val MODE_STATS = "stats"
        const val MODE_RESET = "reset"
        const val MODE_SCHEMES = "schemes"

        fun intent(context: Context, mode: String): Intent =
            Intent(context, UukeeToolsActivity::class.java).putExtra(EXTRA_MODE, mode)
    }
}

@Composable
private fun ToolCard(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatsContent() {
    var stats by remember { mutableStateOf(UukeeTrafficStats.snapshot()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            stats = UukeeTrafficStats.snapshot()
        }
    }
    ToolCard {
        Text("Последнее подключение", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        if (stats.startedAtMillis > 0) {
            StatRow(
                "Начало",
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(stats.startedAtMillis))
            )
        }
        StatRow("Через VPN, отправлено", stats.sessionProxyUp.toTrafficString())
        StatRow("Через VPN, получено", stats.sessionProxyDown.toTrafficString())
        StatRow("Напрямую", stats.sessionDirect.toTrafficString())
    }
    ToolCard {
        Text("За всё время", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        StatRow("Через VPN, отправлено", stats.totalProxyUp.toTrafficString())
        StatRow("Через VPN, получено", stats.totalProxyDown.toTrafficString())
        StatRow("Напрямую", stats.totalDirect.toTrafficString())
    }
    Text(
        "Считается только на этом телефоне и никуда не отправляется. Статистика работает, " +
            "пока в настройках включён показ скорости.",
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    OutlinedButton(
        onClick = {
            UukeeTrafficStats.reset()
            stats = UukeeTrafficStats.snapshot()
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Обнулить статистику")
    }
}

@Composable
private fun ResetContent(onReset: () -> Unit, onCancel: () -> Unit) {
    ToolCard {
        Text("Все настройки вернутся к исходным.", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "Подписки и серверы останутся. Вход для администраторов и статистика трафика будут сброшены. " +
                "Если VPN включён, выключите и включите его снова.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onReset, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Text("Сбросить настройки")
        }
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
            Text("Отмена")
        }
    }
}

@Composable
private fun SchemeItem(scheme: String, description: String) {
    ToolCard {
        SelectionContainer {
            Text(scheme, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SchemesContent() {
    SchemeItem(
        "uukee://install-sub?url=ССЫЛКА",
        "Добавляет подписку сервиса и открывает приложение. Принимает только адреса uukee.xyz."
    )
    SchemeItem(
        "v2rayng://install-sub?url=ССЫЛКА",
        "Добавляет подписку с любого адреса."
    )
    SchemeItem(
        "v2rayng://install-config?url=ССЫЛКА",
        "Добавляет конфигурацию сервера по ссылке."
    )
}
