package com.rendrapcx.tts.helper

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.constant.Const.Companion.currentIndex
import com.rendrapcx.tts.constant.Const.Companion.inputMode
import com.rendrapcx.tts.constant.Const.InputQuestionDirection
import com.rendrapcx.tts.constant.Direction
import com.rendrapcx.tts.constant.InputMode
import com.rendrapcx.tts.constant.SelectRequest
import com.rendrapcx.tts.databinding.DialogInputSoalBinding
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.ui.BoardActivity
import kotlinx.coroutines.launch
import java.util.UUID

class Questioner {
    @RequiresApi(Build.VERSION_CODES.R)
    fun Context.inputDialogQuestioner(
        context: Context,
        position: Int,
        rowCount: Int,
        rowAvailable: List<Int>,
        colCount: Int,
        colAvailable: List<Int>,
        lifecycle: Lifecycle
    ) {

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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

            addQuestion(id,number,asking,answer,direction, rowAvailable, colAvailable)
            addPartial(id, answer, direction, rowAvailable, colAvailable)

            // FIXME: ini gak bisa call, jadi force close, pake dulu yang disanalah
//            BoardActivity().apply {
//                applicationContext.getInputAnswerDirection()
//                applicationContext.onClickBox()
//            }

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

        if (inputMode == InputMode.NEW.name) {
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
        if (inputMode == InputMode.EDIT.name) {
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
        val boxAvailable = if (direction == InputQuestionDirection.HORIZONTAL.name ) {
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

        // TODO: nanti ganti biar gak nimpah anu geus aya, beh teu kudu pake clip
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

    private fun saveLevel(context: Context, lifecycle: Lifecycle) {
        val levelId = Const.currentLevel
        lifecycle.coroutineScope.launch {
            val level = DB.getInstance(context.applicationContext).level()
            level.insertLevel(
                level = Data.Level(
                    id = levelId,
                    category = "testing baru",
                    dimension = "15x15"
                )
            )
        }
    }

    private fun saveQuestioner(
        context: Context,
        lifecycle: Lifecycle,
    ) {
        val levelId = Const.currentLevel
        lifecycle.coroutineScope.launch {
            Data.listQuestion.filter { it.levelId == levelId }.map { it }.forEach() {
                DB.getInstance(context.applicationContext).question().insertQuestion(
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
        }
    }

    private fun savePartial(context: Context, lifecycle: Lifecycle) {
        val levelId = Const.currentLevel
        lifecycle.coroutineScope.launch {
            Data.listPartial.filter { it.levelId == levelId }.map { it }.forEach() {
                DB.getInstance(context.applicationContext).partial().insertPartial(
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
        }
    }

}