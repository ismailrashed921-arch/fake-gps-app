package com.kilagbe.fakegps

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

data class SavedLocation(val name: String, val lat: Double, val lng: Double)

val Context.dataStore by preferencesDataStore(name = "fake_gps_prefs")

object PrefKeys {
    val SAVED_LOCATIONS = stringPreferencesKey("saved_locations")
    val ACTIVE = booleanPreferencesKey("active")
    val ACTIVE_LAT = doublePreferencesKey("active_lat")
    val ACTIVE_LNG = doublePreferencesKey("active_lng")
    val ACTIVE_NAME = stringPreferencesKey("active_name")
    val AUTO_START = booleanPreferencesKey("auto_start")
    val JITTER = booleanPreferencesKey("jitter")
}

class LocationRepository(private val context: Context) {

    val savedLocationsFlow: Flow<List<SavedLocation>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[PrefKeys.SAVED_LOCATIONS] ?: "[]"
            parseLocations(raw)
        }

    val activeStateFlow: Flow<Triple<Boolean, Double, Double>> =
        context.dataStore.data.map { prefs ->
            Triple(
                prefs[PrefKeys.ACTIVE] ?: false,
                prefs[PrefKeys.ACTIVE_LAT] ?: 23.8103,
                prefs[PrefKeys.ACTIVE_LNG] ?: 90.4125
            )
        }

    suspend fun getSavedLocations(): List<SavedLocation> = savedLocationsFlow.first()

    suspend fun addLocation(loc: SavedLocation) {
        val current = getSavedLocations().toMutableList()
        current.add(loc)
        persist(current)
    }

    suspend fun removeLocation(name: String) {
        val current = getSavedLocations().filterNot { it.name == name }
        persist(current)
    }

    private suspend fun persist(list: List<SavedLocation>) {
        val arr = JSONArray()
        list.forEach {
            val obj = JSONObject()
            obj.put("name", it.name)
            obj.put("lat", it.lat)
            obj.put("lng", it.lng)
            arr.put(obj)
        }
        context.dataStore.edit { prefs ->
            prefs[PrefKeys.SAVED_LOCATIONS] = arr.toString()
        }
    }

    suspend fun setActive(active: Boolean, lat: Double, lng: Double, name: String?) {
        context.dataStore.edit { prefs ->
            prefs[PrefKeys.ACTIVE] = active
            prefs[PrefKeys.ACTIVE_LAT] = lat
            prefs[PrefKeys.ACTIVE_LNG] = lng
            prefs[PrefKeys.ACTIVE_NAME] = name ?: ""
        }
    }

    suspend fun setAutoStart(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[PrefKeys.AUTO_START] = enabled }
    }

    suspend fun setJitter(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[PrefKeys.JITTER] = enabled }
    }

    private fun parseLocations(raw: String): List<SavedLocation> {
        val arr = JSONArray(raw)
        val list = mutableListOf<SavedLocation>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(SavedLocation(obj.getString("name"), obj.getDouble("lat"), obj.getDouble("lng")))
        }
        return list
    }
}
