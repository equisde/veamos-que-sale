package com.roro.lgthinq

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TVPreferences(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("lg_tv_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val KEY_SAVED_TVS = "saved_tvs"
        private const val KEY_LAST_CONNECTED_IP = "last_ip"
    }
    
    fun saveTVDevice(device: TVDevice) {
        val tvs = getSavedTVs().toMutableList()
        
        // Remover TV existente con misma IP
        tvs.removeAll { it.ip == device.ip }
        
        // Agregar nuevo/actualizado
        tvs.add(device)
        
        val json = gson.toJson(tvs)
        prefs.edit()
            .putString(KEY_SAVED_TVS, json)
            .apply()
    }
    
    fun getSavedTVs(): List<TVDevice> {
        val json = prefs.getString(KEY_SAVED_TVS, null) ?: return emptyList()
        val type = object : TypeToken<List<TVDevice>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun getTVByIP(ip: String): TVDevice? {
        return getSavedTVs().firstOrNull { it.ip == ip }
    }
    
    fun updateClientKey(ip: String, clientKey: String) {
        val tv = getTVByIP(ip)
        if (tv != null) {
            saveTVDevice(tv.copy(clientKey = clientKey))
        } else {
            saveTVDevice(TVDevice(ip = ip, clientKey = clientKey))
        }
    }
    
    fun setLastConnectedIP(ip: String) {
        prefs.edit().putString(KEY_LAST_CONNECTED_IP, ip).apply()
    }
    
    fun getLastConnectedIP(): String? {
        return prefs.getString(KEY_LAST_CONNECTED_IP, null)
    }
    
    fun removeTVDevice(ip: String) {
        val tvs = getSavedTVs().toMutableList()
        tvs.removeAll { it.ip == ip }
        val json = gson.toJson(tvs)
        prefs.edit()
            .putString(KEY_SAVED_TVS, json)
            .apply()
    }
    
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
