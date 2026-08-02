package com.example.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString("|||")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isEmpty()) return emptyList()
        return value.split("|||")
    }

    @TypeConverter
    fun fromDoubleList(value: List<Double>): String {
        return value.joinToString("|||")
    }

    @TypeConverter
    fun toDoubleList(value: String): List<Double> {
        if (value.isEmpty()) return emptyList()
        return value.split("|||").map { it.toDouble() }
    }
}
