package com.monkey.data.reposiitory

import android.content.Context
import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

open class BaseSharedPreferenceImpl(private val fileName: String, open val context: Context) {
    open val TAG = "BaseSharedPreference"

    protected val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = fileName)
    protected val sharePreferenceName get() = fileName

    /**
     * get default value from shared preference
     */
    protected inline fun <T, reified R> Preferences.Key<T>.default(default: R): R = runBlocking {
        val value = context.dataStore.data
            .catch { exception ->
                if (exception is CorruptionException || exception is java.io.IOException) {
                    Log.w(TAG, "Data Store Exception $exception")
                    context.filesDir.listFiles()
                        ?.find { it.name == "datastore" }?.let { dataStoreFolder ->
                            dataStoreFolder.listFiles()
                                ?.find { it.name.contains(sharePreferenceName) }?.let {
                                    Log.w(TAG, "Data Store File delete ${it.name}")
                                    it.delete()
                                }
                        }
                    emit(emptyPreferences())
                } else throw exception
            }.map {
                Log.i(
                    TAG,
                    "[default] preference load : " +
                            "${this@default} = ${it[this@default]}, $default"
                )
                if (it[this@default] == null) {
                    context.dataStore.edit { preferences ->
                        preferences[this@default] = default as T
                    }
                }
                return@map it[this@default] ?: default
            }.first()
        if (value is R) value else default
    }

    /**
     * create flow to observe changes in shared preference key value pair
     */
    protected inline fun <reified T> Preferences.Key<T>.createFlow(default: T): MutableStateFlow<T> {
        return MutableStateFlow(this.default(default))
    }
}