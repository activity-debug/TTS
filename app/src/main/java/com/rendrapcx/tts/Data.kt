package com.rendrapcx.tts

class Data {

    data class Levels(var id: String? = null)
    data class Questions(
        var levelId: String? = null,
        var id: String? = null,
        var no: String? = null,
        var ask: String? = null,
        var answer: String? = null,
        var direction: String? = null,
        var members: MutableList<Parts>?=null
    )

    data class Members(val soalId: String, var value : Int)
    data class Parts(
        var levelId: String? = null,
        var questionId: String? = null,
        var id: String? = null,
        var charAt: String? = null,
        var char: String? = null,
        var rowQuestionId: String? = null,
        var hasRow: Boolean? = false,
        var colQuestionId: String? = null,
        var hasCol: Boolean? = false
    )

    companion object {
        var levelList = mutableListOf<Levels>(
//            Levels("1"),
        )
        var memberList = mutableListOf<Members>()
        var questionList = mutableListOf<Questions>(
//            Questions("1", "0", "SAMPLE", "ASA", "HORIZONTAL" ),
//            Questions("1", "1", "SAMPLE", "ASA", "HORIZONTAL" ),
//            Questions("2", "1", "SAMPLE", "ASA", "HORIZONTAL" ),
//            Questions("2", "1", "SAMPLE", "ASA", "HORIZONTAL" ),
//            Questions("2", "1", "SAMPLE", "ASA", "HORIZONTAL" ),
//            Questions("2", "1", "SAMPLE", "ASA", "HORIZONTAL" ),
//            Questions("2", "1", "SAMPLE", "ASA", "HORIZONTAL" ),
//            Questions("2", "1", "SAMPLE", "ASA", "HORIZONTAL" ),
//            Questions("2", "1", "SAMPLE", "ASA", "HORIZONTAL" ),
//            Questions("2", "1", "SAMPLE", "ASA", "HORIZONTAL" ),
//            Questions("2", "1", "SAMPLE", "ASA", "HORIZONTAL" ),
//            Questions("2", "1", "SAMPLE", "ASA", "HORIZONTAL" ),
//            Questions("2", "1", "SAMPLE", "ASA", "HORIZONTAL" ),
//            Questions("2", "1", "SAMPLE", "ASA", "HORIZONTAL" ),
//            Questions("2", "1", "SAMPLE", "ASA", "HORIZONTAL" ),
//            Questions("2", "1", "SAMPLE", "ASA", "HORIZONTAL" ),
//            Questions("2", "1", "SAMPLE", "ASA", "HORIZONTAL" ),
//            Questions("2", "1", "SAMPLE", "ASA", "HORIZONTAL" ),
//            Questions("2", "1", "SAMPLE", "ASA", "HORIZONTAL" ),
//            Questions("2", "1", "SAMPLE", "ASA", "HORIZONTAL" ),
//            Questions("2", "1", "SAMPLE", "ASA", "HORIZONTAL" ),
//            Questions("2", "1", "SAMPLE", "ASA", "HORIZONTAL" ),
//            Questions("2", "1", "SAMPLE", "ASA", "HORIZONTAL" ),
        )

        var partList = mutableListOf<Parts>(
//            Parts("1", "1", "1", "A", "1", true,"", false),
//            Parts("1", "2", "2", "S", "1", true,"", false),
//            Parts("1", "3", "3", "A", "1", true,"", false),
        )
    }
}