package com.rendrapcx.tts.model

import androidx.room.Dao
import com.rendrapcx.tts.model.dao.ILevel
import com.rendrapcx.tts.model.dao.IPartial
import com.rendrapcx.tts.model.dao.IQuestion
import com.rendrapcx.tts.model.dao.IUser
import com.rendrapcx.tts.model.dao.IUserAnswerSlot
import com.rendrapcx.tts.model.dao.IUserAnswerTTS
import com.rendrapcx.tts.model.dao.IUserPreferences

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
    interface UserAnswerTTS : IUserAnswerTTS {}

    @Dao
    interface UserAnswerSlot : IUserAnswerSlot{}

    @Dao
    interface UserPreferences : IUserPreferences {}

}