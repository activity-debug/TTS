package com.rendrapcx.tts.viewmodel

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rendrapcx.tts.R
import com.rendrapcx.tts.constant.ColorAttr
import com.rendrapcx.tts.constant.Direction
import com.rendrapcx.tts.constant.GameState
import com.rendrapcx.tts.constant.InputDirection
import com.rendrapcx.tts.constant.InputMode
import com.rendrapcx.tts.constant.SelectRequest
import com.rendrapcx.tts.constant.TextAttr
import com.rendrapcx.tts.databinding.ActivityCreatorBinding
import com.rendrapcx.tts.model.Data
import java.util.UUID


class BoardViewModel(
) : ViewModel() {

    var position = MutableLiveData<Int>()
    var box = arrayListOf<TextView>()

    var gameState = GameState.CREATOR

    var sTemp = arrayListOf<Int>()

    private val xLen = 10
    private val yLen = 10
    private var countXY = (xLen * yLen)

    var levelId = ""

    var direction = Direction.HORIZONTAL.name
    var inputMode = InputMode.NEW
    var inputDirection = InputDirection.UNKNOWN
    var prevPos = 0
    private var tipTop = true
    private var clip = ""

    var selectedQuestion = ""

    var currentRange = arrayListOf<Int>()

    var currentQuestId = MutableLiveData<String>()
    var currentIndex = MutableLiveData<Int>()

    var pickByArrow = false


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

    fun newLevelId() {
        levelId = UUID.randomUUID().toString().substring(0,10)
    }

    fun getFlipQuestionId(): String {
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

        setCurrentQuestId(rowId)
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
        setCurrentQuestId(colId)
        return colId
    }

    private fun setInputRangeDirection() {
        if (getColumnId() != "" && getRowId() == "") InputDirection.COLUMN
        else if (getRowId() != "" && getColumnId() == "") InputDirection.ROW
        else if (getColumnId() == "" && getColumnId() == "") InputDirection.UNKNOWN
        else {
            inputDirection =
                if (tipTop) InputDirection.COLUMN
                else InputDirection.ROW
        }
    }

    fun getRowRange(): ArrayList<Int> {
        sTemp.clear()
        var range = arrayListOf<Int>()
        Data.listQuestion.filter { it.levelId == levelId }
            .filter { it.id == getRowId() }
            .ifEmpty { return range }
            .map { it }.forEach { it ->
                range = it.slot
            }
        return range
    }

    fun getColumnRange(): ArrayList<Int> {
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
        val current = getCurrent()
        if (pos in range) {
            for (i in range.indices) {
                val x = range[i]
                if (x == current) continue
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


    private fun getQuestion(): String {
        var id = getRowId()
        if (inputDirection == InputDirection.ROW) id = getRowId()
        else if (inputDirection == InputDirection.COLUMN) id = getColumnId()

        var result = ""
        Data.listQuestion.filter { it.levelId == levelId }
            .filter { it.id == id }
            .map { it }.forEach() {
                result = it.asking
            }
        return result
    }

    fun setCurrentQuestId(id: String) {
        currentQuestId.value = id
    }

    fun getCurrentQuestId(): String {
        return currentQuestId.value!!
    }

    fun getRequestQuestions(selectRequest: SelectRequest) {
        val index = if (currentIndex.value!! < 0) {
            Data.listQuestion.indexOfFirst { it.levelId == levelId && it.id == getCurrentQuestId() }
        } else currentIndex.value

        val count = Data.listQuestion.count() { it.levelId == levelId }

        val req = if (selectRequest == SelectRequest.NEXT) {
            if (index!! < count - 1) index + 1 else 0
        } else {
            if (index!! > 0) index - 1 else count - 1
        }

        currentIndex.value = req

        val reqId = Data.listQuestion.filter { it.levelId == levelId }[req].id

        var range = arrayListOf<Int>()
        var dir = Direction.HORIZONTAL.name
        Data.listQuestion.filter { it.levelId == levelId && it.id == reqId }
            .map { it }.forEach() {
                dir = it.direction
                range = it.slot
            }

        setCurrentQuestId(reqId)

        inputDirection = if (dir == Direction.HORIZONTAL.name) InputDirection.ROW
        else InputDirection.COLUMN

        currentRange = range

        setCurrent(range[0])
        pickByArrow = true
    }


    fun boxColor(context: Context, binding: ActivityCreatorBinding, colorAttr: ColorAttr) {
        when (colorAttr) {
            ColorAttr.COLOR_ACTIVE -> {
                for (i in 0 until box.size) {
                    if (box[i].text.isNotEmpty() || box[i].tag !=0 ) {
                        box[i].setTextColor(ContextCompat.getColor(context, R.color.black))
                        box[i].setBackgroundColor(ContextCompat.getColor(context, R.color.white))
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
                box[i].setTextColor(ContextCompat.getColor(context, R.color.white))
                box[i].setBackgroundColor(ContextCompat.getColor(context, R.color.selected))
            }

            ColorAttr.COLOR_RANGE_SELECT -> {

                val range: ArrayList<Int>

                if (!pickByArrow) {
                    tipTop = tipTop != true
                    setInputRangeDirection()
                }

                if (inputDirection == InputDirection.COLUMN) {
                    range = getColumnRange()
                    colorizeRange(context, getCurrent(), range)
                    currentRange = range
                } else if (inputDirection == InputDirection.ROW) {
                    range = getRowRange()
                    colorizeRange(context, getCurrent(), range)
                    currentRange = range
                }

                selectedQuestion = getQuestion()
                pickByArrow = false
            }

            ColorAttr.COLOR_BACKGROUND -> {
                binding.included1.boardTen
                    .setBackgroundColor(ContextCompat.getColor(context, R.color.background))
            }
        }

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

            TextAttr.FILL_TAG -> {
                for (i in 0 until box.size) box[i].tag = 0
                Data.listPartial.filter { it.levelId == levelId }.map { it }.forEach() {
                    box[it.charAt].tag = it.char.uppercase()
                }
            }
        }
    }

    fun boxVisibility() {
        for (i in 0 until box.size) {
            if (box[i].tag == 0) {
                box[i].visibility = View.INVISIBLE
            }
        }
    }
}