package com.v2ray.ang.handler

/** Счётчики трафика UUkee: за текущее подключение и за всё время. Хранятся только на телефоне. */
object UukeeTrafficStats {
    private const val KEY_STARTED = "uukee_stat_started"
    private const val KEY_SESSION_PROXY_UP = "uukee_stat_s_proxy_up"
    private const val KEY_SESSION_PROXY_DOWN = "uukee_stat_s_proxy_down"
    private const val KEY_SESSION_DIRECT = "uukee_stat_s_direct"
    private const val KEY_TOTAL_PROXY_UP = "uukee_stat_t_proxy_up"
    private const val KEY_TOTAL_PROXY_DOWN = "uukee_stat_t_proxy_down"
    private const val KEY_TOTAL_DIRECT = "uukee_stat_t_direct"

    data class Snapshot(
        val startedAtMillis: Long,
        val sessionProxyUp: Long,
        val sessionProxyDown: Long,
        val sessionDirect: Long,
        val totalProxyUp: Long,
        val totalProxyDown: Long,
        val totalDirect: Long,
    )

    private fun read(key: String): Long = MmkvManager.decodeSettingsString(key)?.toLongOrNull() ?: 0L

    private fun write(key: String, value: Long) {
        MmkvManager.encodeSettings(key, value.toString())
    }

    private fun increase(key: String, delta: Long) {
        if (delta > 0) write(key, read(key) + delta)
    }

    fun startSession() {
        write(KEY_STARTED, System.currentTimeMillis())
        write(KEY_SESSION_PROXY_UP, 0)
        write(KEY_SESSION_PROXY_DOWN, 0)
        write(KEY_SESSION_DIRECT, 0)
    }

    fun add(proxyUp: Long, proxyDown: Long, directUp: Long, directDown: Long) {
        increase(KEY_SESSION_PROXY_UP, proxyUp)
        increase(KEY_SESSION_PROXY_DOWN, proxyDown)
        increase(KEY_SESSION_DIRECT, directUp + directDown)
        increase(KEY_TOTAL_PROXY_UP, proxyUp)
        increase(KEY_TOTAL_PROXY_DOWN, proxyDown)
        increase(KEY_TOTAL_DIRECT, directUp + directDown)
    }

    fun snapshot() = Snapshot(
        startedAtMillis = read(KEY_STARTED),
        sessionProxyUp = read(KEY_SESSION_PROXY_UP),
        sessionProxyDown = read(KEY_SESSION_PROXY_DOWN),
        sessionDirect = read(KEY_SESSION_DIRECT),
        totalProxyUp = read(KEY_TOTAL_PROXY_UP),
        totalProxyDown = read(KEY_TOTAL_PROXY_DOWN),
        totalDirect = read(KEY_TOTAL_DIRECT),
    )

    fun reset() {
        listOf(
            KEY_SESSION_PROXY_UP, KEY_SESSION_PROXY_DOWN, KEY_SESSION_DIRECT,
            KEY_TOTAL_PROXY_UP, KEY_TOTAL_PROXY_DOWN, KEY_TOTAL_DIRECT,
        ).forEach { write(it, 0) }
    }
}
