package com.rendrapcx.tts.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getColor
import com.rendrapcx.tts.R
import com.rendrapcx.tts.constant.Const.BoardSet
import com.rendrapcx.tts.constant.Const.Companion.boardSet
import com.rendrapcx.tts.constant.Const.Companion.currentLevel
import com.rendrapcx.tts.constant.Const.InputAnswerDirection
import com.rendrapcx.tts.constant.Direction
import com.rendrapcx.tts.constant.InputDirection
import com.rendrapcx.tts.constant.SelectRequest
import com.rendrapcx.tts.databinding.ActivityBoardBinding
import com.rendrapcx.tts.helper.Dialog
import com.rendrapcx.tts.helper.Helper
import com.rendrapcx.tts.helper.Keypad
import com.rendrapcx.tts.model.Data.Companion.listPartial
import com.rendrapcx.tts.model.Data.Companion.listQuestion

class BoardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBoardBinding
    private var box = arrayListOf<TextView>()
    private var position = 0
    private var inputAnswerDirection = InputAnswerDirection.ROW
    private var tipTop = true
    private val xLen = 10
    private val yLen = 10
    private var countXY = (xLen * yLen)
    private var currentQuestId = ""
    private var currentIndex = 0
    private var currentRange = arrayListOf<Int>()
    private var pickByArrow = false
    private var tagMap = mutableMapOf<Int, String>()
    private var tag = arrayListOf<Int>()

    private var selectedQuestion = ""
    private var onType = false
    private var softKeyboard = true

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Helper().apply { hideSystemUI() }

        initBoardChild()

        if (boardSet == BoardSet.EDITOR) {
            initLayoutEditor()
        } else {
            initLayoutPlay()
            setBoxTagText()

            position = listPartial.filter { it.levelId == currentLevel }.first().charAt

            pickByArrow = false
            if (getColumnId() != "") inputAnswerDirection = InputAnswerDirection.COLUMN
            else if (getRowId() != "") inputAnswerDirection = InputAnswerDirection.ROW
            setOnSelectedColor()
            setOnRangeColor()
            binding.includeQuestionSpan.tvSpanQuestion.text = selectedQuestion
        }

        binding.includeHeader.apply {
            btnBack.setOnClickListener() {
                when (boardSet) {
                    BoardSet.PLAY -> {
                        val i = Intent(this@BoardActivity, QuestionActivity::class.java)
                        startActivity(i)
                        finish()
                    }

                    BoardSet.EDITOR -> {
                        val i = Intent(this@BoardActivity, MainActivity::class.java)
                        startActivity(i)
                        finish()
                    }
                }
            }

        }

        binding.includeBoard.boardTen.setOnClickListener() {
            for (i in 0 until box.size) {
                box[i].setOnClickListener() {
                    position = i
                    box[i].isFocusable
                    pickByArrow = false
                    if (getColumnId() != "") inputAnswerDirection = InputAnswerDirection.COLUMN
                    else if (getRowId() != "") inputAnswerDirection = InputAnswerDirection.ROW
                    setOnSelectedColor()
                    setOnRangeColor()
                    showPartInfo()
                    binding.includeQuestionSpan.tvSpanQuestion.text = selectedQuestion
                    Keypad().showSoftKeyboard(window, it)
                }
            }
        }

        // FIXME: SELECT QUESTION BY ARROW
        binding.includeQuestionSpan.apply {
            btnNextQuestion.setOnClickListener() {
                resetBoxColor()
                getRequestQuestions(SelectRequest.NEXT)
                setOnSelectedColor()
                setOnRangeColor()
                binding.includeQuestionSpan.tvSpanQuestion.text = selectedQuestion
            }
            btnPrevQuestion.setOnClickListener() {
                resetBoxColor()
                getRequestQuestions(SelectRequest.PREV)
                setOnSelectedColor()
                setOnRangeColor()
                binding.includeQuestionSpan.tvSpanQuestion.text = selectedQuestion
            }
            tvSpanQuestion.setOnClickListener() {
                checkWinCondition()
            }
        }

        /* FIXME: EDITOR BINDING ACTION*/
        binding.includeEditor.apply {
            if (boardSet == BoardSet.PLAY) return
            // TODO:
        }

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val x = position
        onType = true
        when (keyCode) {
            in 29..54 -> {
                val s = event?.displayLabel
                box[x].text = s.toString()
                if (inputAnswerDirection == InputAnswerDirection.ROW) {
                    //if (isRowOvers()) {  //fixme: jangan dulu hapus, cek di EDITOR dulu
                    position = selectNextRow()
                    if (position in currentRange) {
                        setOnSelectedColor()
                    } else position = x
                    //}
                }
                if (inputAnswerDirection == InputAnswerDirection.COLUMN) {
                    //if (isColumnOvers()) {
                    position = selectNextColumn()
                    if (position in currentRange) {
                        setOnSelectedColor()
                    } else position = x
                    //}
                }
                onType = false
                checkWinCondition(false)
            }

            67 -> {
                val s = event?.displayLabel
                box[x].text = s.toString()
                if (inputAnswerDirection == InputAnswerDirection.ROW) {
                    //if (isRowOvers()) {
                    position = x - 1
                    if (position in currentRange) {
                        setOnSelectedColor()
                    } else position = currentRange[0]
                    //}
                }
                if (inputAnswerDirection == InputAnswerDirection.COLUMN) {
                    //if (isColumnOvers()) {
                    position = x - xLen
                    if (position in currentRange) {
                        setOnSelectedColor()
                    } else position = currentRange[0]
                    //}
                }
                onType = false
            }
            else -> return false
        }
        return super.onKeyDown(keyCode, event)
    }

    /* TODO: CHECK WIN*/
    private fun checkWinCondition(color: Boolean = true) {
        if (boardSet == BoardSet.EDITOR) return
        var pass = true
        for (i in tag) {
//            if (box[i].text == box[i].tag) {
//                // TODO: if setting enable to cek colorize then do this
//                box[i].setBackgroundColor(getColor(this, R.color.pass))
//            }
            if (box[i].text != box[i].tag) {
                if (color) box[i].setBackgroundColor(getColor(this, R.color.not_pass))
                pass = false
            }
        }
        if (pass) {
            Helper().alertDialog(this, "HOREEE", "YOU WIN")
            // TODO: 1. win dialog, 2. next question or back to list, 3. update score 4. update user data level finished

            Dialog().apply { winDialog(this@BoardActivity, binding) }

        }
    }

    /* FIXME: GET ID QUESTION DARI PARTIAL Row ID */
    private fun getRowId(): String {
        val pos = position
        var rowId = ""
        listPartial.filter { it.levelId == currentLevel }
            .filter { it.charAt == pos }
            .forEach {
                rowId = it.rowQuestionId.ifEmpty { "" }
            }

        currentQuestId = rowId
        return rowId
    }


    /* FIXME: GET ID QUESTION DARI PARTIAL Column ID */
    private fun getColumnId(): String {
        val pos = position
        var colId = ""
        listPartial.filter { it.levelId == currentLevel }
            .filter { it.charAt == pos }
            .forEach {
                colId = it.colQuestionId.ifEmpty { "" }
            }
        currentQuestId = colId
        return colId
    }

    private fun setInputRangeDirection() {
        if (getColumnId() != "" && getRowId() == "") InputDirection.COLUMN
        else if (getRowId() != "" && getColumnId() == "") InputDirection.ROW
        else if (getColumnId() == "" && getColumnId() == "") InputDirection.UNKNOWN
        else {
            inputAnswerDirection =
                if (tipTop) InputAnswerDirection.COLUMN
                else InputAnswerDirection.ROW
        }
    }

    private fun getRowRange(): ArrayList<Int> {
        var range = arrayListOf<Int>()
        listQuestion.filter { it.levelId == currentLevel }
            .filter { it.id == getRowId() }
            .ifEmpty { return range }
            .map { it }.forEach { it ->
                range = it.slot
            }
        return range
    }

    private fun getColumnRange(): ArrayList<Int> {
        var range = arrayListOf<Int>()
        listQuestion.filter { it.levelId == currentLevel }
            .filter { it.id == getColumnId() }
            .ifEmpty { return range }
            .map { it }.forEach { it ->
                range = it.slot
            }
        return range
    }


    fun boxRowOvers(): List<Int> {
        val pos = position
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
        val pos = position
        val range = mutableListOf<Int>()

        /* read next */
        for (i in 0 until yLen) {
            val x = if (i == 0) (pos)
            else range[i - 1] + xLen
            range.add(x)
        }
        return range.filter { it < 100 }
    }

    private fun selectNextRow(): Int {
        val pos = position
        val rm = mutableListOf<Int>()
        for (i in 1 until yLen) {
            rm.add((i * xLen) - 1)
        }

        if (pos in rm || pos == countXY - 1) return 0
        return pos + 1
    }

    private fun selectNextColumn(): Int {
        val pos = position
        if (pos < yLen * (xLen - 1)) return pos + yLen
        return 0
    }

    private fun isRowOvers(): Boolean {
        val x = selectNextRow()
        return x != 0
    }

    private fun isColumnOvers(): Boolean {
        val x = selectNextColumn()
        return x != 0
    }


    private fun getQuestion(): String {
        var id = getRowId()
        if (inputAnswerDirection == InputAnswerDirection.ROW) id = getRowId()
        else if (inputAnswerDirection == InputAnswerDirection.COLUMN) id = getColumnId()

        var result = ""
        listQuestion.filter { it.levelId == currentLevel }
            .filter { it.id == id }
            .map { it }.forEach() {
                result = it.asking
            }
        return result
    }

    private fun getRequestQuestions(selectRequest: SelectRequest) {
        val index = if (currentIndex < 0) {
            listQuestion.indexOfFirst { it.levelId == currentLevel && it.id == currentQuestId }
        } else currentIndex

        val count = listQuestion.count() { it.levelId == currentLevel }

        val req = if (selectRequest == SelectRequest.NEXT) {
            if (index < count - 1) index + 1 else 0
        } else {
            if (index > 0) index - 1 else count - 1
        }

        currentIndex = req

        val reqId = listQuestion.filter { it.levelId == currentLevel }[req].id

        var range = arrayListOf<Int>()
        var dir = Direction.HORIZONTAL.name
        listQuestion.filter { it.levelId == currentLevel && it.id == reqId }
            .map { it }.forEach() {
                dir = it.direction
                range = it.slot
            }

        currentQuestId = reqId

        inputAnswerDirection = if (dir == Direction.HORIZONTAL.name) InputAnswerDirection.ROW
        else InputAnswerDirection.COLUMN

        currentRange = range
        position = range[0]

        pickByArrow = true
    }

    private fun showQuestionInfo() {
        binding.includeEditor.apply {
            textLevelId.text = ""
            textNumber.text = ""
            textDIrection.text = ""
            textAsking.text = ""
            textAnswer.text = ""
            textSlot.text = ""

            listQuestion.filter { it.levelId == currentLevel }
                .forEach() {
                    textLevelId.text = it.levelId
                    textNumber.text = it.number.toString()
                    textDIrection.text = it.direction
                    textAsking.text = it.asking
                    textAnswer.text = it.answer
                    textSlot.text = it.slot.toString()
                }
        }
    }

    private fun showPartInfo() {
        binding.includeEditor.apply {
            textCharAt.text = ""
            textCharStr.text = ""
            textRowId.text = ""
            textColId.text = ""

            listPartial.filter { it.levelId == currentLevel && it.charAt == position }.forEach() {
                textCharAt.text = it.charAt.toString()
                textCharStr.text = it.charStr
                textRowId.text = it.rowQuestionId
                textColId.text = it.colQuestionId
            }
        }
    }

    private fun setBoxTagText() {
        tag.clear()
        listPartial.forEach() {
            for (i in 0 until box.size) {
                if (i == it.charAt) {
                    box[i].tag = it.charStr
                    tag.add(it.charAt)
                    tagMap.put(it.charAt, it.charStr)
                }
            }
        }
        resetBoxColor()
    }

    private fun setColorizeRange(pos: Int, range: ArrayList<Int>) {
        val current = position
        if (pos in range) {
            for (i in range.indices) {
                val x = range[i]
                if (x == current) continue
                box[x].setBackgroundColor(getColor(this, R.color.selected_range))
            }
        }
    }

    private fun setOnRangeColor() {
        val range: ArrayList<Int>

        if (!pickByArrow) {
            tipTop = tipTop != true
            setInputRangeDirection()
        }

        if (inputAnswerDirection == InputAnswerDirection.COLUMN) {
            range = getColumnRange()
            setColorizeRange(position, range)
            currentRange = range
        } else if (inputAnswerDirection == InputAnswerDirection.ROW) {
            range = getRowRange()
            setColorizeRange(position, range)
            currentRange = range
        }

        selectedQuestion = getQuestion()
        pickByArrow = false
    }

    private fun setOnSelectedColor() {
        resetBoxColor()
        val i = position
        box[i].setTextColor(getColor(this, R.color.white))
        box[i].setBackgroundColor(getColor(this, R.color.selected))
    }

    private fun resetBoxColor() {
        for (i in 0 until box.size) {
            if (box[i].tag == "") box[i].visibility = View.INVISIBLE
            box[i].setTextColor(getColor(this, R.color.black))
            box[i].setBackgroundColor(getColor(this, R.color.white))
        }
    }

    /************************************************************************
     * INITIAL LAYOUT AND COMPONENTS
     * ***********************************************************************/
    private fun initLayoutPlay() {
        binding.includeEditor.mainContainer.visibility = View.GONE

        binding.includeHeader.tvLabelTop.text = currentLevel
        binding.includeQuestionSpan.tvSpanQuestion.text = ""

        binding.includeBoard.boardTen.setBackgroundColor(getColor(this, R.color.background))

    }

    private fun initLayoutEditor() {
        binding.includeEditor.mainContainer.visibility = View.VISIBLE

        binding.includeHeader.tvLabelTop.text = ""
        binding.includeQuestionSpan.tvSpanQuestion.text = ""

        binding.includeBoard.boardTen.setBackgroundColor(getColor(this, R.color.background))

        binding.includeEditor.apply {
            textInfo.text = ""
            textLevelId.text = ""
            textQuestionId.text = ""
            textNumber.text = ""
            textDIrection.text = ""
            textAsking.text = ""
            textAnswer.text = ""
            textSlot.text = ""
            textCharAt.text = ""
            textCharStr.text = ""
            textRowId.text = ""
            textColId.text = ""
        }

    }

    private fun initBoardChild() {
        val count = binding.includeBoard.boardTen.childCount
        for (i in 0 until count) {
            val child = binding.includeBoard.boardTen.getChildAt(i)
            if (child is TextView) {
                box.add(child)
                box[i].text = ""
                box[i].tag = ""
            }
        }
    }
}