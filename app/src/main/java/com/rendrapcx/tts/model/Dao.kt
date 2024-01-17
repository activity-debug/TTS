package com.rendrapcx.tts.model

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rendrapcx.tts.model.dao.ILevel
import com.rendrapcx.tts.model.dao.IPartial
import com.rendrapcx.tts.model.dao.IQuestion
import com.rendrapcx.tts.model.dao.IUser
import com.rendrapcx.tts.model.dao.IUserAnswer
import com.rendrapcx.tts.model.dao.IUserPreferences
import kotlinx.coroutines.flow.Flow

class Dao {

    @Dao
    interface Level : ILevel {}

    @Dao
    interface Question : IQuestion {}

    @Dao
    interface Partial : IPartial {}

    @Dao
    interface User : IUser {}

    @Dao
    interface UserAnswer : IUserAnswer {}

    @Dao
    interface UserPreferences : IUserPreferences {}

}