package com.rendrapcx.tts.ui

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.rendrapcx.tts.constant.ColorAttr
import com.rendrapcx.tts.constant.Constants
import com.rendrapcx.tts.constant.Direction
import com.rendrapcx.tts.constant.InputDirection
import com.rendrapcx.tts.constant.InputMode
import com.rendrapcx.tts.constant.SelectRequest
import com.rendrapcx.tts.constant.TextAttr
import com.rendrapcx.tts.databinding.ActivityCreatorBinding
import com.rendrapcx.tts.databinding.DialogInputSoalBinding
import com.rendrapcx.tts.helper.Keypad
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.viewmodel.BoardViewModel
import kotlinx.coroutines.launch
import java.util.UUID


class CreatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatorBinding
    private var vm = BoardViewModel()

    @Suppress("DEPRECATION")
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vm = ViewModelProvider(this)[BoardViewModel::class.java]

        boxViewInit()
        vm.newLevelId()
        initComponents()
        vm.boxColor(this, binding, ColorAttr.COLOR_BACKGROUND)
        vm.boxColor(this, binding, ColorAttr.COLOR_DISABLE)

        vm.boxText(TextAttr.CLEAR_TEXT)

        vm.position.observe(this) {
            binding.included2.tvLevelId.text = vm.levelId.toString()
            vm.boxColor(this, binding, ColorAttr.COLOR_DISABLE)
            vm.boxColor(this, binding, ColorAttr.COLOR_ACTIVE)
            vm.boxColor(this, binding, ColorAttr.COLOR_SELECTED)

            binding.included2.tvInfo.text =
                "${vm.sTemp} \n" +
                "${vm.getRowId()} | ${vm.getColumnId()} | ${vm.getRowRange()} | ${vm.getColumnRange()}" +
                    "\n POSITION = ${vm.getCurrent()} | PREV = ${vm.prevPos} \n" +
                    "inputDirection: ${vm.inputDirection} | currentRange${vm.currentRange} \n" +
                        "nextRow: ${vm.selectNextRow()} | NextColumn: ${vm.selectNextColumn()}"
        }



        /* --ONCLICK BOX-VIEW */
        binding.included1.apply {
            for (i in 0 until vm.box.size) {
                vm.box[i].setOnClickListener {
                    vm.prevPos = vm.getCurrent()
                    vm.setCurrent(i)
                    if (binding.included2.swSoftKey.isChecked) Keypad().showSoftKeyboard(window, it)
                    loadPart()
                    if (vm.getColumnId() != "") vm.inputDirection = InputDirection.COLUMN
                    else if (vm.getRowId() != "") vm.inputDirection = InputDirection.ROW
                    vm.boxColor(this@CreatorActivity, binding, ColorAttr.COLOR_RANGE_SELECT)
                    vm.boxColor(this@CreatorActivity, binding, ColorAttr.COLOR_SELECTED)
                    binding.included3.tvSpanQuestion.text = vm.selectedQuestion
                }

                vm.box[i].setOnLongClickListener {
                    // TODO:
                    return@setOnLongClickListener true
                }
            }
        }

        //-- INCLUDE2 CONTAINER
        binding.included2.apply {


            /*init or update spinner adapter view*/
            initOrUpdateSpItems()

            /*INCLUDE_2 EVENT LISTENER*/
            spItems.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    Data.listQuestion.filter { it.levelId == vm.levelId }
                        .filter { it.number.toString() + "." + it.direction + ":" + it.id == spItems.selectedItem }
                        .map { it }.forEach { it ->
                            etQuestId.text = it.id
                            etNo.text = it.number.toString()
                            etDirection.text = it.direction
                            etAsk.text = it.asking
                            etAnswer.text = it.answer
                            tvMember.text = "${it.slot}"
                        }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            btnResetInput.setOnClickListener {
                vm.setCurrent(0)
                Data.listQuestion.removeIf { it.levelId == vm.levelId }
                Data.listPartial.removeIf { it.levelId == vm.levelId }
                initOrUpdateSpItems()
                clearQuestionFields()
                vm.newLevelId()
                vm.boxText(TextAttr.CLEAR_TEXT)
                vm.boxColor(this@CreatorActivity, binding, ColorAttr.COLOR_DISABLE)
            }

            btnSwitchQuestion.setOnClickListener {
                if (btnSwitchQuestion.text == Constants.strRight) {
                    btnSwitchQuestion.text = Constants.strDown
                    vm.direction = Direction.VERTICAL.name
                } else {
                    btnSwitchQuestion.text = Constants.strRight
                    vm.direction = Direction.HORIZONTAL.name
                }
            }

            btnNewQuest.setOnClickListener {
                if (vm.getCurrent() < 0) return@setOnClickListener
                inputQuestioner(this@CreatorActivity)
            }

            tvClip.setOnClickListener() {
                if (vm.getClip().isNotEmpty()) {
                    if (tvRightAt.text.isEmpty()) {
                        tvRightAt.text = vm.getClip()
                        vm.setClip("")
                        tvClip.visibility = View.INVISIBLE
                        btnClearClip.visibility = View.INVISIBLE
                        Data.listPartial.filter { it.levelId == vm.levelId }
                            .filter { it.id == tvPartId.text }
                            .map {
                                it.rowQuestionId = tvRightAt.text.toString()
                            }
                        initOrUpdateSpItems()
                        loadPart()
                    }
                    if (tvDownAt.text.isEmpty()) {
                        tvDownAt.text = vm.getClip()
                        vm.setClip("")
                        tvClip.visibility = View.INVISIBLE
                        btnClearClip.visibility = View.INVISIBLE
                        Data.listPartial.filter { it.levelId == vm.levelId }
                            .filter { it.id == tvPartId.text }
                            .map {
                                it.colQuestionId = tvDownAt.text.toString()
                            }
                        initOrUpdateSpItems()
                        loadPart()
                    }
                }
            }

            swInfoVisibility.setOnClickListener {
                if (swInfoVisibility.isChecked) {
                    tvInfo.visibility = View.VISIBLE
                    panelInfo.visibility = View.VISIBLE
                } else {
                    tvInfo.visibility = View.GONE
                    panelInfo.visibility = View.GONE
                }
            }

            swQuestionVisibility.setOnClickListener() {
                if (swQuestionVisibility.isChecked) {
                    textView12.visibility = View.VISIBLE
                    panelQuestions.visibility = View.VISIBLE
                    textView8.visibility = View.VISIBLE
                    tvLevelId.visibility = View.VISIBLE
                    etQuestId.visibility = View.VISIBLE
                    etNo.visibility = View.VISIBLE
                    etAsk.visibility = View.VISIBLE
                    etAnswer.visibility = View.VISIBLE
                    etDirection.visibility = View.VISIBLE
                    tvMember.visibility = View.VISIBLE
                    tvLevelId.visibility = View.VISIBLE
                } else {
                    textView12.visibility = View.GONE
                    panelQuestions.visibility = View.GONE
                    textView8.visibility = View.GONE
                    tvLevelId.visibility = View.GONE
                    etQuestId.visibility = View.GONE
                    etNo.visibility = View.GONE
                    etAsk.visibility = View.GONE
                    etAnswer.visibility = View.GONE
                    etDirection.visibility = View.GONE
                    tvMember.visibility = View.GONE
                    tvLevelId.visibility = View.GONE
                }
            }

            tvRightAt.setOnClickListener() {
                if (vm.getClip().isEmpty()) {
                    vm.setClip(tvRightAt.text.toString())
                    tvClip.text = tvRightAt.text //vm.clip
                    tvClip.visibility = View.VISIBLE
                    btnClearClip.visibility = View.VISIBLE
                    Toast.makeText(this@CreatorActivity, "Copied", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            tvDownAt.setOnClickListener() {
                if (vm.getClip().isEmpty()) {
                    vm.setClip(tvDownAt.text.toString())
                    tvClip.text = tvDownAt.text
                    tvClip.visibility = View.VISIBLE
                    btnClearClip.visibility = View.VISIBLE
                    Toast.makeText(this@CreatorActivity, "Copied", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            btnClearClip.setOnClickListener() {
                vm.setClip("")
                btnClearClip.visibility = View.INVISIBLE
                tvClip.visibility = View.INVISIBLE
            }

            btnGetLevel.setOnClickListener(){
                Toast.makeText(this@CreatorActivity, "${vm.levelId}", Toast.LENGTH_SHORT).show()
                binding.included2.tvLevelId.text = "asdasdasdasd"
                btnGetLevel.text = vm.levelId
//                Data.listLevel.add(
//                    Data.Level(id = vm.levelId, category = "Testing", dimension = "10x10")
//                )
//                textView.text=""
//                val cm = applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
//                cm.text = Data.listLevel.toString()
            }
            btnGetQuestion.setOnClickListener(){
                textView.text=""
                textView.text = Data.listQuestion.filter { it.levelId == vm.levelId }.toString()
                val cm = applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                cm.text = Data.listQuestion.filter { it.levelId == vm.levelId }.toString()
            }
            btnGetPartial.setOnClickListener(){
                textView.text=""
                textView.text = Data.listPartial.filter { it.levelId == vm.levelId }.toString()
                val cm = applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                cm.text = Data.listPartial.filter { it.levelId == vm.levelId }.toString()
            }
            btnLoadSoal.setOnClickListener(){
                //boxViewInit()


                vm.boxText(TextAttr.CLEAR_TEXT)

//                vm.gameState = GameState.PLAY

                vm.levelId = "1"
                tvLevelId.text = vm.levelId

                initOrUpdateSpItems()
                loadPart()
                clearQuestionFields()
//                vm.boxText(TextAttr.FILL_TAG)
//                vm.boxVisibility()
                vm.boxText(TextAttr.FILL_TEXT)
//                initComponents()
                vm.setCurrent(0)
                vm.currentIndex.value = 1
                vm.boxColor(this@CreatorActivity, binding, ColorAttr.COLOR_DISABLE)
                vm.boxColor(this@CreatorActivity, binding, ColorAttr.COLOR_ACTIVE)

            }


        } //EndBinding Include2

        binding.included3.apply {
            btnNextQuestion.setOnClickListener() {
                vm.getRequestQuestions(SelectRequest.NEXT)
                vm.boxColor(this@CreatorActivity, binding, ColorAttr.COLOR_RANGE_SELECT)
                vm.boxColor(this@CreatorActivity, binding, ColorAttr.COLOR_SELECTED)
                binding.included3.tvSpanQuestion.text = vm.selectedQuestion
            }
            btnPrevQuestion.setOnClickListener() {
                vm.getRequestQuestions(SelectRequest.PREV)
                vm.boxColor(this@CreatorActivity, binding, ColorAttr.COLOR_RANGE_SELECT)
                vm.boxColor(this@CreatorActivity, binding, ColorAttr.COLOR_SELECTED)
                binding.included3.tvSpanQuestion.text = vm.selectedQuestion
            }
        } //end binding included3

    } //create

    private fun loadPart() {
        binding.included2.apply {
            tvPartId.text = ""
            tvCharAt.text = ""
            tvChar.text = ""
            tvRightAt.text = ""
            tvDownAt.text = ""

            Data.listPartial.filter { it.levelId == vm.levelId }
                .filter { it.charAt == vm.getCurrent() }
                .map { it }.forEach {
                    tvPartId.text = it.id
                    tvCharAt.text = it.charAt.toString()
                    tvChar.text = it.char
                    tvRightAt.text = it.rowQuestionId
                    tvDownAt.text = it.colQuestionId
                }
        }
    }

    private fun clearQuestionFields() {
        binding.included2.apply {
            etQuestId.text = ""
            etNo.text = ""
            etDirection.text = ""
            etAsk.text = ""
            etAnswer.text = ""
            tvMember.text = ""
        }
    }

    private fun initOrUpdateSpItems() {
        val list = ArrayAdapter<String>(
            this,
            androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item
        )
        list.clear()
        Data.listQuestion
            .filter { it.levelId == vm.levelId }
            .map { it }
            .forEach {
                list.add(it.number.toString() + "." + it.direction + ":" + it.id)
            }
        binding.included2.spItems.adapter = list
    }

    /* SHOW INPUT POPUP*/
    private fun inputQuestioner(context: Context) {
        lifecycleScope.launch {

            val bind = DialogInputSoalBinding.inflate(layoutInflater)
            val builder = AlertDialog.Builder(context).setView(bind.root)
            val dialog = builder.create()

            val bdId = bind.tvIdInput
            val bdNo = bind.etNoInput
            val bdDirection = bind.etDirectionInput
            val bdAsk = bind.etAskInput
            val bdAnswer = bind.etAnswerInput
            val bdMembers = bind.tvSlotPreview

            val bdBtnUpdate = bind.btnUpdateInput
            val bdBtnCancel = bind.btnCancelInput

            /*INITIAL INPUT MODE*/
            val partAt = vm.getFlipQuestionId()
            var cDir = ""

            Data.listQuestion.filter { it.levelId == vm.levelId }
                .filter { it.id == partAt }
                .map { it }.forEach {
                    cDir = it.direction
                }

            vm.inputMode = if (cDir == vm.direction) InputMode.EDIT
            else InputMode.NEW

            /*-------INITIAL COMPONENTS-------------------------*/
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(false)

            if (vm.inputMode == InputMode.NEW) {
                bdId.text = UUID.randomUUID().toString().substring(0, 10)
                bdNo.setText(vm.getCurrent().toString())
                bdDirection.setText(vm.direction)
                bdAnswer.setText("")
                bdId.isEnabled = false
                bdDirection.isEnabled = false
                bdNo.isEnabled = false
            }

            if (vm.inputMode == InputMode.EDIT) {
                Data.listQuestion.filter { it.levelId == vm.levelId }
                    .filter { it.id == partAt }
                    .map { it }.forEach {
                        bdId.text = it.id
                        bdNo.setText(vm.getCurrent().toString())
                        bdDirection.setText(it.direction)
                        bdAsk.setText(it.asking)
                        bdAnswer.setText(it.answer)
                    }
                bdId.isEnabled = false
                bdDirection.isEnabled = false
                bdNo.isEnabled = false
            }

            if (vm.direction == Direction.HORIZONTAL.name) {
                bdMembers.text = vm.boxRowOvers().toString()
                bdAnswer.hint = vm.boxRowOvers().count().toString() + " Slot"
                bdAnswer.filters += InputFilter.LengthFilter(vm.boxRowOvers().count())
            } else {
                bdMembers.text = vm.boxColumnOvers().toString()
                bdAnswer.hint = vm.boxColumnOvers().count().toString() + " Slot"
                bdAnswer.filters += InputFilter.LengthFilter(
                    vm.boxColumnOvers().count()
                )
            }

            bdAsk.requestFocus()

            /*--EVENT LISTENER------------------------------------------*/
            bdAnswer.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    if (vm.direction == Direction.HORIZONTAL.name) {
                        bdAnswer.filters += InputFilter.LengthFilter(
                            vm.boxRowOvers().count()
                        )
                    } else {
                        bdAnswer.filters += InputFilter.LengthFilter(
                            vm.boxColumnOvers().count()
                        )
                    }
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            bdBtnUpdate.setOnClickListener {
                val answerText = bdAnswer.text.toString().trim()
                val direction = bdDirection.text.toString()
                val questionId = bdId.text.toString()
                val id = bdId.text.toString()
                val number = bdNo.text.toString().toInt()
                val asking = bdAsk.text.toString().trim()
                val answer = bdAnswer.text.toString().trim()

                //VALIDASI
                if (asking.isEmpty() && answer.isEmpty()) return@setOnClickListener

//                if (vm.inputMode == InputMode.EDIT) removePrevPart()

                addPartList(questionId = questionId, answerText = answerText)

                if (vm.inputMode == InputMode.NEW) {
                    saveQuestioner(
                        levelId = vm.levelId,
                        id = id,
                        number = number,
                        asking = asking,
                        answer = answer,
                        direction = direction
                    )
                }
                if (vm.inputMode == InputMode.EDIT) {
                    saveQuestioner(
                        levelId = vm.levelId,
                        id = id,
                        number = number,
                        asking = asking,
                        answer = answer,
                        direction = direction
                    )
                }
                vm.boxText(TextAttr.FILL_TEXT)
                vm.boxColor(this@CreatorActivity, binding, ColorAttr.COLOR_ACTIVE)
                vm.boxColor(this@CreatorActivity, binding, ColorAttr.COLOR_SELECTED)

                dialog.dismiss()
            }

            bdBtnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun removePrevPart() {
        val questionId = vm.getFlipQuestionId()
        Data.listPartial.removeIf {
            it.levelId == vm.levelId &&
                    it.rowQuestionId == questionId || it.colQuestionId == questionId
        }
    }

    private fun addPartList(
        questionId: String,
        answerText: String,
    ) {
        val part = Data.listPartial
        val boxAvailable = if (vm.direction == Direction.HORIZONTAL.name) {
            vm.boxRowOvers()
        } else {
            vm.boxColumnOvers()
        }

        val slot = boxAvailable.subList(0, answerText.length)
        var prevSlot = mutableListOf<Int>()
        Data.listQuestion.filter { it.levelId == vm.levelId }
            .map { it }.forEach() {
                prevSlot = it.slot
            }
        var sama = ""
        for (i in 0 until slot.size) {
            if (slot[i] in prevSlot) {
                sama = sama + slot[i] + ", "
            }
        }
        //Toast.makeText(this, "${sama}", Toast.LENGTH_SHORT).show()


        for (i in answerText.indices) {
            part.add(
                Data.Partial(
                    levelId = vm.levelId,
                    id = UUID.randomUUID().toString().substring(0, 10),
                    charAt = boxAvailable[i],
                    char = answerText[i].toString(),
                    rowQuestionId = if (vm.direction == Direction.HORIZONTAL.name) questionId else "",
                    colQuestionId = if (vm.direction == Direction.VERTICAL.name) questionId else "",
                )
            )
        }
    }

    private fun saveQuestioner(
        levelId: String,
        id: String,
        number: Int,
        asking: String,
        answer: String,
        direction: String,
    ) {
        val data = Data.listQuestion
        val box = if (vm.direction == Direction.HORIZONTAL.name) {
            vm.boxRowOvers().toMutableList()
        } else {
            vm.boxColumnOvers().toMutableList()
        }

        val slot = arrayListOf<Int>()
        for (i in 0 until answer.length) {
            slot.add(box[i])
        }

        //Toast.makeText(this, "${vm.itSLot}", Toast.LENGTH_SHORT).show()
        if (vm.inputMode == InputMode.NEW) {
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
            vm.setCurrent(slot[0])
            vm.currentIndex.value = Data.listQuestion.count(){it.levelId == levelId}
        }
        if (vm.inputMode == InputMode.EDIT) {
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

        initOrUpdateSpItems()
        loadPart()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode in 29..54) {
            val s = event?.displayLabel
            vm.box[vm.getCurrent()].text = s.toString()
            if (vm.inputDirection == InputDirection.ROW) {
                if (vm.isRowOvers()) {
                    if (vm.selectNextRow() in vm.currentRange)
                        vm.setCurrent(vm.selectNextRow())
                }
            }
            if (vm.inputDirection == InputDirection.COLUMN) {
                if (vm.isColumnOvers()) {
                    if (vm.selectNextColumn() in vm.currentRange)
                        vm.setCurrent(vm.selectNextColumn())
                }
            }
        } else {
            return false
        }

        return super.onKeyDown(keyCode, event)
    }


    private fun initComponents() {
        binding.included2.apply {
            tvInfo.visibility = View.GONE
            panelInfo.visibility = View.GONE
            btnSwitchQuestion.text = Constants.strRight
            tvClip.text = ""
            tvClip.visibility = View.INVISIBLE
            btnClearClip.visibility = View.INVISIBLE
            textView12.visibility = View.GONE
            panelQuestions.visibility = View.GONE
            textView8.visibility = View.GONE
            tvLevelId.visibility = View.GONE
            etQuestId.visibility = View.GONE
            etNo.visibility = View.GONE
            etAsk.visibility = View.GONE
            etAnswer.visibility = View.GONE
            etDirection.visibility = View.GONE
            tvMember.visibility = View.GONE
            tvLevelId.visibility = View.GONE

            btnMoveDown.visibility = View.INVISIBLE
            btnMoveRight.visibility = View.INVISIBLE
        }
        binding.included3.apply {
            tvSpanQuestion.text = "TEST"
        }
    }

    private fun boxViewInit() {
        for (i in 0 until 100) {
            val child = binding.included1.boardTen.getChildAt(i)
            if (child is TextView) vm.box.add(child)
        }
    }


} //end

