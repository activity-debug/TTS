package com.rendrapcx.tts.model

class Data {

    data class Level(
        var id: String,
        var category : String,
        var dimension : String,
    )

    data class Question(
        var id: String,
        var number : Int,
        var direction: String,
        var asking : String,
        var answer : String,
        var slot : ArrayList<Int>,
        var levelId: String,
    )

    data class Partial(
        var id: String,
        var charAt: Int,
        var char: String,
        var rowQuestionId: String,
        var colQuestionId: String,
        var levelId: String,
    )

    companion object {
        var listLevel = mutableListOf<Level>()
        var listQuestion = mutableListOf<Question>()
        var listPartial = mutableListOf<Partial>()
    }
}