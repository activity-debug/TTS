package com.rendrapcx.tts.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.coroutineScope
import com.rendrapcx.tts.R
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.constant.Const.BoardSet
import com.rendrapcx.tts.constant.Const.Companion.boardSet
import com.rendrapcx.tts.constant.Const.Companion.currentIndex
import com.rendrapcx.tts.constant.Const.Companion.currentLevel
import com.rendrapcx.tts.constant.Const.Companion.position
import com.rendrapcx.tts.constant.Const.InputAnswerDirection
import com.rendrapcx.tts.constant.Const.InputQuestionDirection
import com.rendrapcx.tts.constant.Direction
import com.rendrapcx.tts.constant.InputDirection
import com.rendrapcx.tts.constant.InputMode
import com.rendrapcx.tts.constant.SelectRequest
import com.rendrapcx.tts.databinding.ActivityBoardBinding
import com.rendrapcx.tts.databinding.DialogInputSoalBinding
import com.rendrapcx.tts.helper.Dialog
import com.rendrapcx.tts.helper.Helper
import com.rendrapcx.tts.helper.Keypad
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.model.Data.Companion.listLevel
import com.rendrapcx.tts.model.Data.Companion.listPartial
import com.rendrapcx.tts.model.Data.Companion.listQuestion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class BoardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBoardBinding
    private var box = arrayListOf<TextView>()
    private var intKey = arrayListOf<TextView>()
    private var inputAnswerDirection = InputAnswerDirection.ROW
    private var inputQuestionDirection = InputQuestionDirection.HORIZONTAL.name
    private var tipTop = true
    private val xLen = 10
    private val yLen = 10
    private var countXY = (xLen * yLen)
    private var currentQuestId = ""
    private var currentRange = arrayListOf<Int>()
    private var pickByArrow = false
    private var tagMap = mutableMapOf<Int, String>()
    private var tag = arrayListOf<Int>()
    private var clip = ""

    private var selectedQuestion = ""
    private var onType = false
    private var softKeyboard = true

    private var curPartId = ""
    private var curRowId = ""
    private var curColId = ""
    private var curCharAt = 0
    private var curCharStr = ""

    private var curQuestion = ""

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Helper().apply { hideSystemUI() }

        initBoardChild()
        initIntKeyChild()

        when (boardSet) {
            BoardSet.EDITOR_NEW -> {
                initLayoutEditor()
                currentLevel = UUID.randomUUID().toString().substring(0, 20)
                binding.includeHeader.tvLabelTop.text = currentLevel
                setBoxTagText()
                position = 0
                listLevel.add(
                    Data.Level(currentLevel, "2024", "Title", "Admin")
                )
                fillTextDescription()
                setOnSelectedColor()
            }

            BoardSet.EDITOR_EDIT -> {
                initLayoutEditor()
                setBoxTagText()

                position = listPartial.first { it.levelId == currentLevel }.charAt

                fillTextDescription()

                getInputAnswerDirection()
                onClickBox()
            }

            else -> {
                initLayoutPlay()
                setBoxTagText()

                position = listPartial.first { it.levelId == currentLevel }.charAt

                getInputAnswerDirection()
                onClickBox()
            }
        }

        binding.includeKeyboard.apply {
            btnBackSpace.setOnClickListener(){
                Toast.makeText(this@BoardActivity, "BACKSPACE", Toast.LENGTH_SHORT).show()
            }
            btnShuffle.setOnClickListener(){
                Toast.makeText(this@BoardActivity, "SHUFFLE", Toast.LENGTH_SHORT).show()
            }
            //KEYBOARD PRESS
            for (i in 0 until intKey.size){
                intKey[i].setOnClickListener(){
                    Toast.makeText(this@BoardActivity, "${i}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        /* TODO: HEADER ACTIONS*/
        binding.includeHeader.apply {
            btnBack.setOnClickListener() {
                val i = Intent(this@BoardActivity, QuestionActivity::class.java)
                startActivity(i)
                finish()
            }
            btnSettingPlay.setOnClickListener() {
                Dialog().apply { settingDialog(this@BoardActivity) }
            }
        }

        /* TODO: BOX CLICK ACTION*/
        binding.includeBoard.boardTen.setOnClickListener() {
            for (i in 0 until box.size) {
                box[i].setOnClickListener() {
                    pickByArrow = false
                    position = i
                    getInputAnswerDirection()
                    onClickBox()
                    when (boardSet) {
                        BoardSet.EDITOR_NEW, BoardSet.EDITOR_EDIT -> {
                            if (clip.isNotEmpty()) pasteId()
                        }

                        BoardSet.PLAY -> {
                            Keypad().showSoftKeyboard(window, it)
                        }

                        else -> {}
                    }
                }

                box[i].setOnLongClickListener() {
                    if (clip.isEmpty()) {
                        clip = curRowId.ifEmpty { curColId }
                        Helper().apply {
                            alertDialog(
                                this@BoardActivity,
                                "Select box to paste"
                            )
                        }
                    }
                    return@setOnLongClickListener true
                }
            }
        }

        // FIXME: SELECT QUESTION BY ARROW
        binding.includeQuestionSpan.apply {
            btnNextQuestion.setOnClickListener() {
                resetBoxColor()
                if (boardSet != BoardSet.PLAY) fillText()
                getRequestQuestions(SelectRequest.NEXT)
                onClickBox()
            }
            btnPrevQuestion.setOnClickListener() {
                resetBoxColor()
                if (boardSet != BoardSet.PLAY) fillText()
                getRequestQuestions(SelectRequest.PREV)
                onClickBox()
            }
            tvSpanQuestion.setOnClickListener() {
                checkWinCondition()
            }
        }

        /* FIXME: EDITOR BINDING ACTION*/
        binding.includeEditor.apply {
            if (boardSet == BoardSet.PLAY) return
            // TODO:

            btnSave.setOnClickListener() {
                if (listQuestion.isEmpty()) {
                    Helper().apply { alertDialog(this@BoardActivity, "Data masih kosong") }
                } else {
                    saveAndApply()
                }
            }

            btnAdd.setOnClickListener() {
                Const.inputMode = InputMode.NEW.name
                inputDataQuestioner(
                    position,
                    boxRowOvers().count(),
                    boxRowOvers(),
                    boxColumnOvers().count(),
                    boxColumnOvers(),
                )
            }

            btnEdit.setOnClickListener() {
                Dialog().apply { inputDescription(binding) }
            }
        }



    }

    private fun fillTextDescription(){
        listLevel.filter { it.id == Const.currentLevel }.forEach() {
            binding.includeEditor.apply {
                textLevelId.text = it.id
                textCategory.text = it.category
                textTitle.text = it.title
                textCreator.text = it.userId
            }
        }
    }

    private fun pasteId() {
        if (curRowId.isEmpty()) {
            listPartial.filter { it.levelId == currentLevel }
                .filter { it.id == curPartId }
                .map {
                    it.rowQuestionId = clip
                }
        }
        if (curColId.isEmpty()) {
            listPartial.filter { it.levelId == currentLevel }
                .filter { it.id == curPartId }
                .map {
                    it.colQuestionId = clip
                }
        }
        fillText()
        clip = ""
        Toast.makeText(this, "ID Copied, Clip cleared", Toast.LENGTH_SHORT).show()
    }

    private fun getInputAnswerDirection() {
        if (getColumnId() != "") inputAnswerDirection = InputAnswerDirection.COLUMN
        else if (getRowId() != "") inputAnswerDirection = InputAnswerDirection.ROW
    }

    private fun onClickBox() {
        if (boardSet != BoardSet.PLAY) fillText()

        setOnSelectedColor()
        setOnRangeColor()
        showPartInfo()
        binding.includeQuestionSpan.tvSpanQuestion.text = selectedQuestion
    }

    private fun saveAndApply() {
        val levelId = Const.currentLevel
        lifecycle.coroutineScope.launch {
            val level = DB.getInstance(applicationContext).level()
            level.insertLevel(
                level = Data.Level(
                    id = levelId,
                    category = binding.includeEditor.textCategory.text.toString(),
                    title = binding.includeEditor.textTitle.text.toString(),
                    userId = binding.includeEditor.textCreator.text.toString()
                )
            )
            delay(1000L)
        }

        lifecycle.coroutineScope.launch {
            Data.listQuestion.filter { it.levelId == levelId }.map { it }.forEach() {
                DB.getInstance(applicationContext).question().insertQuestion(
                    Data.Question(
                        levelId = it.levelId,
                        id = it.id,
                        number = it.number,
                        direction = it.direction,
                        asking = it.asking,
                        answer = it.answer,
                        slot = it.slot
                    )
                )
            }
            delay(1000L)
        }

        lifecycle.coroutineScope.launch {
            Data.listPartial.filter { it.levelId == levelId }.map { it }.forEach() {
                DB.getInstance(applicationContext).partial().insertPartial(
                    Data.Partial(
                        id = it.id,
                        charAt = it.charAt,
                        charStr = it.charStr,
                        rowQuestionId = it.rowQuestionId,
                        colQuestionId = it.colQuestionId,
                        levelId = levelId,
                    )
                )
            }
            delay(1000L)
        }
        Helper().apply { alertDialog(this@BoardActivity, "Data berhasil disimpan") }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val x = position
        when (keyCode) {
            in 29..54 -> {
                val s = event?.displayLabel
                box[x].text = s.toString()
                onPressAbjabMove()
                checkWinCondition(false)
            }

            67 -> {
                val s = event?.displayLabel
                box[x].text = s.toString()
                onPressBackSpace()
            }

            else -> return false
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun onPressBackSpace() {
        onType = true
        val x = position
        if (inputAnswerDirection == InputAnswerDirection.ROW) {
            position = x - 1
            if (position in currentRange) {
                setOnSelectedColor()
            } else position = currentRange[0]
        }
        if (inputAnswerDirection == InputAnswerDirection.COLUMN) {
            position = x - xLen
            if (position in currentRange) {
                setOnSelectedColor()
            } else position = currentRange[0]
        }
        onType = false
    }

    private fun onPressAbjabMove() {
        onType = true
        val x = position
        if (inputAnswerDirection == InputAnswerDirection.ROW) {
            position = selectNextRow()
            if (position in currentRange) {
                setOnSelectedColor()
            } else position = x
        }
        if (inputAnswerDirection == InputAnswerDirection.COLUMN) {
            position = selectNextColumn()
            if (position in currentRange) {
                setOnSelectedColor()
            } else position = x
        }
        onType = false
    }

    /* TODO: CHECK WIN*/
    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkWinCondition(color: Boolean = true) {
        if (boardSet != BoardSet.PLAY) return
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
        if (listQuestion.isEmpty()) return

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

    private fun showAnswerKeypad() {
        binding.includeEditor.apply {
            listQuestion.filter { it.levelId == currentLevel }
                .forEach() {
                    curQuestion = it.answer
                }
            binding.includeHeader.tvLabelTop.text = curQuestion




            for (i in 0 until intKey.size) {

            }


        }
    }

    private fun showPartInfo() {
        binding.includeEditor.apply {
            listPartial.filter { it.levelId == currentLevel && it.charAt == position }.forEach() {
                curPartId = it.id
                curCharAt = it.charAt
                curCharStr = it.charStr
                curRowId = it.rowQuestionId
                curColId = it.colQuestionId
            }
        }
        showAnswerKeypad()
    }

    private fun setBoxTagText() {
        when (boardSet) {
            BoardSet.EDITOR_NEW -> {
                for (i in 0 until box.size) {
                    box[i].text = ""
                }
                resetBoxColor()
            }

            BoardSet.EDITOR_EDIT -> {
                tag.clear()
                listPartial.filter { it.levelId == currentLevel }.forEach() {
                    for (i in 0 until box.size) {
                        if (i == it.charAt) {
                            box[i].text = it.charStr
                            box[i].tag = it.charStr
                            tag.add(it.charAt)
                            tagMap.put(it.charAt, it.charStr)
                        }
                    }
                }
                resetBoxColor()
            }

            BoardSet.PLAY -> {
                tag.clear()
                listPartial.filter { it.levelId == currentLevel }.forEach() {
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

            else -> {}
        }
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

    private fun fillText() {
        listPartial.filter { it.levelId == currentLevel }.map { it }.forEach() {
            for (i in 0 until box.size)
                if (i == it.charAt) {
                    box[i].text = it.charStr
                    box[i].tag = it.charStr
                    tag.add(it.charAt)
                    tagMap.put(it.charAt, it.charStr)
                }
        }
    }

    private fun resetBoxColor() {
        for (i in 0 until box.size) {
            if (box[i].tag == "" && boardSet == BoardSet.PLAY) box[i].visibility = View.INVISIBLE
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
        binding.includeKeyboard.integratedKeyboard.visibility = View.GONE

        binding.includeHeader.tvLabelTop.text = ""
        binding.includeQuestionSpan.tvSpanQuestion.text = ""


        binding.includeBoard.boardTen.setBackgroundColor(getColor(this, R.color.background))

        binding.includeEditor.containerInfo.visibility = View.GONE
        binding.includeEditor.containerPartial.visibility = View.GONE
    }

    private fun initIntKeyChild() {
        val count = binding.includeKeyboard.integratedKeyboard.childCount
        for (i in 0 until count) {
            val child = binding.includeKeyboard.integratedKeyboard.getChildAt(i)
            if (child is TextView) {
                intKey.add(child)
            }
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


    /*
    * INPUT DATA DENGAN DIALOG INTERNAL, KALO PAKE YANG EXTERNAL, GAK BISA MANGGIL onCLick,
    * walau kebaca di external dialog tapi error pas runtime, CARI TAU
    * TODO: cari tau cara manggil fungsi parent dari dialog
    * */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun inputDataQuestioner(
        position: Int,
        rowCount: Int,
        rowAvailable: List<Int>,
        colCount: Int,
        colAvailable: List<Int>,
    ) {

        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val bind = DialogInputSoalBinding.inflate(inflater)
        val builder = AlertDialog.Builder(this).setView(bind.root)
        val dialog = builder.create()
        val window = dialog.window

        val windowInsetsController =
            WindowCompat.getInsetsController(window!!, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
            if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
                || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())
            ) {
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            }
            view.onApplyWindowInsets(windowInsets)
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        //TODO: INITIAL NEW
        val rowFilter = arrayOf<InputFilter>(InputFilter.LengthFilter(rowCount))
        val colFilter = arrayOf<InputFilter>(InputFilter.LengthFilter(colCount))
        bind.etDirectionInput.visibility = View.INVISIBLE   //todo: kalo udah beres hapus komponenya
        bind.etNoInput.setText("${position}")
        bind.etAskInput.requestFocus()
        bind.etAnswerInput.filters = rowFilter
        bind.etAnswerInput.hint = "available ${rowCount} boxes"
        bind.tvSlotPreview.text = "${rowAvailable}"
        bind.swDirection.text = InputQuestionDirection.HORIZONTAL.name

        //NEW QUESTION ID
        bind.tvIdInput.text = UUID.randomUUID().toString()

        //FIXME: ACTIONS LISTENER
        bind.swDirection.setOnClickListener() {

            if (bind.swDirection.isChecked) {
                bind.swDirection.text = InputQuestionDirection.VERTICAL.name
                bind.etAnswerInput.setText("")
                bind.etAnswerInput.hint = "available ${colCount} boxes"
                bind.etAnswerInput.filters = colFilter
                bind.tvSlotPreview.text = "${colAvailable}"
            } else {
                bind.swDirection.text = InputQuestionDirection.HORIZONTAL.name
                bind.etAnswerInput.setText("")
                bind.etAnswerInput.hint = "available ${rowCount} boxes"
                bind.etAnswerInput.filters = rowFilter
                bind.tvSlotPreview.text = "${rowAvailable}"
            }
        }

        bind.btnUpdateInput.setOnClickListener() {
            if (bind.etAskInput.text.isEmpty() || bind.etAnswerInput.text.isEmpty()) return@setOnClickListener

            val id: String = bind.tvIdInput.text.toString()
            val number: Int = bind.etNoInput.text.toString().toInt()
            val asking: String = bind.etAskInput.text.toString()
            val answer: String = bind.etAnswerInput.text.toString().trim()
            val direction: String = bind.swDirection.text.toString()

            addQuestion(id, number, asking, answer, direction, rowAvailable, colAvailable)
            addPartial(id, answer, direction, rowAvailable, colAvailable)


            getInputAnswerDirection()
            onClickBox()

            dialog.dismiss()
        }
        bind.btnCancelInput.setOnClickListener() {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun addQuestion(
        id: String,
        number: Int,
        asking: String,
        answer: String,
        direction: String,
        rowAvailable: List<Int>,
        colAvailable: List<Int>
    ) {
        val levelId = Const.currentLevel
        val data = Data.listQuestion
        val box = if (direction == InputQuestionDirection.HORIZONTAL.name) {
            rowAvailable.toMutableList()
        } else {
            colAvailable.toMutableList()
        }

        val slot = arrayListOf<Int>()
        for (i in 0 until answer.length) {
            slot.add(box[i])
        }

        if (Const.inputMode == InputMode.NEW.name) {
            data.add(
                Data.Question(
                    levelId = levelId,
                    id = id,
                    number = number,
                    direction = direction,
                    asking = asking,
                    answer = answer,
                    slot = slot
                )
            )
            Const.position = slot[0]
            currentIndex = Data.listQuestion.count() { it.levelId == levelId }
        }

        //TODO: HAPUS DULU YANG SEBELUMNYA
        if (Const.inputMode == InputMode.EDIT.name) {
            val sf =
                data.filter { it.levelId == levelId && it.id == id && it.direction == direction }
            sf.map { it }.forEach {
                it.number = number
                it.direction = direction
                it.asking = asking
                it.answer = answer
                it.slot = slot
            }
        }
    }

    private fun addPartial(
        questionId: String,
        answerText: String,
        direction: String,
        rowAvailable: List<Int>,
        colAvailable: List<Int>
    ) {
        val levelId = Const.currentLevel
        val part = Data.listPartial
        val boxAvailable = if (direction == InputQuestionDirection.HORIZONTAL.name) {
            rowAvailable
        } else {
            colAvailable
        }

        val slot = boxAvailable.subList(0, answerText.length)
        var prevSlot = mutableListOf<Int>()
        Data.listQuestion.filter { it.levelId == levelId }
            .map { it }.forEach() {
                prevSlot = it.slot
            }
        var sama = ""
        for (i in 0 until slot.size) {
            if (slot[i] in prevSlot) {
                sama = sama + slot[i] + ", "
            }
        }

        for (i in answerText.indices) {
            part.add(
                Data.Partial(
                    levelId = levelId,
                    id = UUID.randomUUID().toString(),
                    charAt = boxAvailable[i],
                    charStr = answerText[i].toString(),
                    rowQuestionId = if (direction == InputQuestionDirection.HORIZONTAL.name) questionId else "",
                    colQuestionId = if (direction == InputQuestionDirection.VERTICAL.name) questionId else "",
                )
            )
        }
    }
}