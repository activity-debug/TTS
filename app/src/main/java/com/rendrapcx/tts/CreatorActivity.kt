package com.rendrapcx.tts

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.CalendarContract.Colors
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.rendrapcx.tts.databinding.ActivityCreatorBinding
import com.rendrapcx.tts.databinding.DialogInputSoalBinding
import com.rendrapcx.tts.databinding.DialogListItemBinding
import java.util.UUID

class CreatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatorBinding
    private var vm = MainViewModel()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vm = ViewModelProvider(this)[MainViewModel::class.java]

        initBoxView()

        vm.position.observe(this) {
            vm.apply {
                setBoxView(BoxSet.COLOR_RANGE_UNSELECT)
                setBoxView(BoxSet.COLOR_UNSELECT)
                setBoxView(BoxSet.COLOR_SELECTED)
            }
            binding.included2.tvInfo.text = "POSITION = ${vm.getCurrent()} \n" +
                    "next = ${vm.isHasNext()} at ${vm.moveNext()} | down = ${vm.isHasDown()} at ${vm.moveDown()} \n" +
                    "AvailableNext = ${vm.nextBoxExists()} \n" +
                    "availableDown = ${vm.downBoxExists()}"
        }

        vm.setNewLevelId()

        //--ONCLICK BOX
        binding.included1.apply {
            for (i in 0 until vm.boxView.size) {
                vm.boxView[i].setOnClickListener() {
                    vm.setCurrent(i)
                    if (binding.included2.swSoftKey.isChecked) Helper().showSoftKeyboard(window, it)
                    loadPart()
                    selectHorizontalRange()
                }

                vm.boxView[i].setOnLongClickListener() {
                    vm.boxView[i].text = ""
                    return@setOnLongClickListener true
                }
            }
        }

        //-- INCLUDE2 CONTAINER
        binding.included2.apply {

            tvLevelId.text = vm.levelId

            initSpItems() //init spinner item list
            spItems.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
//                    var x = ""
                    Data.questionList.filter { it.levelId == vm.levelId }
                        .filter { it.no + "." + it.direction == spItems.selectedItem }
                        .map { it }.forEach() {
                            etQuestId.text = it.id
                            etNo.text = it.no
                            etDirection.text = it.direction
                            etAsk.text = it.ask
                            etAnswer.text = it.answer
                            var s = ""
                            it.members?.map { it }!!.forEach() {
                                s += "[${it.charAt}:${it.char}],"
                            }
                            tvMember.text = s
//                            x = it.id.toString()
                        }
//                    tvMember.text = ""
//                    Data.partList.filter { it.levelId == vm.levelId }
//                        .filter { it.rowQuestionId == x || it.colQuestionId == x }
//                        .map { it }.forEach() {
//                            tvMember.text = tvMember.text.toString() + it.char + ", "
//                        }

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            btnResetInput.setOnClickListener() {
                vm.apply { setBoxView(BoxSet.CLEAR_TEXT) }
                Data.questionList.removeIf() { it.levelId == vm.levelId }
                Data.partList.removeIf() { it.levelId == vm.levelId }
                initSpItems()
                clearQuestionFields()
            }

            btnSwitchQuestion.setOnClickListener() {
                initListItem(this@CreatorActivity)
            }

            btnNewQuest.setOnClickListener() {
                if (vm.getCurrent() < 0) return@setOnClickListener
                inputQuestioner(this@CreatorActivity)
            }

            tvRightAt.setOnClickListener() {
                initListItem(this@CreatorActivity)
            }

            tvDownAt.setOnClickListener() {
                initListItem(this@CreatorActivity)
            }

            swInfoVisibility.setOnClickListener() {
                if (!swInfoVisibility.isChecked) {
                    tvInfo.visibility = View.GONE
                    panelInfo.visibility = View.GONE
                } else {
                    tvInfo.visibility = View.VISIBLE
                    panelInfo.visibility = View.VISIBLE
                }
            }
        }

    } //create

    private fun selectHorizontalRange() {
        binding.included2.apply {
            val pos = vm.getCurrent()
            val count = Data.partList.count() { it.levelId == vm.levelId }
            if (count > 0) {
                try {
                    val rowId = Data.partList.filter { it.levelId == vm.levelId }
                        .filter { it.charAt == pos.toString() }
                        .ifEmpty { return@apply }  //----------> error disini, krn gak ketemu chartAt nya, krn masih kosong
                        .map { it.rowQuestionId }.first().toString()
//                Helper().apply { showToast("${rowId}") }

                    val readRight = arrayListOf<String>()
                    Data.partList.filter { it.levelId == vm.levelId }
                        .filter { it.rowQuestionId == rowId }.ifEmpty { return@apply }
                        .map { it }.forEach() { readRight.add(it.charAt.toString()) }
//            Helper().apply { showToast("${readRight}") }

                    val range = arrayListOf<Int>()
                    Data.partList.filter { it.levelId == vm.levelId }
                        .filter { it.rowQuestionId == rowId }
                        .map { it }.forEach() {
                            range.add(it.charAt.toString().toInt())
                        }
                    if (pos in range) {
                        for (i in range.indices) {
                            val x = range[i]
                            vm.boxView[x].setBackgroundColor(Color.LTGRAY)
                        }
                        vm.apply { setBoxView(BoxSet.COLOR_SELECTED) }
                    }
                } catch (err: Exception) {
                    return@apply
                }

            }


        }
    }

    private fun loadPart() {
        binding.included2.apply {
            tvCharAt.text = ""
            tvChar.text = ""
            swRight.isChecked = false
            tvRightAt.text = ""
            swDOwn.isChecked = false
            tvDownAt.text = ""


            Data.partList.filter { it.levelId == vm.levelId }
                .filter { it.charAt == vm.getCurrent().toString() }
                .map { it }.forEach() {
                    tvCharAt.text = it.charAt
                    tvChar.text = it.char
                    swRight.isChecked = it.hasRow.toString().toBoolean()
                    tvRightAt.text = it.rowQuestionId
                    swDOwn.isChecked = it.hasCol.toString().toBoolean()
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

    private fun initSpItems() {
        val list = ArrayAdapter<String>(this, R.layout.simple_spinner_dropdown_item)
        list.clear()
        Data.questionList
            .filter { it.levelId == vm.levelId }
            .map { it }
            .forEach() { list.add(it.no + "." + it.direction) }
        binding.included2.spItems.adapter = list
    }

    //--Dialog ini mah
    private fun initListItem(context: Context) {
        val bind = DialogListItemBinding.inflate(layoutInflater)

        val adapter = AdapterListItem(::listItemClicked)
        val builder = AlertDialog.Builder(context).setView(bind.root)
        val dialog = builder.create()

        var list = mutableListOf<Data.Questions>()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        bind.rcView1.layoutManager = LinearLayoutManager(this)

        list.clear()

        list = Data.questionList.filter { it.levelId == vm.levelId }.toMutableList()
        if (list.size != 0) {
            adapter.setItems(list)
            bind.rcView1.adapter = adapter
        }

        dialog.show()

    }

    private fun listItemClicked(questions: Data.Questions) {
        binding.included2.apply {
            etQuestId.text = questions.id
            etNo.text = questions.no
            etDirection.text = questions.direction
            etAsk.text = questions.ask
            etAnswer.text = questions.answer
            tvMember.text = questions.members.toString()
        }
    }

    /* SHOW INPUT POPUP*/
    @SuppressLint("SetTextI18n", "UseSwitchCompatOrMaterialCode", "SuspiciousIndentation")
    private fun inputQuestioner(context: Context) {
        val bind = DialogInputSoalBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(context).setView(bind.root)
        val dialog = builder.create()


        val bgQuestId = binding.included2.etQuestId
        val bgNo = binding.included2.etNo
        val bgAsk = binding.included2.etAsk
        val bgAnswer = binding.included2.etAnswer
        val bgDirection = binding.included2.etDirection

        val bdId = bind.tvIdInput
        val bdNo = bind.etNoInput
        val bdDirection = bind.etDirectionInput
        val bdAsk = bind.etAskInput
        val bdAnswer = bind.etAnswerInput
        val bdMembers = bind.tvMemberInput

        val bdSwDirection = bind.swDirectionInput
        val bdSwAutoFill = bind.swAutoFill
        val bdBtnUpdate = bind.btnUpdateInput
        val bdBtnCancel = bind.btnCancelInput

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)


        val inputMode : InputMode
        var posAt = arrayListOf<String>()
        Data.questionList.filter lif@ { it.levelId == vm.levelId }
            .filter { it.id == bgQuestId.text.toString() }
            .map { it }.forEach(){
                it.members?.map { it }?.forEach(){
                    posAt.add(it.charAt!!)
                }
            }

        inputMode = if (posAt.contains(vm.getCurrent().toString())) InputMode.EDIT
        else InputMode.NEW

        Toast.makeText(this, "${inputMode.name} -> ${posAt}", Toast.LENGTH_SHORT).show()

        if (inputMode == InputMode.NEW) {
            bdId.text = vm.getNewQuestionerID().toString()
            bdNo.setText(vm.getCurrent().toString())
            bdDirection.setText("HORIZONTAL")
            bdAnswer.setText("")
        }

        if (inputMode == InputMode.EDIT) {
            bdDirection.setText(bgDirection.text)
            bdSwDirection.isChecked = bgDirection.text != "HORIZONTAL"
            bdId.text = bgQuestId.text
            bdNo.setText(bgNo.text)
            bdAsk.setText(bgAsk.text)
            bdAnswer.setText(bgAnswer.text)
        }

        false.also {
            bdId.isEnabled = it
            bdDirection.isEnabled = it
            bdNo.isEnabled = it
        }
        true.also { bdSwAutoFill.isChecked = it }
        bdAnswer.hint = "${vm.getAvailableRight()} box available"
        bdMembers.text = "${vm.nextBoxExists()}"
        bdAsk.requestFocus()

        bdSwDirection.setOnClickListener() {
            if (!bdSwDirection.isChecked) {
                bdDirection.setText("HORIZONTAL")
                bdAnswer.setText("")
                bdAnswer.hint = "${vm.getAvailableRight()} box available"
                bdMembers.text = vm.nextBoxExists().toString()
            } else {
                bdDirection.setText("VERTICAL")
                bdAnswer.setText("")
                bdAnswer.hint = "${vm.getAvailableDown()} box available"
                bdMembers.text = vm.downBoxExists().toString()
            }
        }

        bdSwAutoFill.setOnClickListener() {
            bdAnswer.setText("")
        }

        bdAnswer.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (bdSwAutoFill.isChecked) {
                    if (bdSwDirection.isChecked) {
                        bdAnswer.filters += InputFilter.LengthFilter(vm.getAvailableDown())
                    }
                    if (!bdSwDirection.isChecked) {
                        bdAnswer.filters += InputFilter.LengthFilter(vm.getAvailableRight())
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}

        })

        bdBtnUpdate.setOnClickListener() {
            //--AUTO FILL
            val str = bdAnswer.text.toString()
            val dir = bdDirection.text.toString()
            val box = if (dir == "HORIZONTAL") vm.nextBoxExists() else vm.downBoxExists()

            //TODO: Remove Prev Data on Current Set

            //Add Member
            val members = Data.partList
            if (bdSwAutoFill.isChecked) {
                val row = if (dir == "HORIZONTAL") bdId.text else ""
                val col = if (dir == "HORIZONTAL") "" else bdId.text
                for (i in str.indices) {
                    vm.boxView[box[i]].text = str[i].toString().uppercase()
                    members.add(
                        Data.Parts(
                            vm.levelId,
                            bdId.text.toString(),
                            UUID.randomUUID().toString(),
                            box[i].toString(),
                            str[i].toString().uppercase(),
                            row.toString(),
                            row.isNotEmpty(),
                            col.toString(),
                            col.isNotEmpty()
                        ),
                    )
                }
            }


            //--SAVE DATA
            if (inputMode == InputMode.NEW) {
                saveQuestioner(
                    InputMode.NEW,
                    vm.levelId,
                    bdId.text.toString(),
                    bdNo.text.toString(),
                    bdAsk.text.toString(),
                    bdAnswer.text.toString(),
                    bdDirection.text.toString(),
                    members
                )
            }
            if (inputMode == InputMode.EDIT) {
                saveQuestioner(
                    InputMode.EDIT,
                    vm.levelId,
                    bdId.text.toString(),
                    bdNo.text.toString(),
                    bdAsk.text.toString(),
                    bdAnswer.text.toString(),
                    bdDirection.text.toString(),
                    members
                )
            }

            dialog.dismiss()
        }

        bdBtnCancel.setOnClickListener() {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveQuestioner(
        mode: InputMode,
        levelId: String,
        id: String,
        no: String,
        ask: String,
        answer: String,
        direction: String,
        members: MutableList<Data.Parts>
    ) {
        val data = Data.questionList

        if (mode == InputMode.NEW) {
            data.add(Data.Questions(levelId, id, no, ask, answer, direction, members))
        }
        if (mode == InputMode.EDIT) {
            val sf =
                data.filter { it.levelId == levelId && it.id == id && it.direction == direction }
            sf.map { it }.forEach() {
                it.no = no
                it.direction = direction
                it.ask = ask
                it.answer = answer
                it.members = members
            }
        }
        initSpItems()
        loadPart()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode in 29..54) {
            val s = event?.displayLabel
            vm.boxView[vm.getCurrent()].text = s.toString()
        } else {
            return false
        }

        return super.onKeyDown(keyCode, event)
    }

    private fun initBoxView() {
        for (i in 0 until 100) {
            val child = binding.included1.boardTen.getChildAt(i)
            if (child is TextView) vm.boxView.add(child)
        }
        vm.apply { setBoxView(BoxSet.CLEAR_TEXT) }
    }


} //end
