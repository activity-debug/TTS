package com.rendrapcx.tts.constant

import com.rendrapcx.tts.model.Data

class Const {
    enum class BoardSet { EDITOR, PLAY }
    enum class InputQuestionDirection { ROW, COLUMN }
    enum class InputAnswerDirection { ROW, COLUMN }

    companion object {
//        var listLevel = mutableListOf<Data.Level>()
//        var listQuestion = mutableListOf<Data.Question>()
//        var listPartial = mutableListOf<Data.Partial>()

        var gameState = GameState.CREATOR
        var boardSet = BoardSet.EDITOR
        var inputQuestionDirection = InputQuestionDirection.ROW
        var inputAnswerDirection = InputAnswerDirection.ROW

        var currentLevel = ""

        val strCheck = "✔"
        val strUncheck = "✖"
        val strSave = "💾"
        val strEdit = "📝"
        val strNew = "\uD83C\uDD95"
        val strRight = "➡\uFE0F"
        val strDown = "⬇\uFE0F"
        val strMoveRight = "👉🏻"
        val strMoveDown = "\uD83D\uDC47\uD83C\uDFFB"
    }
}