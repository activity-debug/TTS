package com.rendrapcx.tts.constant

import android.media.MediaPlayer
import androidx.lifecycle.MutableLiveData

class Const {
    enum class BoardSet { PLAY_KATEGORI, PLAY_RANDOM, EDITOR_NEW, EDITOR_EDIT }
    enum class InputQuestionDirection { H, V }
    enum class InputAnswerDirection { ROW, COLUMN }

    enum class AnswerStatus {DONE, PROGRESS, UNDONE}

    enum class FilterStatus {ALL, DRAFT, POST}

    enum class SelectRequest{
        NEXT, PREV
    }

    enum class InputMode { NEW, EDIT }

    enum class Direction { H, V }

    enum class Counter { DELETE, SAVE, LOAD }

    companion object {
        var pubCaAppId = "ca-app-pub-5609246517650629~7589046483"
        var bannerCaAppId = "ca-app-pub-5609246517650629/2803623664"
        var intersCaAppId = "ca-app-pub-5609246517650629/9483231011"

        //FireBase DB
        var dbApp = "https://terka-tts-default-rtdb.asia-southeast1.firebasedatabase.app"
        var dbRefQuestions = "questions"

        var boardSet = BoardSet.EDITOR_NEW
        var isEditor = false
        var listSelesai = arrayListOf<String>()
        var listProgress = arrayListOf<String>()

        var position = 0
        var currentIndex = 0

        var currentLevel = ""
        var currentCategory = ""
        var inputMode = ""

        var isEnableClick = true


        var player = MediaPlayer()
        var playTitleOnline = ""
        var playTitleOffline = ""
        var currentTrackOffline = 0
        var currentTrackOnline = 0
        var indexPlayOnline = 0
        var indexPlayOffline = 0
        var isPlay = false
        var isMusic = false
        var isOnlinePlay = false

        var koinUser = 0
        var koinPay = 50
        var hargaPalu = 70
    }
}