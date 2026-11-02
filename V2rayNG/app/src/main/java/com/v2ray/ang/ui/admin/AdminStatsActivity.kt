package com.v2ray.ang.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.ui.base.BaseComponentActivity
import com.v2ray.ang.ui.main.ConstellationBackground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val API_URL = "https://kabinet.uukee.xyz/api/cabinet/app-admin"
private const val TOKEN_KEY = "uukee_admin_token"
private const val REFRESH_MILLIS = 30_000L

private data class NodeStat(val name: String, val online: Int, val up: Boolean, val disabled: Boolean)

private data class AdminStats(
    val onlineNow: Int,
    val onlineDay: Int,
    val onlineWeek: Int,
    val everConnected: Int,
    val totalUsers: Int,
    val nodes: List<NodeStat>,
)

private class ApiException(val code: Int, message: String) : IOException(message)

private val httpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .build()

private fun execute(request: Request): JSONObject {
    httpClient.newCall(request).execute().use { response ->
        val body = response.body?.string().orEmpty()
        if (!response.isSuccessful) {
            val detail = runCatching { JSONObject(body).optString("detail") }.getOrNull().orEmpty()
            throw ApiException(response.code, detail.ifEmpty { "Ошибка сервера: ${response.code}" })
        }
        return JSONObject(body)
    }
}

private fun login(login: String, password: String): String {
    val body = JSONObject().put("login", login).put("password", password).toString()
        .toRequestBody("application/json".toMediaType())
    return execute(Request.Builder().url("$API_URL/login").post(body).build()).getString("token")
}

private fun loadStats(token: String): AdminStats {
    val json = execute(
        Request.Builder().url("$API_URL/stats").header("Authorization", "Bearer $token").build()
    )
    val nodes = json.getJSONArray("nodes")
    return AdminStats(
        onlineNow = json.getInt("online_now"),
        onlineDay = json.getInt("online_day"),
        onlineWeek = json.getInt("online_week"),
        everConnected = json.getInt("ever_connected"),
        totalUsers = json.getInt("total_users"),
        nodes = List(nodes.length()) { index ->
            val node = nodes.getJSONObject(index)
            NodeStat(
                name = node.getString("name"),
                online = node.getInt("online"),
                up = node.getBoolean("up"),
                disabled = node.getBoolean("disabled"),
            )
        },
    )
}

/** Вкладка администраторов: вход по логину и паролю, затем счётчики. Только просмотр. */
class AdminStatsActivity : BaseComponentActivity() {
    @Composable
    override fun ScreenContent() {
        var token by remember { mutableStateOf(MmkvManager.decodeSettingsString(TOKEN_KEY).orEmpty()) }
        val saveToken: (String) -> Unit = { value ->
            MmkvManager.encodeSettings(TOKEN_KEY, value)
            token = value
        }
        Box(Modifier.fillMaxSize()) {
            ConstellationBackground()
            if (token.isEmpty()) {
                LoginScreen(onLoggedIn = saveToken, onBack = { finish() })
            } else {
                StatsScreen(token = token, onLogout = { saveToken("") }, onBack = { finish() })
            }
        }
    }
}

@Composable
private fun LoginScreen(onLoggedIn: (String) -> Unit, onBack: () -> Unit) {
    var loginText by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        GlassCard {
            Text(
                "Для администраторов",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = loginText,
                onValueChange = { loginText = it.trim() },
                label = { Text("Логин") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it.trim() },
                label = { Text("Пароль") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            if (error.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    busy = true
                    error = ""
                    scope.launch {
                        try {
                            val token = withContext(Dispatchers.IO) { login(loginText, password) }
                            onLoggedIn(token)
                        } catch (e: IOException) {
                            error = if (e is ApiException) e.message.orEmpty() else "Нет связи с сервером"
                        } catch (e: org.json.JSONException) {
                            error = "Неверный ответ сервера"
                        } finally {
                            busy = false
                        }
                    }
                },
                enabled = !busy && loginText.isNotEmpty() && password.isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (busy) "Входим…" else "Войти")
            }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Назад")
            }
        }
    }
}

@Composable
private fun StatsScreen(token: String, onLogout: () -> Unit, onBack: () -> Unit) {
    var stats by remember { mutableStateOf<AdminStats?>(null) }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(token) {
        while (true) {
            try {
                stats = withContext(Dispatchers.IO) { loadStats(token) }
                error = ""
            } catch (e: ApiException) {
                if (e.code == 401) {
                    onLogout()
                    return@LaunchedEffect
                }
                error = e.message.orEmpty()
            } catch (e: IOException) {
                error = "Нет связи с сервером"
            } catch (e: org.json.JSONException) {
                error = "Неверный ответ сервера"
            }
            delay(REFRESH_MILLIS)
        }
    }

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
                "Статистика",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            TextButton(onClick = onBack) { Text("Назад") }
            TextButton(onClick = onLogout) { Text("Выйти") }
        }
        if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        val current = stats
        if (current == null) {
            if (error.isEmpty()) Text("Загружаем…")
        } else {
        Spacer(Modifier.height(8.dp))
        GlassCard {
            Text("Онлайн сейчас", style = MaterialTheme.typography.bodyMedium)
            Text(
                current.onlineNow.toString(),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile("За сутки", current.onlineDay, Modifier.weight(1f))
            StatTile("За неделю", current.onlineWeek, Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile("Подключались хоть раз", current.everConnected, Modifier.weight(1f))
            StatTile("Всего в панели", current.totalUsers, Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Серверы",
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        current.nodes.forEach { node ->
            NodeRow(node)
            Spacer(Modifier.height(6.dp))
        }
        }
    }
}

@Composable
private fun GlassCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
private fun StatTile(label: String, value: Int, modifier: Modifier = Modifier) {
    GlassCard(modifier) {
        Text(value.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NodeRow(node: NodeStat) {
    val color = when {
        node.disabled -> Color(0xFF9CA3AF)
        node.up -> Color(0xFF22C55E)
        else -> Color(0xFFEF4444)
    }
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                node.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (node.disabled) "выключен" else if (node.up) node.online.toString() else "недоступен",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (node.up) MaterialTheme.colorScheme.onSurface else color
            )
        }
    }
}
