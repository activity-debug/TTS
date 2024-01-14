package com.rendrapcx.tts.constant

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

class Const {
    enum class BoardSet { EDITOR, PLAY }
    enum class InputQuestionDirection { HORIZONTAL, VERTICAL }
    enum class InputAnswerDirection { ROW, COLUMN }

    companion object {


        var isSignedIn = false
        var currentUserId = "JACK"

        var gameState = GameState.CREATOR
        var boardSet = BoardSet.EDITOR

        var position = 0
        var currentIndex = 0


        var currentLevel = ""
        var inputMode = ""

        val strRight = "➡\uFE0F"
        val strDown = "⬇\uFE0F"
    }
}