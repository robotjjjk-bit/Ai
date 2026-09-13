package com.nexchat.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.ds by preferencesDataStore("nex_settings")

@Singleton
class SettingsStore @Inject constructor(@ApplicationContext private val ctx: Context) {
    private val DARK = booleanPreferencesKey("dark_v2")
    private val DYN = booleanPreferencesKey("dyn")
    private val DEF_PROV = stringPreferencesKey("def_prov")
    private val DEF_MODEL = stringPreferencesKey("def_model")
    private val SEARCH_KEY = stringPreferencesKey("search_key")
    private val SEARCH_KIND = stringPreferencesKey("search_kind")

    val darkModeFlow = ctx.ds.data.map { it[DARK] }
    val dynamicColorFlow = ctx.ds.data.map { it[DYN] ?: true }

    private fun secrets() = EncryptedSharedPreferences.create(
        ctx, "nex_secrets", MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun putApiKey(providerId: String, key: String) { secrets().edit().putString("key_$providerId", key).apply() }
    fun getApiKey(providerId: String): String = secrets().getString("key_$providerId", "") ?: ""
    suspend fun setDark(v: Boolean?) { ctx.ds.edit { if (v == null) it.remove(DARK) else it[DARK] = v } }
    suspend fun setDynamic(v: Boolean) { ctx.ds.edit { it[DYN] = v } }
}
