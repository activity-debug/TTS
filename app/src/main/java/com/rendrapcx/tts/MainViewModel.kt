package com.rendrapcx.tts

import android.content.Context
import android.graphics.Color
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.asFlow
import java.util.UUID


open class MainViewModel : ViewModel() {
    private var prevPos = -1
    var position = MutableLiveData<Int>()
    var boxView = arrayListOf<TextView>()

    //private var countSoalId = MutableLiveData<Int>()

    val xLen = 10
    val yLen = 10
    private var countXY = (xLen * yLen)

    var questions = Data.questionList

    var levelId = ""


    init {
        position.value = -1
        //countSoalId.value = 0
    }

    fun nextBoxExists(): List<Int> {
        val pos = getCurrent()
        val batas = mutableListOf<Int>()
        val range = mutableListOf<Int>()
        //range batas kanan
        for (i in 0 until xLen) {
            batas.add((i * xLen) - 1)
        }

        val sisa = if (pos < xLen) (batas.size - pos) //+ " -> " + range.toString()
        else (batas.size - (pos.mod(xLen))) //+ " -> " + range.toString()

        for (i in pos..(pos + sisa) - 1) {
            range.add(i)
        }

//        return "Available: ${sisa} -> ${range}"
        return range
    }

    fun getAvailableRight(): Int {
        return nextBoxExists().count()
    }

    fun downBoxExists(): List<Int> {
        val pos = getCurrent()
        val range = mutableListOf<Int>()
        //read next
        for (i in 0 until yLen) {
            val x = if (i == 0) (pos)
            else range[i - 1] + xLen
            range.add(x)
        }
        //val count = range.count() { it < (xLen * yLen) }
        //val result = "Available : ${count} -> ${box}"
        return range.filter { it < 100 }
    }

    fun getAvailableDown(): Int {
        return downBoxExists().count()
    }

    fun moveNext(): Int {
        val pos = getCurrent()
        val rm = mutableListOf<Int>()
        for (i in 1 until yLen) {
            rm.add((i * xLen) - 1)
        }

        if (pos in rm || pos == countXY - 1) return 0
        return pos + 1
    }

    fun moveDown(): Int {
        val pos = getCurrent()
        if (pos < yLen * (xLen - 1)) return pos + yLen
        return 0
    }

    fun isHasNext(): Boolean {
        val x = moveNext()
        return x != 0
    }

    fun isHasDown(): Boolean {
        val x = moveDown()
        return x != 0
    }

    fun getNewQuestionerID(): String {
        return UUID.randomUUID().toString()
    }

    private fun setPrev(int: Int) {
        prevPos = int
    }

    fun getCurrent(): Int {
        return position.value!!
    }

    fun setCurrent(int: Int) {
        position.value = int
    }

    fun setNewLevelId() {
        if (questions.isNotEmpty()) {
            val sf = questions.last().levelId
            sf?.map { it }?.forEach() {
                levelId = (it + 1).toString()
            }
        } else {
            levelId = "1"
        }
    }

    fun Context.setBoxView(boxSet: BoxSet) {
        val selectedColor = ContextCompat.getColor(this, R.color.selected)
        val unSelectColor = ContextCompat.getColor(this, R.color.active)
        when (boxSet) {
            BoxSet.CLEAR_TEXT -> {
                for (i in 0 until boxView.size) {
                    boxView[i].text = ""
                }
            }

            BoxSet.TEXT_ID -> {
                for (i in 0 until boxView.size) {
                    boxView[i].text = i.toString()
                }
            }

            BoxSet.COLOR_UNSELECT -> {
                //if (prevPos in 0..boxView.size) boxView[prevPos].setBackgroundColor(unSelectColor)
            }

            BoxSet.COLOR_SELECTED -> {
                if (getCurrent() in 0..boxView.size) boxView[getCurrent()].setBackgroundColor(
                    selectedColor
                )
                //setPrev(getCurrent())
            }
            BoxSet.COLOR_RANGE_UNSELECT -> {
                for (i in 0 until boxView.size){
                    boxView[i].setBackgroundColor(unSelectColor)
                }
            }

            else -> {}
        }
    }
}