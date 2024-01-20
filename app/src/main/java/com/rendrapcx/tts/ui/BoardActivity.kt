package com.rendrapcx.tts.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
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
import androidx.lifecycle.lifecycleScope
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.snackbar.Snackbar
import com.rendrapcx.tts.R
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.constant.Const.BoardSet
import com.rendrapcx.tts.constant.Const.Companion.boardSet
import com.rendrapcx.tts.constant.Const.Companion.currentCategory
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
import com.rendrapcx.tts.databinding.DialogWinBinding
import com.rendrapcx.tts.helper.Dialog
import com.rendrapcx.tts.helper.Helper
import com.rendrapcx.tts.helper.Keypad
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.model.Data.Companion.listLevel
import com.rendrapcx.tts.model.Data.Companion.listPartial
import com.rendrapcx.tts.model.Data.Companion.listQuestion
import com.rendrapcx.tts.model.Data.Companion.listUserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class BoardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBoardBinding
    private var box = arrayListOf<TextView>()
    private var intKey = arrayListOf<TextView>()
    private var inputAnswerDirection = InputAnswerDirection.ROW
    private var tipTop = true
    private val xLen = 10
    private val yLen = 10
    private var countXY = (xLen * yLen)
    private var currentQuestId = ""
    private var currentRange = arrayListOf<Int>()
    private var pickByArrow = false
    private var tag = arrayListOf<Int>()
    private var clip = ""

    private var selectedQuestion = ""
    private var onType = false

    private var curPartId = ""
    private var curRowId = ""
    private var curColId = ""
    private var curCharAt = 0
    private var curCharStr = ""

    private var isNew = true
    private var finishedId = arrayListOf<String>() //ganti nanti

    @SuppressLint("ClickableViewAccessibility")
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
                currentLevel = UUID.randomUUID().toString().substring(0, 20)
                listLevel.add(
                    Data.Level(currentLevel, "2024", "Title", "Admin")
                )

                binding.apply {
                    includeEditor.mainContainer.visibility = View.VISIBLE
                    includeKeyboard.integratedKeyboard.visibility = View.GONE
                    includeQuestionSpan.tvSpanQuestion.text = ""
                    includeEditor.containerInfo.visibility = View.GONE
                    includeEditor.containerPartial.visibility = View.GONE
                    includeHeader.tvLabelTop.text =
                        listLevel.first() { it.id == currentLevel }.title
                }

                position = 0
                setBoxTagText()
                getInputAnswerDirection()
                onClickBox()

                fillTextDescription()
                setOnSelectedColor()
                Dialog().apply { inputDescription(binding) }
            }

            BoardSet.EDITOR_EDIT -> {
                binding.apply {
                    includeEditor.mainContainer.visibility = View.VISIBLE
                    includeKeyboard.integratedKeyboard.visibility = View.GONE
                    includeQuestionSpan.tvSpanQuestion.text = ""
                    includeEditor.containerInfo.visibility = View.GONE
                    includeEditor.containerPartial.visibility = View.GONE
                    includeHeader.tvLabelTop.text =
                        listLevel.first() { it.id == currentLevel }.title
                }

                position = listPartial.first { it.levelId == currentLevel }.charAt

                setBoxTagText()
                fillTextDescription()
                getInputAnswerDirection()
                onClickBox()
                Dialog().apply { inputDescription(binding) }
            }

            BoardSet.PLAY, BoardSet.PLAY_USER -> {
                binding.apply {
                    includeEditor.mainContainer.visibility = View.GONE
                    includeHeader.tvLabelTop.text =
                        listLevel.first() { it.id == currentLevel }.title
                }

                position = listPartial.first { it.levelId == currentLevel }.charAt
                setBoxTagText()
                getInputAnswerDirection()
                onClickBox()
            }

            BoardSet.PLAY_NEXT -> {

            }
        }

        for (i in 0 until box.size) {
            YoYo.with(Techniques.RotateInUpRight)
                .duration(2000)
                .repeat(0)
                .playOn(box[i]);
        }

        binding.includeKeyboard.apply {
            btnBackSpace.setOnClickListener() {
                box[position].text = ""
                onPressBackSpace()
                YoYo.with(Techniques.Landing)
                    .duration(500)
                    //.repeat(1)
                    .playOn(btnBackSpace);
            }
            btnShuffle.setOnClickListener() {
                showAnswerKeypad()
                YoYo.with(Techniques.Landing)
                    .duration(500)
                    //.repeat(1)
                    .playOn(btnShuffle);
            }
            //KEYBOARD PRESS
            for (i in 0 until intKey.size) {
                intKey[i].setOnTouchListener(View.OnTouchListener() { _, motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            box[position].text = intKey[i].text
                            onPressAbjabMove()
                            checkWinCondition(false)
                            YoYo.with(Techniques.Landing)
                                .duration(1000)
                                .repeat(0)
                                .playOn(box[position]);
                        }

                        MotionEvent.ACTION_UP -> {
                            YoYo.with(Techniques.Landing)
                                .duration(500)
                                .repeat(0)
                                .playOn(intKey[i]);
                        }
                    }
                    return@OnTouchListener true
                })
            }
        }

        /* TODO: HEADER ACTIONS*/
        binding.includeHeader.apply {
            btnBack.setOnClickListener() {
                val intent = if (boardSet == BoardSet.PLAY_USER)
                    Intent(this@BoardActivity, MainActivity::class.java)
                else Intent(this@BoardActivity, QuestionActivity::class.java)
                startActivity(intent)
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
            btnSettingPlay.setOnClickListener() {
                Dialog().apply { settingDialog(this@BoardActivity, lifecycle) }
            }
        }

        /*  BOX CLICK ACTION*/
        binding.includeBoard.boardTen.setOnClickListener() {
            for (i in 0 until box.size) {
                box[i].setOnClickListener() {
                    pickByArrow = false
                    position = i
                    getInputAnswerDirection()
                    onClickBox()

                    YoYo.with(Techniques.RubberBand)
                        .duration(1000)
                        .repeat(0)
                        .playOn(box[i]);

                    when (boardSet) {
                        BoardSet.EDITOR_NEW, BoardSet.EDITOR_EDIT -> {
                            if (clip.isNotEmpty()) pasteId()
                        }

                        BoardSet.PLAY, BoardSet.PLAY_USER -> {
                            if (listUserPreferences[0].integratedKeyboard) {
                                Keypad().showSoftKeyboard(window, it)
                            }
                        }

                        else -> {}
                    }
                }

                box[i].setOnLongClickListener() {
                    if (clip.isEmpty()) {
                        clip = curRowId.ifEmpty { curColId }
                        Dialog().showDialog(this@BoardActivity, "pilih box sibling untuk menempelkan")
                    }
                    return@setOnLongClickListener true
                }
            }
        }

        // FIXME: SELECT QUESTION BY ARROW
        binding.includeQuestionSpan.apply {
            btnNextQuestion.setOnClickListener() {
                if (isNew && boardSet == BoardSet.EDITOR_NEW) return@setOnClickListener
                resetBoxColor()
                fillText()
                getRequestQuestions(SelectRequest.NEXT)
                onClickBox()
                YoYo.with(Techniques.ZoomIn)
                    .duration(500)
                    //.repeat(1)
                    .playOn(btnNextQuestion);
                YoYo.with(Techniques.FadeInLeft)
                    .duration(500)
                    //.repeat(1)
                    .playOn(tvSpanQuestion);
            }
            btnPrevQuestion.setOnClickListener() {
                if (isNew && boardSet == BoardSet.EDITOR_NEW) return@setOnClickListener
                resetBoxColor()
                fillText()
                getRequestQuestions(SelectRequest.PREV)
                onClickBox()
                YoYo.with(Techniques.ZoomIn)
                    .duration(500)
                    //.repeat(1)
                    .playOn(btnPrevQuestion);
                YoYo.with(Techniques.FadeInRight)
                    .duration(500)
                    //.repeat(1)
                    .playOn(tvSpanQuestion);
            }
            tvSpanQuestion.setOnClickListener() {
                //checkWinCondition()
                //playNext()
            }
        }

        /* EDITOR BINDING ACTION                                                                 */
        binding.includeEditor.apply {
            if (boardSet == BoardSet.PLAY || boardSet == BoardSet.PLAY_USER) return
            // TODO:

            btnSave.setOnClickListener() {
                if (listQuestion.isEmpty()) {
                    Dialog().showDialog(this@BoardActivity, "Data masih kosong")
                } else {
                    isNew = false
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

            //EDIT DESCRIPTION
            btnEdit.setOnClickListener() {
                Dialog().apply { inputDescription(binding) }
            }
        }


    }

    private fun fillTextDescription() {
        if (boardSet == BoardSet.EDITOR_NEW || boardSet == BoardSet.EDITOR_EDIT) {
            listLevel.filter { it.id == Const.currentLevel }.forEach() {
                binding.includeEditor.apply {
                    textLevelId.text = it.id
                    textCategory.text = it.category
                    textTitle.text = it.title
                    textCreator.text = it.userId
                }
            }
        }
    }

    private fun pasteId() {
        if (boardSet == BoardSet.EDITOR_NEW || boardSet == BoardSet.EDITOR_EDIT) {
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
            Snackbar.make(binding.boardActivityRoot, "berhasil menempelkan", Snackbar.LENGTH_SHORT)
                .setAction("Undo", View.OnClickListener {
                    Toast.makeText(
                        this@BoardActivity,
                        "belum tersedia",
                        Toast.LENGTH_SHORT
                    ).show()
                })
                .show()
        }
    }

    private fun getInputAnswerDirection() {
        if (getColumnId() != "") inputAnswerDirection = InputAnswerDirection.COLUMN
        else if (getRowId() != "") inputAnswerDirection = InputAnswerDirection.ROW
    }

    private fun onClickBox() {
        fillText() //ONLY when edit

        setOnSelectedColor()
        setOnRangeColor()
        showPartInfo()
        binding.includeQuestionSpan.tvSpanQuestion.text = selectedQuestion
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun saveAndApply() {
        val levelId = currentLevel
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
        Dialog().showDialog(this@BoardActivity, "Data berhasil disimpan")
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
                if (box[x].isFocused) {
                    box[x].text = ""
                    onPressBackSpace()
                }
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
        if (boardSet == BoardSet.EDITOR_EDIT || boardSet == BoardSet.EDITOR_NEW) return
        var pass = true
        for (i in tag) {
//            if (box[i].text == box[i].tag) {
//                // TODO: if setting enable to cek colorize then do this
//                box[i].setBackgroundColor(getColor(this, R.color.pass))
//            }
            if (box[i].text != box[i].tag) {
                if (color) {
                    box[i].setBackgroundColor(getColor(this, R.color.not_pass))
                    YoYo.with(Techniques.Flash)
                        .duration(1000)
                        .repeat(0)
                        .playOn(box[i]);
                }
                pass = false
            }
        }
        if (pass) {
            // TODO: 1. win dialog, 2. next question or back to list, 3. update score 4. update user data level finished
            val mp = MediaPlayer.create(applicationContext, R.raw.crowd_applause)
            mp.start()
            winDialog(this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun winDialog(
        context: Context,
    ) {
        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val bind = DialogWinBinding.inflate(inflater)
        val builder = AlertDialog.Builder(context).setView(bind.root)
        val dialog = builder.create()

        extracted(dialog)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        bind.btnNext.setOnClickListener() {
            playNext()
            dialog.dismiss()
        }

        bind.btnBack.setOnClickListener() {
            val i = Intent(context.applicationContext, MainActivity::class.java)
            startActivity(i)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            dialog.dismiss()
        }

        dialog.show()
    }

    /* asdsd ad                                                                              */
    private fun playNext() {
        lifecycleScope.launch {
            //Get Data Level by Category on Play
            val dataLevel = DB.getInstance(applicationContext).level().getAllByCategory(
                currentCategory
            )
            val count = dataLevel.count()

            finishedId.add(currentLevel)

            if (count == finishedId.count()) {
                val i = Intent(this@BoardActivity, MainActivity::class.java)
                startActivity(i)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                return@launch
            }

            var newId = ""
            for (i in dataLevel) {
                if (!finishedId.contains(i.id) ) {
                    newId = i.id
                    break
                }
            }

            currentLevel = newId

            //GET DATA from New currentLevel
            boardSet = BoardSet.PLAY_USER

            listLevel.clear()
            listLevel =
                DB.getInstance(applicationContext).level().getLevel(currentLevel)

            listQuestion.clear()
            listQuestion =
                DB.getInstance(applicationContext).question()
                    .getQuestion(currentLevel)
            listPartial.clear()
            listPartial = DB.getInstance(applicationContext).partial().getPartial(
                currentLevel
            )

            binding.apply {
                includeEditor.mainContainer.visibility = View.GONE
                includeHeader.tvLabelTop.text =
                    listLevel.first() { it.id == currentLevel }.title
            }

            position = listPartial.first { it.levelId == currentLevel }.charAt

            for (i in 0 until box.size) {
                box[i].text = ""
                box[i].tag = ""
                box[i].visibility = View.VISIBLE
            }

            binding.apply {

            }

            setBoxTagText()
            getInputAnswerDirection()
            onClickBox()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun extracted(dialog: AlertDialog) {
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
    }

    /* GET ID QUESTION DARI PARTIAL Row ID */
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


    /* GET ID QUESTION DARI PARTIAL Column ID */
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


    private fun getAnswer(): String {
        var id = getRowId()
        if (inputAnswerDirection == InputAnswerDirection.ROW) id = getRowId()
        else if (inputAnswerDirection == InputAnswerDirection.COLUMN) id = getColumnId()

        var result = ""
        listQuestion.filter { it.levelId == currentLevel }
            .filter { it.id == id }
            .map { it }.forEach() {
                result = it.answer
            }
        return result
    }

    private fun getQuestion(): String {
        var id = if (listQuestion.isEmpty()) ""
        else getRowId()

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
            val abjad = Helper().abjadKapital()
            val jawab = getAnswer()
            val charArr = jawab.toSortedSet()
            val size = 14
            val kurang = size - charArr.size

            if (kurang > 0) {
                while (charArr.size < 14) {
                    val s = abjad.random()
                    if (!jawab.contains(s)) {
                        charArr.add(s[0])
                    }
                }
            }

            val keyJawab = charArr.toCharArray()
            keyJawab.shuffle()
            for (i in 0 until intKey.size) {
                intKey[i].text = keyJawab[i].toString()
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
                            //tagMap.put(it.charAt, it.charStr)
                        }
                    }
                }
                resetBoxColor()
            }

            BoardSet.PLAY, BoardSet.PLAY_USER -> {
                tag.clear()
                listPartial.filter { it.levelId == currentLevel }.forEach() {
                    for (i in 0 until box.size) {
                        if (i == it.charAt) {
                            box[i].text = ""
                            box[i].tag = it.charStr
                            tag.add(it.charAt)
                            //tagMap.put(it.charAt, it.charStr)
                        }
                    }
                }
                resetBoxColor()
            }

            BoardSet.PLAY_NEXT -> {
                // TODO: ISI PLAY NEXT 
            }
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
        if (boardSet == BoardSet.EDITOR_NEW || boardSet == BoardSet.EDITOR_EDIT) {
            listPartial.filter { it.levelId == currentLevel }.map { it }.forEach() {
                for (i in 0 until box.size)
                    if (i == it.charAt) {
                        box[i].text = it.charStr
                        box[i].tag = it.charStr
                        tag.add(it.charAt)
                    }
            }
        }
    }

    private fun resetBoxColor() {
        for (i in 0 until box.size) {
            if (box[i].tag == "") {
                if (boardSet == BoardSet.PLAY_USER || boardSet == BoardSet.PLAY) {
                    box[i].visibility = View.INVISIBLE
                }
            }
            box[i].setTextColor(getColor(this, R.color.button))
            box[i].setBackgroundColor(getColor(this, R.color.white))
        }
    }

    /************************************************************************
     * INITIAL LAYOUT AND COMPONENTS
     * ***********************************************************************/
    private fun initLayoutPlay() {
        binding.includeEditor.mainContainer.visibility = View.GONE

        binding.includeQuestionSpan.tvSpanQuestion.text = ""
        binding.includeHeader.tvLabelTop.text = listLevel.first() { it.id == currentLevel }.title
    }

    private fun initLayoutEditor() {
        binding.includeEditor.mainContainer.visibility = View.VISIBLE
        binding.includeKeyboard.integratedKeyboard.visibility = View.GONE

        binding.includeHeader.tvLabelTop.text = "TERKA EDITOR"
        binding.includeQuestionSpan.tvSpanQuestion.text = ""

        binding.includeEditor.containerInfo.visibility = View.GONE
        binding.includeEditor.containerPartial.visibility = View.GONE
        binding.includeHeader.tvLabelTop.text = listLevel.first() { it.id == currentLevel }.title
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
            if (bind.etAskInput.text.isEmpty()) {
                bind.etAskInput.error = "tidak boleh kosong"
                return@setOnClickListener
            }
            if (bind.etAnswerInput.text.isEmpty()) {
                bind.etAnswerInput.error = "tidak boleh kosong"
                return@setOnClickListener
            }
            if (!bind.etAnswerInput.text.all { it.isLetter() }) {
                bind.etAnswerInput.error = "hanya huruf kapital, jangan ada spasi, angka atau simbol"
                return@setOnClickListener
            }

            val id: String = bind.tvIdInput.text.toString()
            val number: Int = bind.etNoInput.text.toString().toInt()
            val asking: String = bind.etAskInput.text.toString().trim()
            val answer: String = bind.etAnswerInput.text.toString().uppercase()
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