package com.pawmatch.app.data.api

import android.content.Context
import android.content.SharedPreferences

object ServerConfigManager {
    private const val PREF_NAME = "pawmatch_server_config"
    private const val KEY_SERVER_URL = "server_url"
    const val DEFAULT_URL = "http://192.168.0.3:8080/api/"

    private var prefs: SharedPreferences? = null

    fun init(ctx: Context) {
        prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    var serverUrl: String
        get() = prefs?.getString(KEY_SERVER_URL, DEFAULT_URL) ?: DEFAULT_URL
        set(value) {
            prefs?.edit()?.putString(KEY_SERVER_URL, value)?.apply()
        }
}
