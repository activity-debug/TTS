package com.rendrapcx.tts.constant

import android.content.Context
import android.provider.ContactsContract.Data
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.rendrapcx.tts.model.Data.*

class Const {
    enum class BoardSet { PLAY, PLAY_USER, PLAY_RANDOM, PLAY_NEXT, EDITOR_NEW, EDITOR_EDIT }
    enum class InputQuestionDirection { H, V }
    enum class InputAnswerDirection { ROW, COLUMN, UNKNOWN }

    enum class QrAction {READ, CREATE}

    enum class AnswerStatus {DONE, PROGRESS, UNDONE}

    enum class FilterStatus {ALL, DRAFT, POST}

    enum class SelectRequest{
        NEXT, PREV
    }

    enum class InputMode { NEW, EDIT }

    enum class Direction { H, V }

    companion object {
        var pubCaAppId = "ca-app-pub-5609246517650629~7589046483"
        var bannerCaAppId = "ca-app-pub-5609246517650629/2803623664"
        var intersCaAppId = "ca-app-pub-5609246517650629/9483231011"

        var selesai = arrayListOf<String>()
        var progress = arrayListOf<String>()

        var isEditor = false

        var boardSet = BoardSet.EDITOR_NEW

        var position = 0
        var currentIndex = 0

        var currentLevel = ""
        var currentCategory = ""
        var inputMode = ""

    }
}