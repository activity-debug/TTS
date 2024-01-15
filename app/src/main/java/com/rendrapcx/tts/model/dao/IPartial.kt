package com.rendrapcx.tts.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rendrapcx.tts.model.Data

interface IPartial {
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