package com.example.myshoplist.features.shopping_list.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.myshoplist.features.shopping_list.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesRepository {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("myshoplist_prefs", Context.MODE_PRIVATE)

    override suspend fun saveActiveSharedListId(listId: String) {
        sharedPreferences.edit().putString("ACTIVE_SHARED_LIST_ID", listId).apply()
    }

    override fun getActiveSharedListId(): Flow<String?> = callbackFlow {
        // Emitir el valor actual inmediatamente
        trySend(sharedPreferences.getString("ACTIVE_SHARED_LIST_ID", null))

        // Escuchar cambios futuros
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == "ACTIVE_SHARED_LIST_ID") {
                trySend(prefs.getString(key, null))
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        // Limpiar el listener cuando el Flow se cancele
        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    override suspend fun clearSharedListId() {
        sharedPreferences.edit().remove("ACTIVE_SHARED_LIST_ID").apply()
    }
}