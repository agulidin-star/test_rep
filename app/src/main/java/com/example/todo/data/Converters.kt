package com.example.todo.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.ArrayList
import java.util.List

class Converters {

    @TypeConverter
    fun listToString(value: List<Int>?): String {
        return if (value == null || value.isEmpty()) {
            "[]"
        } else {
            Gson().toJson(value)
        }
    }

    @TypeConverter
    fun stringToList(value: String?): List<Int> {
        return if (value == null || value.isEmpty() || value == "[]") {
            ArrayList()
        } else {
            val type: Type = object : TypeToken<List<Int>>() {}.type
            Gson().fromJson<List<Int>>(value, type)
        }
    }
}