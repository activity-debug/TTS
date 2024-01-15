package com.rendrapcx.tts.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        Data.Level::class,
        Data.Question::class,
        Data.Partial::class,
        Data.User::class,
        Data.UserAnswer::class,
        Data.UserPreferences::class,
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DB : RoomDatabase() {
    abstract fun level(): Dao.Level
    abstract fun question(): Dao.Question
    abstract fun partial(): Dao.Partial
    abstract fun user(): Dao.User
    abstract fun userAnswer(): Dao.UserAnswer
    abstract fun userPreferences(): Dao.UserPreferences


    companion object {
        @Volatile
        private var INSTANCE: DB? = null
        fun getInstance(context: Context): DB {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        DB::class.java,
                        "tts-db"
                    )
                        //.createFromAsset("database/tts-db")
                        .build()
                }
                return instance
            }
        }
    }
}
