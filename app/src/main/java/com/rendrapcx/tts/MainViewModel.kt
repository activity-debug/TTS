package com.rendrapcx.tts

import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

enum class Track { RIGHT, DOWN, BOTH }
data class Level(var id: Int)
data class Soal(var id : Int, var tanya: String, var jawab: String, var levelId: Int)
data class Part(var id: Int, var boxId: Int, var char: String, var direction: Track, var soalId: Int)

class MainViewModel : ViewModel() {
    var position = -1
    private var selectedValue = MutableLiveData<String>()

    var boxView = arrayListOf<TextView>()

    var level = Sample.levelList
    var soal = Sample.soalList
    var part = Sample.partList

    init {
        selectedValue.value = ""
    }

    fun currentValue(value: String){
        selectedValue.value = value
    }
}


object Sample {
    var levelList = mutableListOf<Level>(
        Level(1),
    )
    var soalList = mutableListOf<Soal>(
        Soal(1, "hanya dasar", "ALAS", 1),
        Soal(2, "tidak benar-benar", "ASAL", 1),

    )
    var partList = mutableListOf<Part>(
        Part(1,1,"A", Track.RIGHT,1),
        Part(1,2,"L", Track.RIGHT,1),
        Part(1,3,"A", Track.RIGHT,1),
        Part(1,4,"S", Track.RIGHT,1),
        Part(1,6,"A", Track.RIGHT,1),
        Part(1,7,"S", Track.RIGHT,1),
        Part(1,8,"A", Track.RIGHT,1),
        Part(1,9,"L", Track.RIGHT,1),
    )
}