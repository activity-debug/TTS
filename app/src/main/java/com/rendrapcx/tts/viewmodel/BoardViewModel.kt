package com.rendrapcx.tts.viewmodel

import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rendrapcx.tts.R
import com.rendrapcx.tts.constant.ColorAttr
import com.rendrapcx.tts.constant.Direction
import com.rendrapcx.tts.constant.InputDirection
import com.rendrapcx.tts.constant.InputMode
import com.rendrapcx.tts.constant.TextAttr
import com.rendrapcx.tts.databinding.ActivityCreatorBinding
import com.rendrapcx.tts.model.Data


class BoardViewModel : ViewModel() {
    var position = MutableLiveData<Int>()
    var box = arrayListOf<TextView>()


    private val xLen = 10
    private val yLen = 10
    private var countXY = (xLen * yLen)

    private var questions = Data.listQuestion
    private var partial = Data.listPartial

    var levelId = ""

    //var idsAt = ""
    var direction = Direction.HORIZONTAL.name
    var inputMode = InputMode.NEW
    var inputDirection = InputDirection.UNKNOWN
    var prevPos = 0
    private var tipTop = true
    private var clip = ""

    var currentRange = arrayListOf<Int>()

    init {
        position.value = 0
    }

    fun setClip(clip: String) {
        this.clip = clip
    }

    fun getClip(): String {
        return clip
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
            sf.map { it }.forEach() {
                levelId = (it + 1).toString()
            }
        } else {
            levelId = "1"
        }
    }

    fun getQuestionId(): String {
        val pos = getCurrent()
        var partAt = ""
        Data.listPartial.filter { it.levelId == levelId }
            .filter { it.charAt == pos } //.ifEmpty { return partAt }
            .forEach {
                partAt = it.colQuestionId.ifEmpty { it.rowQuestionId }
            }
        return partAt
    }

    fun getRowId(): String {
        val pos = getCurrent()
        var rowId = ""
        Data.listPartial.filter { it.levelId == levelId }
            .filter { it.charAt == pos }
            .forEach {
                rowId = it.rowQuestionId.ifEmpty { "" }
            }
        return rowId
    }

    fun getColumnId(): String {
        val pos = getCurrent()
        var colId = ""
        Data.listPartial.filter { it.levelId == levelId }
            .filter { it.charAt == pos }
            .forEach {
                colId = it.colQuestionId.ifEmpty { "" }
            }
        return colId
    }

    private fun setInputRangeDirection() {
            if (getColumnId() != "" && getRowId() == "") InputDirection.COLUMN
            else if (getRowId() != "" && getColumnId() == "") InputDirection.ROW
            else if (getColumnId() == "" && getColumnId() == "") InputDirection.UNKNOWN
            else { inputDirection =
                    if (tipTop) InputDirection.COLUMN
                    else InputDirection.ROW }
    }

    private fun getRowRange(): ArrayList<Int> {
        var range = arrayListOf<Int>()
        Data.listQuestion.filter { it.levelId == levelId }
            .filter { it.id == getRowId() }
            .ifEmpty { return range }
            .map { it }.forEach { it ->
                range = it.slot
            }
        return range
    }

    private fun getColumnRange(): ArrayList<Int> {
        var range = arrayListOf<Int>()
        Data.listQuestion.filter { it.levelId == levelId }
            .filter { it.id == getColumnId() }
            .ifEmpty { return range }
            .map { it }.forEach { it ->
                range = it.slot
            }
        return range
    }


    private fun colorizeRange(context: Context, pos: Int, range: ArrayList<Int>) {
        if (pos in range) {
            for (i in range.indices) {
                val x = range[i]
                box[x].setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.selected_range
                    )
                )
            }
        }
    }


    fun boxRowOvers(): List<Int> {
        val pos = getCurrent()
        val batas = mutableListOf<Int>()
        val range = mutableListOf<Int>()

        /* range batas kanan */
        for (i in 0 until xLen) {
            batas.add((i * xLen) - 1)
        }

        val sisa = if (pos < xLen) (batas.size - pos)
        else (batas.size - (pos.mod(xLen)))

        for (i in pos..<(pos + sisa)) {
            range.add(i)
        }
        return range
    }

    fun boxColumnOvers(): List<Int> {
        val pos = getCurrent()
        val range = mutableListOf<Int>()

        /* read next */
        for (i in 0 until yLen) {
            val x = if (i == 0) (pos)
            else range[i - 1] + xLen
            range.add(x)
        }
        return range.filter { it < 100 }
    }

    fun selectNextRow(): Int {
        val pos = getCurrent()
        val rm = mutableListOf<Int>()
        for (i in 1 until yLen) {
            rm.add((i * xLen) - 1)
        }

        if (pos in rm || pos == countXY - 1) return 0
        return pos + 1
    }

    fun selectNextColumn(): Int {
        val pos = getCurrent()
        if (pos < yLen * (xLen - 1)) return pos + yLen
        return 0
    }

    fun isRowOvers(): Boolean {
        val x = selectNextRow()
        return x != 0
    }

    fun isColumnOvers(): Boolean {
        val x = selectNextColumn()
        return x != 0
    }

    fun boxText(textAttr: TextAttr) {
        when (textAttr) {
            TextAttr.CLEAR_TEXT -> {
                for (i in 0 until box.size) {
                    box[i].text = ""
                }
            }

            TextAttr.FILL_TEXT -> {
                Data.listPartial.filter { it.levelId == levelId }.map { it }.forEach() {
                    box[it.charAt].text = it.char.uppercase()
                }
            }
        }
    }


    fun boxColor(context: Context, binding: ActivityCreatorBinding, colorAttr: ColorAttr) {
        when (colorAttr) {
            ColorAttr.COLOR_ACTIVE -> {
                for (i in 0 until box.size) {
                    if (box[i].text.isNotEmpty()) {
                        box[i].setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                        box[i].setTextColor(ContextCompat.getColor(context, R.color.black))
                    }
                }
            }

            ColorAttr.COLOR_DISABLE -> {
                for (i in 0 until box.size) {
                    box[i].setBackgroundColor(ContextCompat.getColor(context, R.color.disable))
                    box[i].setTextColor(ContextCompat.getColor(context, R.color.disable))
                }
            }

            ColorAttr.COLOR_SELECTED -> {
                val i = getCurrent()
                box[i].setBackgroundColor(ContextCompat.getColor(context, R.color.selected))
                box[i].setTextColor(ContextCompat.getColor(context, R.color.white))
            }

            ColorAttr.COLOR_RANGE_SELECT -> {
                var range = arrayListOf<Int>()
                tipTop = tipTop != true
                setInputRangeDirection()
//                if (range.isEmpty()) return

                if (inputDirection == InputDirection.COLUMN) {
                    range = getColumnRange()
                    colorizeRange(context, getCurrent(), range)
                    currentRange = range
                } else if (inputDirection == InputDirection.ROW) {
                    range = getRowRange()
                    colorizeRange(context, getCurrent(), range)
                    currentRange = range
                }


            }

            ColorAttr.COLOR_BACKGROUND -> {
                binding.included1.boardTen
                    .setBackgroundColor(ContextCompat.getColor(context, R.color.background))
            }
        }

    }


}


////////
//val pos = getCurrent()
//val range = itSLot
//if (pos in range) {
//    for (i in range.indices) {
//        val x = range[i]
//        box[x].setBackgroundColor(
//            ContextCompat.getColor(
//                context,
//                R.color.selected_range
//            )
//        )
//    }
//}