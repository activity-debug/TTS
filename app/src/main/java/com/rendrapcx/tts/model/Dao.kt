package com.rendrapcx.tts.model

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

class Dao {


    @Dao
    interface Level {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertLevel(level: Data.Level)

        @Update
        suspend fun updateLevel(level: Data.Level)

        @Delete()
        suspend fun deleteLevel(level: Data.Level)

//        @Query("select * from level")
//        fun getAllLevel(): LiveData<List<Data.Level>>

        @Query("select * from level")
        suspend fun getAllLevel(): MutableList<Data.Level>

        @Query("SELECT * FROM level WHERE `id` = :id;")
        suspend fun getLevel(id: String): MutableList<Data.Level>

        @Query("DELETE FROM level WHERE `id` = :id;")
        suspend fun deleteLevelById(id: String)

    }

    @Dao
    interface Question {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertQuestion(question: Data.Question)

        @Query("select * from question")
        fun getAllQuestion(): LiveData<List<Data.Question>>

        @Query("SELECT * FROM question WHERE level_id = :levelId;")
        suspend fun getQuestion(levelId: String): MutableList<Data.Question>

        @Query("DELETE FROM question WHERE level_id = :levelId;")
        suspend fun deleteQuestionByLevelId(levelId: String)

    }

    @Dao
    interface Partial {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertPartial(partial: Data.Partial)

        @Query("select * from partial")
        fun getAllPartial(): LiveData<List<Data.Partial>>

        @Query("SELECT * FROM partial WHERE level_id = :levelId;")
        suspend fun getPartial(levelId: String): MutableList<Data.Partial>

        @Query(value =  "UPDATE partial " +
                        "   SET row_question_id = :rowId " +
                        "       WHERE id = :id;")
        suspend fun updateRowId(id: String, rowId: String)


        @Query(value =  "UPDATE partial " +
                        "   SET col_question_id =:colId " +
                        "       WHERE id = :id;")
        suspend fun updateColId(id: String, colId: String)

        @Query("DELETE FROM partial WHERE level_id = :levelId;")
        suspend fun deletePartialByLevelId(levelId: String)

    }


    @Dao
    interface User {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insertUser(user: Data.User)

        @Query("SELECT * FROM user")
        suspend fun getAllUser(): MutableList<Data.User>

        @Query("SELECT * FROM user WHERE username = :username")
        suspend fun getCurrentUser(username : String): MutableList<Data.User>

    }

    @Dao
    interface UserAnswer {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insertUserAnswer(userAnswer: Data.UserAnswer)

        @Update
        fun updateUserAnswer(userAnswer: Data.UserAnswer)

        @Query("SELECT * FROM user_answer")
        suspend fun getAllUserAnswer(): MutableList<Data.UserAnswer>

        @Query("SELECT * FROM user_answer WHERE level_id=:levelId")
        suspend fun getLevelAnswer(levelId: String) : MutableList<Data.UserAnswer>
    }

    @Dao
    interface UserPreferences{

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertUserPref(userPreferences: Data.UserPreferences)

        @Query("SELECT * FROM user_preferences")
        suspend fun getAllUserPreferences(): MutableList<Data.UserPreferences>

        @Query("UPDATE user_preferences " +
                "   SET show_finished = :showFinished " +
                "   WHERE `id` = :id;")
        suspend fun updateShowFinished(id: String, showFinished: Boolean)

        @Query("UPDATE user_preferences " +
                "   SET sort_order_by_author = :sortOrderByAuthor " +
                "   WHERE `id` = :id;")
        suspend fun updateSortOrderByAuthor(id: String, sortOrderByAuthor: Boolean)

        @Query("UPDATE user_preferences " +
                "   SET integrated_keyboard = :integratedKeyboard " +
                "   WHERE `id` = :id;")
        suspend fun updateIntegratedKeyboard(id: String, integratedKeyboard: Boolean)
    }
}