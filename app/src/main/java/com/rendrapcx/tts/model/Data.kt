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

    data class Partial(
        var id: String,
        var charAt: Int,
        var charStr: String,
        var rowQuestionId: String,
        var colQuestionId: String,
        var levelId: String,
    ) {
        constructor() : this("", 0, "", "", "", "")
    }

    @Entity(tableName = "user_answer_tts")
    data class UserAnswerTTS(
        @PrimaryKey(autoGenerate = false)
        @ColumnInfo(name = "id") var id: String,
        @ColumnInfo(name = "user_id") var userId: String,
        @ColumnInfo(name = "level_id") var levelId: String,
        @ColumnInfo(name = "status") var status: AnswerStatus,
    )

    @Entity(tableName = "user_answer_slot")
    data class UserAnswerSlot(
        @PrimaryKey(autoGenerate = false)
        @ColumnInfo(name = "id") var id: String,
        @ColumnInfo(name = "level_id") var levelId: String,
        @ColumnInfo(name = "answer_slot") var answerSlot: MutableMap<Int, String>,
        @ColumnInfo(name = "payed") var payed: Int
    )

    @Entity(tableName = "user_answer_random")
    data class UserAnswerRandom(
        @PrimaryKey(autoGenerate = false)
        @ColumnInfo(name = "id") var id: String,
        @ColumnInfo(name = "level_id") var levelId: String,
        @ColumnInfo(name = "answer_slot") var answerSlot: MutableMap<Int, String>,
        @ColumnInfo(name = "payed") var payed: Int
    )

    @Entity(tableName = "user_preferences")
    data class UserPreferences(
        @PrimaryKey(autoGenerate = false)
        @ColumnInfo(name = "id") var id: Int,
        @ColumnInfo(name = "active_filter_tab") var activeFilterTab: FilterStatus,
        @ColumnInfo(name = "integrated_keyboard") var integratedKeyboard: Boolean,
        @ColumnInfo(name = "is_music") var isMusic: Boolean,
        @ColumnInfo(name = "is_sound") var isSound: Boolean,
        @ColumnInfo(name = "is_editor") var isEditor: Boolean,
        @ColumnInfo(name = "last_acak") var lastAcak: String,
        @ColumnInfo(name = "koin") var koin: Int,
    )

    @Serializable
    data class QRShare(
        var level: MutableList<Level>,
        var question: MutableList<Question>
    )

    data class OnlineLevel(
        val id: String? = "",
        val category: String?="",
        val editor: String?="",
        val encodeString: String?="",
    ){}

    data class OnlineLevelList(
        val id: String? = "",
        val category: String?="",
        val editor: String?="",
    ){}

    companion object {
        var listLevel = mutableListOf<Level>()
        var listOnLevel = mutableListOf<OnlineLevel>()
        var listOnlineList = mutableListOf<OnlineLevelList>()
        var listQuestion = mutableListOf<Question>()
        var listPartial = mutableListOf<Partial>()
        var userAnswerTTS = mutableListOf<UserAnswerTTS>()
        var userAnswerRandom = mutableListOf<UserAnswerRandom>()
        var userPreferences = mutableListOf<UserPreferences>()
    }
}