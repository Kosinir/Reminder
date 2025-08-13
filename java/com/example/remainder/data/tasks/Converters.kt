package com.example.remainder.data.tasks
import androidx.room.TypeConverter

class Converters {
    // konwersja typu RepeatInterval <-> String
    @TypeConverter
    fun fromRepeatInterval(value: RepeatInterval): String = value.name

    @TypeConverter
    fun toRepeatInterval(value: String): RepeatInterval =
        RepeatInterval.valueOf(value)

    class BooleanListConverter {
        @TypeConverter
        fun fromBooleanList(list: List<Boolean>): String {
            return list.joinToString(",") { it.toString() }
            }

        @TypeConverter
        fun toBooleanList(data: String): List<Boolean> {
            return data.split(",").mapNotNull {
                when(it.trim()){
                    "true" -> true
                    "false" -> false
                    else -> null
                }
            }
        }

    }

}