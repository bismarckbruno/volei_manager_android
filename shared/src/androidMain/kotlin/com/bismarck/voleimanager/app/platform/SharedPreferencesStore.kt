package com.bismarck.voleimanager.app.platform

import android.content.Context
import android.content.SharedPreferences

/** [PreferencesStore] sobre `SharedPreferences` (arquivo `"volei"`, o mesmo usado desde sempre). */
class SharedPreferencesStore(private val prefs: SharedPreferences) : PreferencesStore {

    override fun contains(key: String) = prefs.contains(key)
    override fun getString(key: String, defaultValue: String?) = prefs.getString(key, defaultValue)
    override fun getInt(key: String, defaultValue: Int) = prefs.getInt(key, defaultValue)
    override fun getLong(key: String, defaultValue: Long) = prefs.getLong(key, defaultValue)
    override fun getBoolean(key: String, defaultValue: Boolean) = prefs.getBoolean(key, defaultValue)

    override fun edit(): PreferencesStore.Editor = object : PreferencesStore.Editor {
        private val editor = prefs.edit()

        override fun putString(key: String, value: String?) = apply { editor.putString(key, value) }
        override fun putInt(key: String, value: Int) = apply { editor.putInt(key, value) }
        override fun putLong(key: String, value: Long) = apply { editor.putLong(key, value) }
        override fun putBoolean(key: String, value: Boolean) = apply { editor.putBoolean(key, value) }
        override fun remove(key: String) = apply { editor.remove(key) }
        override fun clear() = apply { editor.clear() }
        override fun apply() = editor.apply()
    }

    companion object {
        const val FILE_NAME = "volei"

        fun create(context: Context) =
            SharedPreferencesStore(context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE))
    }
}
