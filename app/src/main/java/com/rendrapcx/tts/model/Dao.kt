package com.rendrapcx.tts.model

import androidx.lifecycle.LiveData
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

        @Delete
        suspend fun deleteLevel(level: Data.Level)

//        @Query("select * from level")
//        fun getAllLevel(): LiveData<List<Data.Level>>

        @Query("select * from level")
        suspend fun getAllLevel(): MutableList<Data.Level>

        @Query("SELECT * FROM level WHERE id = :id;")
        suspend fun getLevel(id: String): MutableList<Data.Level>

    }

    @Dao
    interface Question {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertQuestion(question: Data.Question)

        @Update(onConflict = OnConflictStrategy.REPLACE)
        suspend fun updateQuestion(question: Data.Question)

        @Delete
        suspend fun deleteQuestion(question: Data.Question)

        @Query("select * from question")
        fun getAllQuestion(): LiveData<List<Data.Question>>

        @Query("SELECT * FROM question WHERE level_id = :levelId;")
        suspend fun getQuestion(levelId: String): MutableList<Data.Question>

    }

    @Dao
    interface Partial {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertPartial(partial: Data.Partial)

        @Update(onConflict = OnConflictStrategy.REPLACE)
        suspend fun updatePartial(partial: Data.Partial)

        @Delete
        suspend fun deletePartial(partial: Data.Partial)

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

    }

}