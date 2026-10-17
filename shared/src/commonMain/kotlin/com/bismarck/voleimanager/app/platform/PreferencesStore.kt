package com.bismarck.voleimanager.app.platform

/**
 * Armazenamento chave-valor persistente (preferências do app, estado do jogo salvo, contadores).
 * A API espelha a de `SharedPreferences` para que o Android continue gravando exatamente nas
 * mesmas chaves/arquivo (`"volei"`) de antes; no iOS é implementado sobre `NSUserDefaults`.
 */
interface PreferencesStore {
    fun contains(key: String): Boolean
    fun getString(key: String, defaultValue: String?): String?
    fun getInt(key: String, defaultValue: Int): Int
    fun getLong(key: String, defaultValue: Long): Long
    fun getBoolean(key: String, defaultValue: Boolean): Boolean

    fun edit(): Editor

    interface Editor {
        fun putString(key: String, value: String?): Editor
        fun putInt(key: String, value: Int): Editor
        fun putLong(key: String, value: Long): Editor
        fun putBoolean(key: String, value: Boolean): Editor
        fun remove(key: String): Editor
        fun clear(): Editor
        fun apply()
    }
}

/** Implementação em memória, para testes em código comum. */
class InMemoryPreferencesStore : PreferencesStore {
    private val values = mutableMapOf<String, Any>()

    override fun contains(key: String) = values.containsKey(key)
    override fun getString(key: String, defaultValue: String?) = values[key] as? String ?: defaultValue
    override fun getInt(key: String, defaultValue: Int) = values[key] as? Int ?: defaultValue
    override fun getLong(key: String, defaultValue: Long) = values[key] as? Long ?: defaultValue
    override fun getBoolean(key: String, defaultValue: Boolean) = values[key] as? Boolean ?: defaultValue

    override fun edit(): PreferencesStore.Editor = object : PreferencesStore.Editor {
        private val pending = mutableMapOf<String, Any?>()
        private var clearFirst = false

        private fun put(key: String, value: Any?) = apply { pending[key] = value }
        override fun putString(key: String, value: String?) = put(key, value)
        override fun putInt(key: String, value: Int) = put(key, value)
        override fun putLong(key: String, value: Long) = put(key, value)
        override fun putBoolean(key: String, value: Boolean) = put(key, value)
        override fun remove(key: String) = put(key, null)
        override fun clear() = apply { clearFirst = true; pending.clear() }

        override fun apply() {
            if (clearFirst) values.clear()
            pending.forEach { (k, v) -> if (v == null) values.remove(k) else values[k] = v }
        }
    }
}
