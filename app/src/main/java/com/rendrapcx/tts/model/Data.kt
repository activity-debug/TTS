package com.rendrapcx.tts.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

class Data {
    @Entity(tableName = "level")
    data class Level(
        @PrimaryKey(autoGenerate = false)
        @ColumnInfo(name = "id") var id: String,
        @ColumnInfo(name = "category") var category: String,
        @ColumnInfo(name = "title") var title: String,
        @ColumnInfo(name = "user_id") var userId: String,
    )

    @Entity(tableName = "question")
    data class Question(
        @PrimaryKey(autoGenerate = false)
        @ColumnInfo(name = "id") var id: String,
        @ColumnInfo(name = "number") var number: Int,
        @ColumnInfo(name = "direction") var direction: String,
        @ColumnInfo(name = "asking") var asking: String,
        @ColumnInfo(name = "answer") var answer: String,
        @ColumnInfo(name = "slot") var slot: ArrayList<Int>,
        @ColumnInfo(name = "level_id") var levelId: String,
    )

    @Entity(tableName = "partial")
    data class Partial(
        @PrimaryKey(autoGenerate = false)
        @ColumnInfo(name = "id") var id: String,
        @ColumnInfo(name = "char_at") var charAt: Int,
        @ColumnInfo(name = "char_str") var charStr: String,
        @ColumnInfo(name = "row_question_id") var rowQuestionId: String,
        @ColumnInfo(name = "col_question_id") var colQuestionId: String,
        @ColumnInfo(name = "level_id") var levelId: String,
)
    {
        constructor() : this("",0,"","","","")
    }

    @Entity(tableName = "User")
    data class User(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id") var id : Int,
        @ColumnInfo(name = "name") var name : String,
        @ColumnInfo(name = "username") var username : String,
        @ColumnInfo(name = "email") var email : String,
    )

    @Entity(tableName = "user_answer")
    data class UserAnswer(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id") var id : Int,
        @ColumnInfo(name = "user_id") var userId: Int?=-1,
        @ColumnInfo(name = "level_id") var levelId: String?=null,
        @ColumnInfo(name = "answer") var answer: String? =null,
        @ColumnInfo(name = "status") var status: Int? = 0,
    )

    @Entity(tableName = "user_preferences")
    data class UserPreferences(
        @PrimaryKey(autoGenerate = false)
        @ColumnInfo(name = "id") var id : String,
        @ColumnInfo(name = "show_finished") var showFinished : Boolean,
        @ColumnInfo(name = "sort_order_by_author") var sortOrderByAuthor : Boolean,
        @ColumnInfo(name = "integrated_keyboard") var integratedKeyboard : Boolean,
    )

    companion object {
        var listLevel = mutableListOf<Level>()
        var listQuestion = mutableListOf<Question>()
        var listPartial = mutableListOf<Partial>()
    }
}