package com.rendrapcx.tts.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class Converters {

//    @TypeConverter
//    fun listToJson(value: ArrayList<Int>?) = Gson().toJson(value)
//
//    @TypeConverter
//    fun jsonToList(value: String) = Gson().fromJson(value, Array<Int>::class.java).toList()

    @TypeConverter
    fun fromString(value: String?): ArrayList<Int> {
        val listType: Type = object : TypeToken<ArrayList<Int?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<Int?>?): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}

//object Converters {
//    @TypeConverter
//    fun fromString(value: String?): ArrayList<String> {
//        val listType: Type = object : TypeToken<ArrayList<String?>?>() {}.type
//        return Gson().fromJson(value, listType)
//    }
//
//    @TypeConverter
//    fun fromArrayList(list: ArrayList<String?>?): String {
//        val gson = Gson()
//        return gson.toJson(list)
//    }
//}