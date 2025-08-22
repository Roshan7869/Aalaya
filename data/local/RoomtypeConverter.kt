package com.aalay.app.data.local.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room type converters for complex data types
 * Handles List<String> and other complex types used in entities
 */
class Converters {
    
    private val gson = Gson()
    
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return if (value == null) "" else gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) {
            emptyList()
        } else {
            try {
                val listType = object : TypeToken<List<String>>() {}.type
                gson.fromJson(value, listType) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    @TypeConverter
    fun fromDoubleList(value: List<Double>?): String {
        return if (value == null) "" else gson.toJson(value)
    }
    
    @TypeConverter
    fun toDoubleList(value: String): List<Double> {
        return if (value.isEmpty()) {
            emptyList()
        } else {
            try {
                val listType = object : TypeToken<List<Double>>() {}.type
                gson.fromJson(value, listType) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}