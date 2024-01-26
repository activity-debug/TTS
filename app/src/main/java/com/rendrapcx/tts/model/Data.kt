package com.rendrapcx.tts.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rendrapcx.tts.constant.Const.AnswerStatus
import com.rendrapcx.tts.constant.Const.FilterStatus
import kotlinx.serialization.Serializable

class Data {
    @Serializable
    @Entity(tableName = "level")
    data class Level(
        @PrimaryKey(autoGenerate = false)
        @ColumnInfo(name = "id") var id: String,
        @ColumnInfo(name = "category") var category: String,
        @ColumnInfo(name = "title") var title: String,
        @ColumnInfo(name = "user_id") var userId: String,
        @ColumnInfo(name = "status") var status: FilterStatus,
    )

    @Serializable
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

    @Entity(tableName = "user")
    data class User(
        @PrimaryKey(autoGenerate = false)
        @ColumnInfo(name = "id") var id : String,
        @ColumnInfo(name = "username") var username : String,
        @ColumnInfo(name = "password") var password : String,
        @ColumnInfo(name = "is_guest") var isGuest : Boolean,
    )

    @Entity(tableName = "user_answer_tts")
    data class UserAnswerTTS(
        @PrimaryKey(autoGenerate = false)
        @ColumnInfo(name = "id") var id : Int,
        @ColumnInfo(name = "user_id") var userId: Int,
        @ColumnInfo(name = "level_id") var levelId: String,
        @ColumnInfo(name = "answer_slot") var answerSlot: ArrayList<String>,
        @ColumnInfo(name = "status") var status: AnswerStatus,
    )

    @Entity(tableName = "user_answer_tbk")
    data class UserAnswerTBK(
        @PrimaryKey(autoGenerate = false)
        @ColumnInfo(name = "id") var id : Int,
        @ColumnInfo(name = "tbk_id") var tbkId : String,
    )

    @Entity(tableName = "user_preferences")
    data class UserPreferences(
        @PrimaryKey(autoGenerate = false)
        @ColumnInfo(name = "id") var id : String,
        @ColumnInfo(name = "current_user") var currentUser : Int,
        @ColumnInfo(name = "is_login") var isLogin: Boolean,
        @ColumnInfo(name = "active_filter_tab") var activeFilterTab : FilterStatus ,
        @ColumnInfo(name = "sort_order_by_author") var sortOrderByAuthor : Boolean,
        @ColumnInfo(name = "integrated_keyboard") var integratedKeyboard : Boolean,
        @ColumnInfo(name = "is_music") var isMusic : Boolean,
        @ColumnInfo(name = "is_sound") var isSound : Boolean,
    )

    @Serializable
    data class QRShare(
        var level : MutableList<Level>,
        var question : MutableList<Question>
    )

    companion object {
        var listLevel = mutableListOf<Level>()
        var listQuestion = mutableListOf<Question>()
        var listPartial = mutableListOf<Partial>()
        var listUser = mutableListOf<User>()
        var userPreferences = mutableListOf<UserPreferences>()
        var qrShare = mutableListOf<QRShare>()
    }
}