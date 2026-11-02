package com.v2ray.ang.handler

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Автовыбор для кнопки «Подключиться»: по типу сети берётся сервер «Авто» из подписки,
 * а лучший выход внутри него выбирает балансировщик. Ручной выбор отключает автомат.
 */
object UukeeAutoServer {
    private const val KEY_MANUAL = "uukee_manual_server"

    var isManual: Boolean
        get() = MmkvManager.decodeSettingsBool(KEY_MANUAL, false)
        set(value) {
            MmkvManager.encodeSettings(KEY_MANUAL, value)
        }

    fun pick(context: Context): String? {
        val autoServers = MmkvManager.decodeAllServerList().mapNotNull { guid ->
            val remarks = MmkvManager.decodeServerConfig(guid)?.remarks?.lowercase().orEmpty()
            if (remarks.contains("авто")) guid to remarks else null
        }
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
        val mobile = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        val isLte = { remarks: String -> remarks.contains("lte") }
        val isWifi = { remarks: String -> remarks.contains("wi-fi") || remarks.contains("wifi") }
        val best = autoServers.firstOrNull { (_, remarks) ->
            if (mobile) isLte(remarks) && !isWifi(remarks) else isWifi(remarks) && !isLte(remarks)
        }
        return (best ?: autoServers.firstOrNull())?.first
    }
}
