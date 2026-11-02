package com.v2ray.ang.handler

import com.v2ray.ang.AppConfig

/**
 * Российские приложения по умолчанию работают мимо VPN: банки и госсервисы часто
 * не открываются при включённом VPN, а так его не нужно выключать вручную.
 * Список можно изменить в разделе «Прокси для выбранных приложений».
 */
object UukeeBypassApps {
    private const val KEY_SEEDED = "uukee_bypass_seeded"

    private val PACKAGES = setOf(
        // Банки
        "ru.sberbankmobile",
        "ru.vtb24.mobilebanking.android",
        "com.idamob.tinkoff.android",
        "ru.alfabank.mobile.android",
        "ru.raiffeisennews",
        "ru.gazprombank.android.mobilebank.app",
        "logo.com.mbanking",
        "ru.psbank.mobile",
        "com.openbank",
        // Госсервисы
        "ru.rostel",
        "ru.mos.app",
        "ru.nalog.www",
        // Маркетплейсы и сервисы
        "com.wildberries.ru",
        "ru.ozon.app.android",
        "com.avito.android",
        "ru.yandex.searchplugin",
        "ru.yandex.yandexmaps",
        "ru.yandex.taxi",
        "ru.yandex.music",
        "ru.yandex.mail",
        "ru.yandex.market",
        "com.vkontakte.android",
        "ru.ok.android",
        "ru.mail.mailapp",
        "ru.oneme.app",
        "ru.rutube.app",
        "ru.dublgis.dgismobile",
        "ru.rzd.pass",
    )

    fun seedOnce() {
        if (MmkvManager.decodeSettingsBool(KEY_SEEDED, false)) return
        MmkvManager.encodeSettings(KEY_SEEDED, true)
        // Если пользователь уже настроил список сам, его не трогаем.
        if (MmkvManager.decodeSettingsBool(AppConfig.PREF_PER_APP_PROXY, false)) return
        MmkvManager.encodeSettings(AppConfig.PREF_PER_APP_PROXY_SET, PACKAGES.toMutableSet())
        MmkvManager.encodeSettings(AppConfig.PREF_BYPASS_APPS, true)
        MmkvManager.encodeSettings(AppConfig.PREF_PER_APP_PROXY, true)
    }
}
