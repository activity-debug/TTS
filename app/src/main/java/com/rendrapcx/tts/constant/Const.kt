package com.rendrapcx.tts.constant

import android.content.Context
import android.provider.ContactsContract.Data
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.rendrapcx.tts.model.Data.*

class Const {
    enum class BoardSet { PLAY, PLAY_USER, PLAY_RANDOM, PLAY_NEXT, EDITOR_NEW, EDITOR_EDIT }
    enum class InputQuestionDirection { HORIZONTAL, VERTICAL }
    enum class InputAnswerDirection { ROW, COLUMN, UNKNOWN }

    enum class QrAction {READ, CREATE}

    enum class AnswerStatus {DONE, PROGRESS, UNDONE}

    enum class FilterStatus {ALL, DRAFT, POST}

    companion object {
        var pubCaAppId = "ca-app-pub-5609246517650629~7589046483"
        var bannerCaAppId = "ca-app-pub-5609246517650629/2803623664"
        var intersCaAppId = "ca-app-pub-5609246517650629/9483231011"

        var qrAction = QrAction.CREATE

        var isSignedIn = false
        var currentUserId = "ADMIN"
        var currentUser = -1

        var selesai = arrayListOf<String>()
        var progress = arrayListOf<String>()

        var resetLevel = false

        var gameState = GameState.CREATOR
        var boardSet = BoardSet.EDITOR_NEW

        var position = 0
        var currentIndex = 0


        var currentLevel = ""
        var currentCategory = ""
        var inputMode = ""

//        var isSound = true
        val strGreen = "🟢"
        val strYellow = "🟡"
        val strRed = "🔴"
        val strDone = "✅"
        val strRight = "➡\uFE0F"
        val strDown = "⬇\uFE0F"
    }
}