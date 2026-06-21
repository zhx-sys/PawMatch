package com.pawmatch.app.data.api

import android.content.Context
import com.pawmatch.app.data.model.LoginResponse
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore("pawmatch_prefs")

object TokenManager {
    private val KEY_TOKEN = stringPreferencesKey("token")
    private val KEY_USER_ID = longPreferencesKey("user_id")
    private val KEY_USER_TYPE = longPreferencesKey("user_type")

    var token: String = ""
        private set
    var userId: Long = 0
        private set
    var userType: Int = 0
        private set

    private var context: Context? = null

    fun init(ctx: Context) {
        context = ctx
        runBlocking {
            val prefs = ctx.dataStore.data.first()
            token = prefs[KEY_TOKEN] ?: ""
            userId = prefs[KEY_USER_ID] ?: 0
            userType = (prefs[KEY_USER_TYPE] ?: 0).toInt()
        }
    }

    suspend fun save(loginRes: LoginResponse) {
        context?.let { ctx ->
            ctx.dataStore.edit {
                it[KEY_TOKEN] = loginRes.token
                it[KEY_USER_ID] = loginRes.userId
                it[KEY_USER_TYPE] = loginRes.userType.toLong()
            }
        }
        token = loginRes.token
        userId = loginRes.userId
        userType = loginRes.userType
    }

    suspend fun clear() {
        context?.let { ctx ->
            ctx.dataStore.edit { it.clear() }
        }
        token = ""
        userId = 0
        userType = 0
    }

    val isLoggedIn: Boolean get() = token.isNotEmpty()
}
