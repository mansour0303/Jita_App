package com.example.jita.data

import androidx.room.TypeConverter
import com.example.jita.TaskPriority
import org.json.JSONArray
import org.json.JSONException
import java.util.Calendar

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Calendar? {
        return value?.let {
            Calendar.getInstance().apply { timeInMillis = it }
        }
    }

    @TypeConverter
    fun calendarToTimestamp(calendar: Calendar?): Long? {
        return calendar?.timeInMillis
    }

    @TypeConverter
    fun fromPriority(value: String?): TaskPriority? {
        return value?.let { enumValueOf<TaskPriority>(it) }
    }

    @TypeConverter
    fun priorityToString(priority: TaskPriority?): String? {
        return priority?.name
    }
}

/**
 * Converter for lists of strings to JSON string representation
 */
class StringListConverter {
    @TypeConverter
    fun fromString(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        
        return try {
            val jsonArray = JSONArray(value)
            val stringList = mutableListOf<String>()
            
            for (i in 0 until jsonArray.length()) {
                stringList.add(jsonArray.getString(i))
            }
            stringList
        } catch (e: JSONException) {
            emptyList()
        }
    }
    
    @TypeConverter
    fun fromList(list: List<String>?): String {
        if (list.isNullOrEmpty()) return "[]"
        
        val jsonArray = JSONArray()
        list.forEach { jsonArray.put(it) }
        return jsonArray.toString()
    }
}

/**
 * Converter for lists of integers to JSON string representation
 */
class IntListConverter {
    @TypeConverter
    fun fromString(value: String?): List<Int> {
        if (value.isNullOrEmpty()) return emptyList()
        
        return try {
            val jsonArray = JSONArray(value)
            val intList = mutableListOf<Int>()
            
            for (i in 0 until jsonArray.length()) {
                intList.add(jsonArray.getInt(i))
            }
            intList
        } catch (e: JSONException) {
            emptyList()
        }
    }
    
    @TypeConverter
    fun fromList(list: List<Int>?): String {
        if (list.isNullOrEmpty()) return "[]"
        
        val jsonArray = JSONArray()
        list.forEach { jsonArray.put(it) }
        return jsonArray.toString()
    }
} 