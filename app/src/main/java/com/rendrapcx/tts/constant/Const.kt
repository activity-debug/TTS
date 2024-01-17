package com.rendrapcx.tts.constant

import android.content.Context
import android.provider.ContactsContract.Data
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.rendrapcx.tts.model.Data.*

class Const {
    enum class BoardSet { PLAY, PLAY_USER, PLAY_NEXT, EDITOR_NEW, EDITOR_EDIT }
    enum class InputQuestionDirection { HORIZONTAL, VERTICAL }
    enum class InputAnswerDirection { ROW, COLUMN }

    enum class QrAction {READ, CREATE}

    companion object {
        var qrAction = QrAction.CREATE

        var isSignedIn = false
        var currentUserId = "ADMIN"
        var currentUserRefId = ""

        var gameState = GameState.CREATOR
        var boardSet = BoardSet.EDITOR_NEW

        var position = 0
        var currentIndex = 0


        var currentLevel = ""
        var inputMode = ""

        val strRight = "➡\uFE0F"
        val strDown = "⬇\uFE0F"
    }
}