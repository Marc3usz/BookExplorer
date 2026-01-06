package com.example.bookexplorer

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "favorites")

class FavoritesManager(private val context: Context) {
    private val FAVORITES_KEY = stringPreferencesKey("favorite_books")

    val favoritesFlow: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            val favoritesString = preferences[FAVORITES_KEY] ?: ""
            if (favoritesString.isEmpty()) {
                emptySet()
            } else {
                favoritesString.split(",").toSet()
            }
        }

    suspend fun toggleFavorite(bookKey: String) {
        context.dataStore.edit { preferences ->
            val favoritesString = preferences[FAVORITES_KEY] ?: ""
            val currentFavorites: Set<String> = if (favoritesString.isEmpty()) {
                emptySet()
            } else {
                favoritesString.split(",").toSet()
            }

            val newFavorites = if (currentFavorites.contains(bookKey)) {
                currentFavorites - bookKey
            } else {
                currentFavorites + bookKey
            }

            preferences[FAVORITES_KEY] = newFavorites.joinToString(",")
        }
    }

    suspend fun isFavorite(bookKey: String): Boolean {
        return favoritesFlow.first().contains(bookKey)
    }
}