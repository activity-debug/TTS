package com.rendrapcx.tts.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.rendrapcx.tts.R
import com.rendrapcx.tts.constant.Const.AnswerStatus
import com.rendrapcx.tts.constant.Const.BoardSet
import com.rendrapcx.tts.constant.Const.Companion.boardSet
import com.rendrapcx.tts.constant.Const.Companion.currentCategory
import com.rendrapcx.tts.constant.Const.Companion.currentIndex
import com.rendrapcx.tts.constant.Const.Companion.currentLevel
import com.rendrapcx.tts.constant.Const.Companion.hargaPalu
import com.rendrapcx.tts.constant.Const.Companion.inputMode
import com.rendrapcx.tts.constant.Const.Companion.isEnableClick
import com.rendrapcx.tts.constant.Const.Companion.koinPay
import com.rendrapcx.tts.constant.Const.Companion.koinUser
import com.rendrapcx.tts.constant.Const.Companion.listSelesai
import com.rendrapcx.tts.constant.Const.Companion.position
import com.rendrapcx.tts.constant.Const.Direction
import com.rendrapcx.tts.constant.Const.FilterStatus
import com.rendrapcx.tts.constant.Const.InputAnswerDirection
import com.rendrapcx.tts.constant.Const.InputMode
import com.rendrapcx.tts.constant.Const.InputQuestionDirection
import com.rendrapcx.tts.constant.Const.SelectRequest
import com.rendrapcx.tts.databinding.ActivityBoardBinding
import com.rendrapcx.tts.databinding.DialogInputSoalBinding
import com.rendrapcx.tts.databinding.DialogWinBinding
import com.rendrapcx.tts.helper.Helper
import com.rendrapcx.tts.helper.Keypad
import com.rendrapcx.tts.helper.MPlayer
import com.rendrapcx.tts.helper.Progress
import com.rendrapcx.tts.helper.Sora
import com.rendrapcx.tts.helper.UserRef
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.model.Data.Companion.listLevel
import com.rendrapcx.tts.model.Data.Companion.listPartial
import com.rendrapcx.tts.model.Data.Companion.listQuestion
import com.rendrapcx.tts.model.Data.Companion.userPreferences
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class BoardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBoardBinding
    private var box = arrayListOf<TextView>()
    private var intKey = arrayListOf<TextView>()
    private var inputAnswerDirection = InputAnswerDirection.ROW
    private var tipTop = true
    private var pickByArrow = false
    private val xLen = 10
    private val yLen = 10
    private var countXY = (xLen * yLen)
    private var currentQuestId = ""
    private var currentRange = arrayListOf<Int>()
    private var tag = arrayListOf<Int>()

    private var selectedQuestion = ""
    private var onType = false

    private var curPartId = ""
    private var curRowId = ""
    private var curColId = ""
    private var curCharAt = 0
    private var curCharStr = ""
    private var finishedId = arrayListOf<String>() //ganti nanti
    private var salah = arrayListOf<Int>()
    private var hammerAct = false
    private val arrPayed = arrayListOf<Int>()

    private var isNext = false
    private var indexOfCategory = ""

    private var acakHolder = arrayListOf<String>()

    private lateinit var mAdView: AdView

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Helper().apply { hideSystemUI() }

        loadBannerAds()

        isEnableClick = true


        binding.includeEditor.mainContainer.visibility = View.GONE
        binding.includeQuestionSpan.tvSpanQuestion.text = ""
        binding.includeHeader.include.tvKoin.text = koinUser.toString()
        binding.imgHammer.visibility = View.INVISIBLE

        initBoardChild()
        initIntKeyChild()

        when (boardSet) {
            BoardSet.EDITOR_NEW -> {
                editorNew()
            }

            BoardSet.EDITOR_EDIT -> {
                editorEdit()
            }

            BoardSet.PLAY_KATEGORI -> {
                playNext()
            }

            BoardSet.PLAY_RANDOM -> {
                playRandom()
            }

        }

        binding.includeKeyboard.apply {
            btnBackSpace.setOnClickListener {
                if (!arrPayed.contains(position)) {
                    box[position].text = ""
                    upsertUserSlot(position, box[position].text.toString())
                }
                onPressBackSpace()
                MPlayer().sound(applicationContext, Sora.TYPING)
                YoYo.with(Techniques.Landing)
                    .duration(500)
                    .playOn(btnBackSpace)
            }
            btnShuffle.setOnClickListener {
                lifecycle.coroutineScope.launch {
                    for (i in 0 until intKey.size) {
                        YoYo.with(Techniques.RotateIn)
                            .onEnd {
                                YoYo.with(Techniques.Landing).duration(500).playOn(intKey[i])
                                showAnswerKeypad()
                            }
                            .playOn((intKey[i]))
                        delay(50)
                    }
                }
                MPlayer().sound(applicationContext, Sora.SHUFFLE)
                YoYo.with(Techniques.Landing)
                    .duration(500)
                    .playOn(btnShuffle)
            }

            /* KEYBOARD PRESS */
            for (i in 0 until intKey.size) {
                intKey[i].setOnTouchListener(View.OnTouchListener { _, motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            MPlayer().sound(applicationContext, Sora.TYPING)
                            if (isEnableClick == false) return@OnTouchListener true
                            if (!arrPayed.contains(position)) {
                                upsertUserSlot(position, intKey[i].text.toString())
                                box[position].text = intKey[i].text
                            }
                            onPressAbjabMove()
                            checkWinCondition(false)
                            YoYo.with(Techniques.Wave)
                                .duration(2000)
                                .onEnd {
                                    isEnableClick == true
                                }
                                .playOn(box[position])
                        }

                        MotionEvent.ACTION_UP -> {
                            YoYo.with(Techniques.Landing)
                                .playOn(intKey[i])
                        }
                    }
                    return@OnTouchListener true
                })
            }
        }

        /* HEADER ACTIONS*/
        binding.includeHeader.apply {
            btnBack.setOnClickListener {
                val intent =
                    if (boardSet == BoardSet.PLAY_KATEGORI || boardSet == BoardSet.PLAY_RANDOM)
                        Intent(this@BoardActivity, MainActivity::class.java)
                    else Intent(this@BoardActivity, QuestionActivity::class.java)
                startActivity(intent)
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
            btnSettingPlay.setOnClickListener {
                if (isEnableClick) {
                    Dialog().apply { settingDialog(this@BoardActivity, lifecycle) }
                    isEnableClick = false
                }
            }
        }

        /*  BOX CLICK ACTION*/
        binding.includeBoard.boardTen.setOnClickListener {
            for (i in 0 until box.size) {
                box[i].setOnClickListener {
                    if (isEnableClick) {
                        if (!hammerAct) MPlayer().sound(applicationContext, Sora.BOXES)
                        pickByArrow = false
                        position = i
                        setInputAnswerDirection()
                        onClickBox()

                        when (boardSet) {
                            BoardSet.PLAY_KATEGORI, BoardSet.PLAY_RANDOM -> {
                                if (userPreferences[0].integratedKeyboard) {
                                    Keypad().showSoftKeyboard(window, it)
                                }
                            }

                            else -> {}
                        }

                        if (hammerAct) {
                            isEnableClick = false
                            hammerAction(position)
                        }
                    }
                }
            }
        }

        /* SELECT QUESTION BY ARROW */
        binding.includeQuestionSpan.apply {
            /*ARROW NEXT QUESTION*/
            btnNextQuestion.setOnClickListener {
                if (listPartial.isEmpty()) return@setOnClickListener
                MPlayer().sound(applicationContext, Sora.ARROW)
                getRequestQuestions(SelectRequest.NEXT)
                moveToRequestedQuestion()
                YoYo.with(Techniques.RubberBand).duration(1300).playOn(btnNextQuestion)
                YoYo.with(Techniques.FadeOutRight).duration(1000)
                    .onEnd {
                        binding.includeQuestionSpan.tvSpanQuestion.text = getQuestion()
                        YoYo.with(Techniques.FadeInLeft).duration(300)
                            .playOn(tvSpanQuestion)
                    }
                    .playOn(tvSpanQuestion)
            }
            /*ARROR PREV QUESTION*/
            btnPrevQuestion.setOnClickListener {
                if (listPartial.isEmpty()) return@setOnClickListener
                MPlayer().sound(applicationContext, Sora.ARROW)
                getRequestQuestions(SelectRequest.PREV)
                moveToRequestedQuestion()
                YoYo.with(Techniques.RubberBand).duration(1300).playOn(btnPrevQuestion)
                YoYo.with(Techniques.FadeOutLeft).duration(1000)
                    .onEnd {
                        binding.includeQuestionSpan.tvSpanQuestion.text = getQuestion()
                        YoYo.with(Techniques.FadeInRight).duration(300).playOn(tvSpanQuestion)
                    }
                    .playOn(tvSpanQuestion)
            }

            tvSpanQuestion.setOnClickListener {
                MPlayer().sound(applicationContext, Sora.BOXES)
                YoYo.with(Techniques.RubberBand).duration(1300).playOn(tvSpanQuestion)
                for (i in currentRange.indices) {
                    if (box[currentRange[i]].text.isEmpty()) {
                        position = currentRange[i]
                        resetBoxStyle()
                        box[position].setTextColor(getColor(this@BoardActivity, R.color.white))
                        box[position].setBackgroundResource(R.drawable.box_shape_selected)
                        YoYo.with(Techniques.Bounce)
                            .onEnd { YoYo.with(Techniques.RubberBand).playOn(box[position]) }
                            .playOn(box[position])
                        break
                    }
                }
                setColorizeRange(position, currentRange)

                if (boardSet == BoardSet.EDITOR_EDIT || boardSet == BoardSet.EDITOR_NEW) {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(this@BoardActivity)
                    val input = EditText(this@BoardActivity)
                    builder
                        .setTitle("Update Soal")
                        .setMessage("${currentQuestId} : ${tvSpanQuestion.text}")
                        .setView(input)
                        .setPositiveButton("Update",
                            DialogInterface.OnClickListener { dialog, whichButton ->
                                val value: Editable = input.text
                                listQuestion.filter { it.id == currentQuestId }
                                    .map { it.asking = value.toString() }
                                onClickBox()
                                dialog.dismiss()
                            }).setNegativeButton("Batal",
                            DialogInterface.OnClickListener { dialog, whichButton ->
                                dialog.dismiss()
                            }).show()
                }

            }
        }

        /*GAME HELPER ACTIONS*/
        binding.includeGameHelperBottom.apply {
            /*NINJA*/
            btnNinja.setOnClickListener {
                lifecycleScope.launch {
                    skipActions(0)
                    val job = async {
                        MPlayer().sound(applicationContext, Sora.NINJA)
                        checkWinCondition(color = true)
                        YoYo.with(Techniques.Shake).duration(1000)
                            .onEnd {
                                YoYo.with(Techniques.RotateIn)
                                    .onEnd {
                                        skipActions(1)
                                    }
                                    .playOn(btnNinja)
                            }
                            .playOn(btnNinja)
                    }
                    job.await()
                }
            }

            btnHammer.setOnClickListener() {
                if (koinUser < hargaPalu) {
                    YoYo.with(Techniques.Shake).playOn(btnHammer)
                    YoYo.with(Techniques.Shake).playOn(btnHammerLabel)
                    return@setOnClickListener
                }

                skipActions(0)
                coinDrop(2)
                koinUser = koinUser - hargaPalu
                UserRef().setKoin(koinUser, applicationContext, lifecycle)

                btnHammer.setBackgroundResource(R.drawable.shape_game_helper_not_active)
                btnHammer.setImageResource(R.drawable.hammer_solid_not_active)
                btnHammerLabel.setTextColor(getColor(this@BoardActivity, R.color.button2_text))

                MPlayer().sound(applicationContext, Sora.TOOLBOX)
                YoYo.with(Techniques.Wave).playOn(btnHammerLabel)
                YoYo.with(Techniques.Wave)
                    .onEnd {
                        YoYo.with(Techniques.Wobble).playOn(btnHammerLabel)
                        YoYo.with(Techniques.RotateIn)
                            .onEnd {
                                for (i in tag) {
                                    box[i].setTextColor(
                                        getColor(
                                            this@BoardActivity,
                                            R.color.button
                                        )
                                    )
                                    box[i].setBackgroundResource(R.drawable.box_shape_not_pass)
                                    YoYo.with(Techniques.Pulse).repeat(5).duration(700)
                                        .playOn(box[i])
                                }
                                hammerAct = true
                            }
                            .playOn(btnHammer)
                    }
                    .playOn(btnHammer)
            }

            /*CURSOR FIRST OR LAST*/
            btnCursor.setOnClickListener {
                cursorFirstOrLast()
                YoYo.with(Techniques.RotateIn).playOn(btnCursor)
                MPlayer().sound(applicationContext, Sora.SHUFFLE)
            }
            /*ISI SOAL*/
            btnRobot.setOnClickListener {
                if (koinUser < koinPay) {
                    YoYo.with(Techniques.Shake).playOn(btnRobot)
                    YoYo.with(Techniques.Shake).playOn(btnRobotLabel)
                    return@setOnClickListener
                }

                skipActions(0)
                coinDrop(2)
                koinUser = koinUser - koinPay
                UserRef().setKoin(koinUser, applicationContext, lifecycle)

                btnRobot.setBackgroundResource(R.drawable.shape_game_helper_not_active)
                btnRobot.setImageResource(R.drawable.robot_solid_not_active)
                btnRobotLabel.setTextColor(getColor(this@BoardActivity, R.color.button2_text))

                MPlayer().sound(applicationContext, Sora.ROBOT)
                YoYo.with(Techniques.Wave).playOn(btnRobotLabel)
                YoYo.with(Techniques.Wave)
                    .onEnd {
                        randomFillAText()
                        YoYo.with(Techniques.RotateIn).playOn(btnRobot)
                        YoYo.with(Techniques.Wobble).playOn(btnRobotLabel)
                        skipActions(1)
                    }
                    .playOn(btnRobot)
            }

            /*Kasih tau jawaban 1 row atau kolom*/
            btnGetHint.setOnClickListener {
                if (koinUser < (koinPay * currentRange.size)) {
                    YoYo.with(Techniques.Shake).playOn(btnGetHint)
                    YoYo.with(Techniques.Shake).playOn(btnHintLabel)
                    return@setOnClickListener
                }

                skipActions(0)
                coinDrop(3)
                var size = currentRange.size
                for (i in currentRange) {
                    if (arrPayed.contains(i)) size = size -1
                }
                koinUser = koinUser - (koinPay * size)
                UserRef().setKoin(koinUser, applicationContext, lifecycle)

                btnGetHint.setBackgroundResource(R.drawable.shape_game_helper_not_active)
                btnGetHint.setImageResource(R.drawable.hand_point_up_solid_not_active)
                btnHintLabel.setTextColor(getColor(this@BoardActivity, R.color.button2_text))

                MPlayer().sound(applicationContext, Sora.HINT)
                YoYo.with(Techniques.Wave).playOn(btnHintLabel)
                YoYo.with(Techniques.Wave)
                    .onEnd {
                        randomFillAQuestion()
                        YoYo.with(Techniques.RotateIn).playOn(btnGetHint)
                        YoYo.with(Techniques.Wobble).playOn(btnHintLabel)
                        skipActions(1)
                    }
                    .playOn(btnGetHint)
            }
        }

        /* EDITOR BINDING ACTION                                                                 */
        binding.includeEditor.apply {

            if (boardSet == BoardSet.PLAY_KATEGORI || boardSet == BoardSet.PLAY_RANDOM) return

            /*SAVE QUESTION*/
            btnSave.setOnClickListener {
                if (listQuestion.isEmpty()) {
                    Dialog().showDialog(this@BoardActivity, "Data masih kosong")
                } else {
                    saveAndApply()
                }
            }

            /* CLEAR BOXES RESET QUESTION*/
            btnClear.setOnClickListener {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@BoardActivity)
                builder
                    .setMessage("Yakin mau balikan dari awal?")
                    .setTitle("Info")
                    .setPositiveButton("YA") { dialog, _ ->
                        listPartial.clear()
                        position = 0
                        currentRange.clear()
                        currentIndex = -1
                        currentQuestId = ""
                        tag.clear()
                        for (i in 0 until box.size) {
                            box[i].text = ""
                        }
                        resetBoxStyle()
                        fillTextDescription()
                        setInputAnswerDirection()
                        onClickBox()
                        Dialog().apply { inputDescription(binding) }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Batal") { dialog, _ ->
                        dialog.dismiss()
                    }

                val dialog: AlertDialog = builder.create()
                extracted(dialog)

                dialog.window!!.attributes.windowAnimations = R.style.DialogFadeAnim
                dialog.window!!.attributes.gravity = Gravity.NO_GRAVITY
                dialog.setCancelable(true)

                dialog.show()
            }

            btnAdd.setOnClickListener {
                inputMode = InputMode.NEW.name
                inputDataQuestioner(
                    position,
                    boxRowOvers().count(),
                    boxRowOvers(),
                    boxColumnOvers().count(),
                    boxColumnOvers(),
                )
            }

            //EDIT DESCRIPTION
            btnEdit.setOnClickListener {
                Dialog().apply { inputDescription(binding) }
            }
        }

    } //onCreate

    private fun coinDrop(int: Int) {
        when (int) {
            1 -> MPlayer().sound(applicationContext, Sora.COIN_1)
            2 -> MPlayer().sound(applicationContext, Sora.COIN_2)
            3 -> MPlayer().sound(applicationContext, Sora.COIN_3)
        }
        YoYo.with(Techniques.SlideOutDown).repeat(1)
            .onEnd {
                YoYo.with(Techniques.SlideInDown)
                    .playOn(binding.includeHeader.include.imageView)
                YoYo.with(Techniques.Bounce).repeat(1)
                    .playOn(binding.includeHeader.include.imageView)
                YoYo.with(Techniques.Pulse).repeat(1)
                    .onEnd {
                        binding.includeHeader.include.tvKoin.text = koinUser.toString()
                    }
                    .playOn(binding.includeHeader.include.tvKoin)
            }
            .playOn(binding.includeHeader.include.imageView)
    }

    private fun hammerAction(position: Int) {
        isEnableClick = false
        val i = position

        binding.imgHammer.visibility = View.VISIBLE
        val hammer = binding.imgHammer
        val params = hammer.layoutParams as ConstraintLayout.LayoutParams
        params.leftMargin =
            box[i].left - (box[i].width / 2) // absolute x position
        params.topMargin = box[i].top - 20   // absolute y position
        hammer.requestLayout()
        MPlayer().sound(applicationContext, Sora.HAMMER)
        YoYo.with(Techniques.RubberBand).repeat(5).playOn(box[i])
        YoYo.with(Techniques.Wave).duration(500).repeat(8)
            .onEnd {
                MPlayer().sound(applicationContext, Sora.DING)
                box[i].text = box[i].tag.toString()
                upsertUserSlot(i, box[i].tag.toString(), i)
                arrPayed.add(i)
                binding.imgHammer.visibility = View.INVISIBLE
                YoYo.with(Techniques.RubberBand).repeat(2).playOn(box[i])
                YoYo.with(Techniques.Bounce).repeat(2).playOn(box[i])
                binding.includeGameHelperBottom.btnHammer.setBackgroundResource(R.drawable.shape_game_helper_active)
                binding.includeGameHelperBottom.btnHammer.setImageResource(R.drawable.hammer_solid)
                binding.includeGameHelperBottom.btnHammerLabel.setTextColor(
                    getColor(
                        this,
                        R.color.button
                    )
                )
                skipActions(1)
                hammerAct = false
                isEnableClick = true
                checkWinCondition(color = false)
            }
            .playOn(hammer)
    }

    private fun gameHelperStyle() {
        /*HELPER STYLE BY KOIN CHANGE*/
        var size = currentRange.size
        for (i in currentRange) {
            if (arrPayed.contains(i)) size = size -1
        }

        if (koinUser < (koinPay * size)) {
            binding.includeGameHelperBottom.apply {
                btnGetHint.setBackgroundResource(R.drawable.shape_game_helper_not_active)
                btnGetHint.setImageResource(R.drawable.hand_point_up_solid_not_active)
                btnHintLabel.setTextColor(getColor(this@BoardActivity, R.color.button2_text))
                btnHintLabel.text = "${koinPay * size}"
            }
        } else {
            binding.includeGameHelperBottom.apply {
                btnGetHint.setBackgroundResource(R.drawable.shape_game_helper_active)
                btnGetHint.setImageResource(R.drawable.hand_point_up_solid)
                btnHintLabel.setTextColor(getColor(this@BoardActivity, R.color.button))
                btnHintLabel.text = "${koinPay * size}"
            }
        }
        if (koinUser < koinPay) {
            binding.includeGameHelperBottom.apply {
                btnRobot.setBackgroundResource(R.drawable.shape_game_helper_not_active)
                btnRobot.setImageResource(R.drawable.robot_solid_not_active)
                btnRobotLabel.setTextColor(getColor(this@BoardActivity, R.color.button2_text))
            }
        } else {
            binding.includeGameHelperBottom.apply {
                btnRobot.setBackgroundResource(R.drawable.shape_game_helper_active)
                btnRobot.setImageResource(R.drawable.robot_solid)
                btnRobotLabel.setTextColor(getColor(this@BoardActivity, R.color.button))
            }
        }
        if (koinUser < hargaPalu) {
            binding.includeGameHelperBottom.apply {
                btnHammer.setBackgroundResource(R.drawable.shape_game_helper_not_active)
                btnHammer.setImageResource(R.drawable.hammer_solid_not_active)
                btnHammerLabel.setTextColor(getColor(this@BoardActivity, R.color.button2_text))

            }
        } else {
            binding.includeGameHelperBottom.apply {
                btnHammer.setBackgroundResource(R.drawable.shape_game_helper_active)
                btnHammer.setImageResource(R.drawable.hammer_solid)
                btnHammerLabel.setTextColor(getColor(this@BoardActivity, R.color.button))
            }
        }
    }

    private fun editorEdit() {
        lifecycleScope.launch {
            val job1 = async { listPartial = getPartialData() }
            job1.await()
            Dialog().apply { inputDescription(binding) }

            binding.apply {
                includeEditor.btnClear.visibility = View.INVISIBLE
                includeEditor.mainContainer.visibility = View.VISIBLE
                includeKeyboard.integratedKeyboard.visibility = View.GONE
                includeQuestionSpan.tvSpanQuestion.text = ""
                includeGameHelperBottom.bottomHelper.visibility = View.GONE
                includeHeader.include.componentKoin.visibility = View.GONE
                includeHeader.tvLabelTop.text =
                    listLevel.first { it.id == currentLevel }.title
            }

            position = listPartial.first { it.levelId == currentLevel }.charAt

            setBoxTagText()
            fillTextDescription()
            fillText()
            pickByArrow = false
            setInputAnswerDirection()
            onClickBox()
        }
    }

    private fun editorNew() {
        lifecycleScope.launch {
            val job = async {
                currentLevel = Helper().generateLevelId(listLevel.size)
                listLevel.add(
                    Data.Level(currentLevel, "2024", "Title", "Andra", FilterStatus.DRAFT)
                )
            }
            job.await()

            binding.apply {
                includeEditor.btnClear.visibility = View.INVISIBLE
                includeEditor.mainContainer.visibility = View.VISIBLE
                includeKeyboard.integratedKeyboard.visibility = View.GONE
                includeQuestionSpan.tvSpanQuestion.text = ""
                includeGameHelperBottom.bottomHelper.visibility = View.GONE
                includeHeader.include.componentKoin.visibility = View.GONE
                includeHeader.tvLabelTop.text =
                    listLevel.first { it.id == currentLevel }.title
            }

            listQuestion.clear()
            listPartial.clear()

            Dialog().apply { inputDescription(binding) }

            position = 0
            setBoxTagText()
            pickByArrow = false
            setInputAnswerDirection()
            onClickBox()
            fillTextDescription()
            setOnSelectedColor()
        }
    }

    private fun deleteUserSlotDone() {
        lifecycleScope.launch {
            DB.getInstance(applicationContext).userAnswerSlot().deleteSlotById(currentLevel)
        }
    }

    private fun loadUserState() {
        lifecycleScope.launch {
            val userAnswerSlot = DB.getInstance(applicationContext)
                .userAnswerSlot().getAnswerSlot(currentLevel)
                .ifEmpty { return@launch }
            userAnswerSlot.map { it.answerSlot }.forEach {
                for (i in it.keys) {
                    YoYo.with(Techniques.FlipInY).duration(2000).playOn(box[i])
                    box[i].text = it.getValue(i)
                    delay(50)
                }
            }
            userAnswerSlot.map { it.payed }.forEach() {
                if (it != -1) arrPayed.add(it)
            }
        }
    }

    private fun upsertUserSlot(charAt: Int, charStr: String, payed: Int = -1) {
        lifecycleScope.launch {
            val answerSlot = mutableMapOf<Int, String>()
            answerSlot.put(charAt, charStr)
            DB.getInstance(applicationContext).userAnswerSlot().upsertSlot(
                Data.UserAnswerSlot(
                    id = currentLevel + "-" + charAt.toString(),
                    levelId = currentLevel,
                    answerSlot = answerSlot,
                    payed = payed
                )
            )
        }
    }

    private fun loadBannerAds() {
        MobileAds.initialize(this@BoardActivity) {}
        mAdView = binding.adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        mAdView.adListener = object : AdListener() {
            override fun onAdClicked() {}
            override fun onAdClosed() {}
            override fun onAdFailedToLoad(adError: LoadAdError) {}
            override fun onAdImpression() {}
            override fun onAdLoaded() {}
            override fun onAdOpened() {}
        }

    }

    /*Random isi soal Telunjuk*/
    private fun randomFillAQuestion() {
        lifecycle.coroutineScope.launch {
            skipActions(0)
            //kasih jawaban
            val job = async {
                for (i in currentRange.indices) {
                    val x = currentRange[i]
                    MPlayer().sound(applicationContext, Sora.TYPING)
                    if (inputAnswerDirection == InputAnswerDirection.ROW) {
                        YoYo.with(Techniques.Hinge)
                            .onEnd {
                                position = x
                                setOnSelectedColor()
                                box[x].text = box[x].tag.toString()
                                upsertUserSlot(x, box[x].tag.toString(), x)
                                arrPayed.add(x)
                                YoYo.with(Techniques.RollIn)
                                    .onEnd {
                                        YoYo.with(Techniques.Bounce).playOn(box[x])
                                        YoYo.with(Techniques.RubberBand).repeat(1).playOn(box[x])
                                    }
                                    .playOn(box[x])
                            }
                            .playOn(box[x])
                    } else {
                        YoYo.with(Techniques.Hinge)
                            .onEnd {
                                position = x
                                setOnSelectedColor()
                                box[x].text = box[x].tag.toString()
                                upsertUserSlot(x, box[x].tag.toString(), x)
                                arrPayed.add(x)
                                YoYo.with(Techniques.SlideInUp)
                                    .onEnd {
                                        YoYo.with(Techniques.Bounce).playOn(box[x])
                                        YoYo.with(Techniques.RubberBand).repeat(1).playOn(box[x])
                                    }
                                    .playOn(box[x])
                            }
                            .playOn(box[x])
                    }
                    delay(100)
                }
            }
            job.await()

            YoYo.with(Techniques.Pulse).repeat(1).duration(300)
                .onEnd {
                    MPlayer().sound(applicationContext, Sora.BONUS)
                    YoYo.with(Techniques.RubberBand)
                        .onEnd {
                            pickByArrow = false
                            setInputAnswerDirection()
                            onClickBox()
                            checkWinCondition(color = false)
                        }
                        .playOn(box[position])
                }
                .playOn(binding.includeGameHelperBottom.btnGetHint)
            skipActions(1)
        }
    }

    /*RANDOM ISI TEXT ROBOT*/
    private fun randomFillAText() {
        lifecycle.coroutineScope.launch {
            skipActions(0)
            val lastPos = position
            val arr = arrayListOf<Int>()
            val job = async {
                MPlayer().sound(applicationContext, Sora.ROBOT_RANDOM)
                for (i in tag.indices) {
                    resetBoxStyle()
                    position = tag[i]
                    setOnSelectedColor()
                    if (box[tag[i]].text != box[tag[i]].tag) arr.add(tag[i])

                    delay(30)
                }
            }
            job.await()

            MPlayer().sound(applicationContext, Sora.HINT_RANDOM)

            var x: Int
            if (arr.isNotEmpty()) {
                x = arr.random()
                val job1 = async {
                    for (i in arr.indices) {
                        resetBoxStyle()
                        position = arr[i]
                        setOnSelectedColor()
                        delay(100)
                        if (arr[i] == x) break
                    }
                }
                job1.await()
            } else {
                x = lastPos
            }

            YoYo.with(Techniques.Pulse).repeat(1).duration(300)
                .onEnd {
                    MPlayer().sound(applicationContext, Sora.SUCCESS)
                    position = x
                    box[x].text = box[x].tag.toString()
                    upsertUserSlot(x, box[x].tag.toString(), x)
                    arrPayed.add(x)
                    pickByArrow = false
                    setInputAnswerDirection()
                    onClickBox()
                    checkWinCondition(color = false)
                }
                .playOn(binding.includeGameHelperBottom.btnRobot)

            skipActions(1)
        }
    }


    private fun cursorFirstOrLast() {
        lifecycle.coroutineScope.launch {
            skipActions(0)
            val job = async {
                if (position == currentRange[0]) {
                    val arr = currentRange
                    for (i in currentRange.indices) {
                        position = currentRange[i]
                        pickByArrow = false
                        setOnSelectedColor()
                        setColorizeRange(position, arr)
                        YoYo.with(Techniques.RubberBand)
                            .duration(300)
                            .playOn(box[currentRange[i]])
                        delay(150)
                    }
                } else {
                    var arr = currentRange
                    val arrDesc = currentRange.sortedDescending()
                    for (i in arrDesc.indices) {
                        position = arrDesc[i]
                        pickByArrow = false
                        setOnSelectedColor()
                        setColorizeRange(position, arr)
                        YoYo.with(Techniques.RubberBand)
                            .duration(300)
                            .playOn(box[arrDesc[i]])
                        delay(150)
                    }
                }
                delay(1000)
            }
            job.await()
            skipActions(1)
        }
    }

    private fun skipActions(int: Int) {
        when (int) {
            0 -> {
                binding.includeQuestionSpan.tvSpanQuestion.isEnabled = false
                binding.includeQuestionSpan.btnPrevQuestion.isEnabled = false
                binding.includeQuestionSpan.btnNextQuestion.isEnabled = false
                binding.includeGameHelperBottom.btnGetHint.isEnabled = false
                binding.includeGameHelperBottom.btnNinja.isEnabled = false
                binding.includeGameHelperBottom.btnCursor.isEnabled = false
                binding.includeGameHelperBottom.btnRobot.isEnabled = false
                binding.includeGameHelperBottom.btnHammer.isEnabled = false
                binding.includeKeyboard.btnBackSpace.isEnabled = false
                binding.includeKeyboard.btnShuffle.isEnabled = false
                for (i in 0 until intKey.size) {
                    intKey[i].isEnabled = false
                }
            }

            1 -> {
                binding.includeQuestionSpan.tvSpanQuestion.isEnabled = true
                binding.includeQuestionSpan.btnPrevQuestion.isEnabled = true
                binding.includeQuestionSpan.btnNextQuestion.isEnabled = true
                binding.includeGameHelperBottom.btnGetHint.isEnabled = true
                binding.includeGameHelperBottom.btnNinja.isEnabled = true
                binding.includeGameHelperBottom.btnCursor.isEnabled = true
                binding.includeGameHelperBottom.btnRobot.isEnabled = true
                binding.includeGameHelperBottom.btnHammer.isEnabled = true
                binding.includeKeyboard.btnBackSpace.isEnabled = true
                binding.includeKeyboard.btnShuffle.isEnabled = true
                for (i in 0 until intKey.size) {
                    intKey[i].isEnabled = true
                }
            }
        }
    }

    private fun moveToRequestedQuestion() {
        val arr = arrayListOf<Int>()

        if (boardSet == BoardSet.PLAY_KATEGORI || boardSet == BoardSet.PLAY_RANDOM) {
            for (i in 0 until currentRange.size) {
                arr.add(i)
                if (box[currentRange[i]].text.isEmpty()) {
                    position = currentRange[i]
                    break
                }
            }
            if (arr.size == currentRange.size) {
                position = currentRange[currentRange.size - 1]
            }
        } else position = currentRange[0]

        pickByArrow = false
        setOnSelectedColor()
        setColorizeRange(position, currentRange)

        showAnswerKeypad()
    }

    private fun getPartialData(): MutableList<Data.Partial> {
        val levelId = currentLevel
        val part = mutableListOf<Data.Partial>()
        val soal = listQuestion

        for (i in soal.indices) {
            for (x in soal[i].slot.indices) {
                val slotPart = part.indexOfFirst { it.charAt == soal[i].slot[x] }
                var prevId = ""
                if (slotPart != -1) prevId =
                    part[slotPart].rowQuestionId.ifEmpty { part[slotPart].colQuestionId }
                        .ifEmpty { "" }
                part.add(
                    Data.Partial(
                        levelId = levelId,
                        id = UUID.randomUUID().toString(),
                        charAt = soal[i].slot[x],
                        charStr = soal[i].answer[x].toString(),
                        rowQuestionId = if (soal[i].direction == InputQuestionDirection.H.name) soal[i].id
                        else prevId,
                        colQuestionId = if (soal[i].direction == InputQuestionDirection.V.name) soal[i].id
                        else prevId,
                    )
                )
            }
        }

        return part
    }

    private fun fillTextDescription() {
        if (boardSet == BoardSet.EDITOR_NEW || boardSet == BoardSet.EDITOR_EDIT) {
            listLevel.filter { it.id == currentLevel }.forEach {
                binding.includeEditor.apply {
                    textLevelId.text = it.id
                    textCategory.text = it.category
                    textTitle.text = it.title
                    textCreator.text = it.userId
                }
            }
        }
    }

    private fun setInputAnswerDirection() {
        if (getColumnId() != "") inputAnswerDirection = InputAnswerDirection.COLUMN
        else if (getRowId() != "") inputAnswerDirection = InputAnswerDirection.ROW
    }

    private fun setTiptopRangeDirection() {
        if (getColumnId() != "" && getRowId() == "") InputAnswerDirection.COLUMN //InputDirection.COLUMN
        else if (getColumnId() == "" && getRowId() != "") InputAnswerDirection.ROW //InputDirection.ROW
        //else if (getColumnId() == "" && getRowId() == "") InputAnswerDirection.UNKNOWN //InputDirection.UNKNOWN
        else {
            inputAnswerDirection =
                if (tipTop) InputAnswerDirection.COLUMN
                else InputAnswerDirection.ROW
        }
    }

    private fun onClickBox() {
        setOnSelectedColor()
        setOnRangeStyle()
        showAnswerKeypad()
        binding.includeQuestionSpan.tvSpanQuestion.text = selectedQuestion
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun saveAndApply() {
        lifecycleScope.launch {
            val levelId = currentLevel
            val job1 = async {
                val level = DB.getInstance(applicationContext).level()
                level.insertLevel(
                    level = Data.Level(
                        id = levelId,
                        category = binding.includeEditor.textCategory.text.toString(),
                        title = binding.includeEditor.textTitle.text.toString(),
                        userId = binding.includeEditor.textCreator.text.toString(),
                        status = FilterStatus.DRAFT
                    )
                )
            }
            job1.await()

            val job2 = async {
                listQuestion.filter { it.levelId == levelId }.map { it }.forEach {
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
            }
            job2.await()

            Dialog().showDialog(this@BoardActivity, "Data berhasil disimpan")
        }

    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (!isEnableClick) return false
        val x = position
        when (keyCode) {
            in 29..54 -> {
                val s = event?.displayLabel
                if (!arrPayed.contains(x)) {
                    box[x].text = s.toString()
                    upsertUserSlot(x, s.toString())
                }
                onPressAbjabMove()
                checkWinCondition(false)
                YoYo.with(Techniques.Landing)
                    .duration(1000)
                    .playOn(box[x])
            }

            67 -> {
                if (box[x].isFocused) {
                    if (!arrPayed.contains(x)) box[x].text = ""
                    onPressBackSpace()
                }
            }

            else -> return false
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun onPressBackSpace() {
        if (!isEnableClick) return
        onType = true
        val x = position
        if (inputAnswerDirection == InputAnswerDirection.ROW) {
            position = x - 1
            if (position in currentRange) {
                YoYo.with(Techniques.Landing)
                    .duration(1000)
                    .playOn(box[x])
                setOnSelectedColor()
            } else position = currentRange[0]
        }
        if (inputAnswerDirection == InputAnswerDirection.COLUMN) {
            position = x - xLen
            if (position in currentRange) {
                YoYo.with(Techniques.Landing)
                    .duration(1000)
                    .playOn(box[x])
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
                YoYo.with(Techniques.Wave)
                    .duration(1000)
                    .playOn(box[x])
                setOnSelectedColor()
            } else position = x
        }
        if (inputAnswerDirection == InputAnswerDirection.COLUMN) {
            position = selectNextColumn()
            if (position in currentRange) {
                YoYo.with(Techniques.Wave)
                    .duration(1000)
                    .playOn(box[x])
                setOnSelectedColor()
            } else position = x
        }
        onType = false
    }

    /* CHECK WIN*/
    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkWinCondition(color: Boolean = true) {
        if (boardSet == BoardSet.EDITOR_EDIT || boardSet == BoardSet.EDITOR_NEW) return
        var pass = true
        salah.clear()
        for (i in tag) {
            if (box[i].text != box[i].tag) {
                if (color) {
                    salah.add(i)
                    box[i].setTextColor(getColor(this, R.color.button))
                    box[i].setBackgroundResource(R.drawable.box_shape_not_pass)
                    YoYo.with(Techniques.Shake).repeat(1).duration(1000)
                        .onEnd {
                            val x = salah[0]
                            pickByArrow = false
                            position = x
                            setInputAnswerDirection()
                            onClickBox()
                            YoYo.with(Techniques.Bounce).playOn(box[x])
                            showAnswerKeypad()
                            binding.includeQuestionSpan.tvSpanQuestion.text = getQuestion()
                        }
                        .playOn(box[i])
                }
                pass = false
            }
        }

        if (pass) {
            isEnableClick = false

            koinUser += 500
            UserRef().setKoin(koinUser, applicationContext, lifecycle)
            YoYo.with(Techniques.SlideOutDown).repeat(1)
                .onEnd {
                    YoYo.with(Techniques.SlideInUp).playOn(binding.includeHeader.include.imageView)
                    YoYo.with(Techniques.Bounce).repeat(1)
                        .playOn(binding.includeHeader.include.imageView)
                    YoYo.with(Techniques.Wobble).duration(1000)
                        .onEnd { binding.includeHeader.include.tvKoin.text = koinUser.toString() }
                        .playOn(binding.includeHeader.include.tvKoin)
                }
                .playOn(binding.includeHeader.include.imageView)

            if (boardSet != BoardSet.PLAY_RANDOM) {
                Progress().updateUserAnswer(AnswerStatus.DONE, applicationContext, lifecycle)
                deleteUserSlotDone()
                MPlayer().sound(applicationContext, Sora.WINNING)
                winDialog(this@BoardActivity)
            } else {
                deleteUserSlotDone()
                MPlayer().sound(applicationContext, Sora.WINNING)
                winDialog(this@BoardActivity)
            }
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

        dialog.window!!.attributes.windowAnimations = R.style.DialogFadeAnim
        dialog.window!!.attributes.gravity = Gravity.TOP
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        lifecycleScope.launch {

            if (boardSet == BoardSet.PLAY_RANDOM)
                bind.tvSelamat.text = "Lanjutkan bermain?"
            else bind.tvSelamat.text =
                "Berhasil melewati \nLevel $indexOfCategory \nkategori $currentCategory"

            var dataLevel = mutableListOf<Data.Level>()

            val job = async {
                dataLevel = DB.getInstance(applicationContext).level().getAllByCategory(
                    currentCategory
                ).filter { it.status == FilterStatus.POST }.toMutableList()
                val dataDone = DB.getInstance(applicationContext).userAnswerTTS()
                    .getStatus(AnswerStatus.DONE.name)
                listSelesai.clear()
                dataDone.forEach {
                    listSelesai.add(it.levelId)
                }
            }
            job.await()

            if (boardSet != BoardSet.PLAY_RANDOM) {
                val ct = dataLevel.map { it.id }
                if (listSelesai.containsAll(ct)) {
                    bind.tvSelamat.text = "Selamat \nanda sudah menyelesaikan\n" +
                            "Kategori $currentCategory"
                    bind.btnNext.visibility = View.GONE
                }
            }

            YoYo.with(Techniques.ZoomIn).duration(1000)
                .onEnd { YoYo.with(Techniques.Tada).repeat(2).playOn(bind.imgCongrate) }
                .playOn(bind.imgCongrate)
            YoYo.with(Techniques.RubberBand).repeat(1).playOn(bind.tvSelamat)

            bind.btnNext.setOnClickListener {
                if (boardSet == BoardSet.PLAY_RANDOM) {
                    playRandom()
                    dialog.dismiss()
                } else {
                    isNext = true
                    playNext()
                    dialog.dismiss()
                }
            }

            bind.btnBack.setOnClickListener {
                listSelesai = Progress().getUserSelesai(applicationContext, lifecycle)
                val i = Intent(context.applicationContext, MainActivity::class.java)
                startActivity(i)
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun playRandom() {
        lifecycleScope.launch {
            skipActions(0)
            boardSet = BoardSet.PLAY_RANDOM
            arrPayed.clear()
            for (i in 0 until box.size) {
                box[i].text = ""
                box[i].tag = ""
                box[i].visibility = View.VISIBLE
            }

            listLevel.clear()


            val jobLevel = async {
                val dataDone = DB.getInstance(applicationContext).userAnswerTTS()
                    .getStatus(AnswerStatus.DONE.name)
                listSelesai.clear()
                dataDone.forEach {
                    listSelesai.add(it.levelId)
                }

                listLevel = DB.getInstance(applicationContext).level().getAllLevel()
                val count = listLevel.size
                if (acakHolder.size >= listSelesai.size) {
                    acakHolder.clear()
                }

                for (i in 0 until count) {
                    val x = (1 until count).random()

                    if (acakHolder.isEmpty()) {
                        acakHolder.add(listLevel[x].id)
                        currentLevel = listLevel[x].id
                        break
                    }

                    if (acakHolder.contains(listLevel[x].id)) {
                        continue
                    }

                    if (listSelesai.contains(listLevel[x].id)) {
                        acakHolder.add(listLevel[x].id)
                        currentLevel = listLevel[x].id
                        break
                    }
                }
            }
            jobLevel.await()

            listQuestion.clear()
            val jobQuest = async {
                listQuestion =
                    DB.getInstance(applicationContext).question().getQuestion(currentLevel)
            }
            jobQuest.await()

            listPartial.clear()
            val jobPart = async { listPartial = getPartialData() }
            jobPart.await()

            MPlayer().sound(applicationContext, Sora.INIT_GAME_2)

            binding.apply {
                includeEditor.mainContainer.visibility = View.GONE

                val index = listLevel.indexOfFirst { it.id == currentLevel }
                val category = listLevel[index].category
                val msg = "Level ke:  ${Helper().formatLevelId(index + 1)} \n" +
                        "Kategori: ${category}"
                includeHeader.tvLabelTop.text = msg
            }

            isEnableClick = true

            setBoxTagText()
            position = listPartial.first { it.levelId == currentLevel }.charAt
            pickByArrow = false
            setInputAnswerDirection()
            onClickBox()
            skipActions(1)
        }
    }

    private fun playNext() {
        lifecycleScope.launch {
            skipActions(0)
            //Initial cari LevelId
            var dataLevel = mutableListOf<Data.Level>()
            //var dataDone = mutableListOf<Data.UserAnswerTTS>()
            val job = async {
                dataLevel = DB.getInstance(applicationContext).level().getAllByCategory(
                    currentCategory
                ).filter { it.status == FilterStatus.POST }.toMutableList()
                val dataDone = DB.getInstance(applicationContext).userAnswerTTS()
                    .getStatus(AnswerStatus.DONE.name)
                listSelesai.clear()
                dataDone.forEach {
                    listSelesai.add(it.levelId)
                }
            }
            job.await()

            val ct = dataLevel.map { it.id }
            if (isNext == true) {
                for (i in ct) {
                    if (i !in listSelesai) {
                        currentLevel = i
                        break
                    }
                }
            }
            Progress().updateUserAnswer(AnswerStatus.PROGRESS, applicationContext, lifecycle)

            //GET DATA from New currentLevel
            boardSet = BoardSet.PLAY_KATEGORI

            listLevel.clear()
            listQuestion.clear()
            listPartial.clear()
            arrPayed.clear()

            val jobGetDB = async {
                listLevel.clear()
                listLevel =
                    DB.getInstance(applicationContext).level().getLevel(currentLevel)
                listQuestion.clear()
                listQuestion =
                    DB.getInstance(applicationContext).question()
                        .getQuestion(currentLevel)
            }
            jobGetDB.await()

            val jobExtract = async { listPartial = getPartialData() }
            jobExtract.await()

            binding.apply {
                includeEditor.mainContainer.visibility = View.GONE
                includeQuestionSpan.tvSpanQuestion.text = ""

                binding.includeHeader.include.tvKoin.text = koinUser.toString()

                val index = dataLevel.indexOfFirst { it.id == currentLevel }
                indexOfCategory = Helper().formatLevelId(index + 1)
                val category = currentCategory
                val msg =
                    "Level ke: ${indexOfCategory} \n" +
                            "Kategori: ${category}"
                includeHeader.tvLabelTop.text = msg

                MPlayer().sound(applicationContext, Sora.INIT_GAME_1)
                /*/DI PLAYNEXT*/
                for (i in 0 until box.size) {
                    box[i].visibility = View.VISIBLE
                    box[i].text = ""
                    box[i].tag = ""
                    YoYo.with(Techniques.RotateInUpRight).duration(1000).playOn(box[i])
                    delay(5)
                }

            }

            isEnableClick = true

            position = listPartial.first { it.levelId == currentLevel }.charAt

            val job1 = async {
                setBoxTagText()
            }
            job1.await()

            position = listPartial.first { it.levelId == currentLevel }.charAt
            pickByArrow = false
            setInputAnswerDirection()
            setOnSelectedColor()
            setOnRangeStyle()
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
            .map { it }.forEach {
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
            .map { it }.forEach {
                result = it.asking
            }
        return result
    }

    private fun getRequestQuestions(selectRequest: SelectRequest) {
        if (listQuestion.isEmpty()) return

        val index = if (currentIndex < 0) {
            listQuestion.indexOfFirst { it.levelId == currentLevel && it.id == currentQuestId }
        } else currentIndex

        val count = listQuestion.count { it.levelId == currentLevel }

        val req = if (selectRequest == SelectRequest.NEXT) {
            if (index < count - 1) index + 1 else 0
        } else {
            if (index > 0) index - 1 else count - 1
        }

        currentIndex = req

        val reqId = listQuestion.filter { it.levelId == currentLevel }[req].id

        var range = arrayListOf<Int>()
        var dir = Direction.H.name
        listQuestion.filter { it.levelId == currentLevel && it.id == reqId }
            .map { it }.forEach {
                dir = it.direction
                range = it.slot
            }

        inputAnswerDirection =
            if (dir == Direction.H.name) InputAnswerDirection.ROW
            else InputAnswerDirection.COLUMN

        currentQuestId = reqId
        currentRange = range
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

    private fun setBoxTagText() {
        when (boardSet) {
            BoardSet.EDITOR_NEW -> {
                for (i in 0 until box.size) {
                    box[i].text = ""
                }
                resetBoxStyle()
            }

            BoardSet.EDITOR_EDIT -> {
                tag.clear()
                resetBoxStyle()
            }

            BoardSet.PLAY_KATEGORI, BoardSet.PLAY_RANDOM -> {
                MPlayer().sound(applicationContext, Sora.INIT_GAME_2)
                tag.clear()
                listPartial.filter { it.levelId == currentLevel }.forEach {
                    for (i in 0 until box.size) {
                        if (i == it.charAt) {
                            box[i].text = ""
                            box[i].tag = it.charStr
                            tag.add(it.charAt)
                        }
                    }
                }
                lifecycle.coroutineScope.launch {
                    for (i in 0 until box.size) {
                        if (box[i].tag == "") {
                            if (boardSet == BoardSet.PLAY_KATEGORI || boardSet == BoardSet.PLAY_RANDOM) {
                                YoYo.with(Techniques.DropOut).duration(1000)
                                    .onEnd {
                                        box[i].visibility = View.INVISIBLE
                                    }
                                    .playOn(box[i])
                            }
                        }
                        delay(40)
                    }
                    for (i in 0 until intKey.size) {
                        binding.includeGameHelperBottom.apply {
                            YoYo.with(Techniques.RotateIn).duration(1000).playOn(btnGetHint)
                            YoYo.with(Techniques.RotateIn).duration(1000).playOn(btnHintLabel)
                            YoYo.with(Techniques.RotateIn).duration(1000).playOn(btnRobot)
                            YoYo.with(Techniques.RotateIn).duration(1000).playOn(btnRobotLabel)
                            YoYo.with(Techniques.RotateIn).duration(1000).playOn(btnHammer)
                            YoYo.with(Techniques.RotateIn).duration(1000).playOn(btnHammerLabel)
                            YoYo.with(Techniques.RotateIn).duration(1000).playOn(btnCursor)
                            YoYo.with(Techniques.RotateIn).duration(1000).playOn(btnNinja)
                        }
                        YoYo.with(Techniques.RotateIn)
                            .onEnd {
                                YoYo.with(Techniques.Landing).duration(500).playOn(intKey[i])
                                showAnswerKeypad()
                                binding.includeQuestionSpan.tvSpanQuestion.text = selectedQuestion
                                skipActions(1)
                            }
                            .playOn((intKey[i]))
                        delay(40)
                    }

                    loadUserState()
                }
                resetBoxStyle()
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
                box[x].setBackgroundResource(R.drawable.box_shape_range)
                if (box[x].text.isEmpty()) {
                    YoYo.with(Techniques.RubberBand)
                        .repeat(1)
                        .playOn(box[x])
                } else {
                    YoYo.with(Techniques.RubberBand).repeat(1).playOn(box[x])
                }
            }
        }
    }

    private fun setOnRangeStyle() {
        val range: ArrayList<Int>

        if (!pickByArrow) {
            tipTop = tipTop != true
            setTiptopRangeDirection()
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
        gameHelperStyle()
    }

    private fun setOnSelectedColor() {
        resetBoxStyle()
        val i = position
        box[i].setTextColor(getColor(this, R.color.white))
        box[i].setBackgroundResource(R.drawable.box_shape_selected)
        YoYo.with(Techniques.Bounce)
            .onEnd { YoYo.with(Techniques.RubberBand).playOn(box[i]) }
            .playOn(box[i])
    }

    private fun fillText() {
        if (boardSet == BoardSet.EDITOR_NEW || boardSet == BoardSet.EDITOR_EDIT) {
            listPartial.filter { it.levelId == currentLevel }.map { it }.forEach {
                for (i in 0 until box.size)
                    if (i == it.charAt) {
                        box[i].text = it.charStr
                        box[i].tag = it.charStr
                        tag.add(it.charAt)
                    }
            }
        }
    }

    private fun resetBoxStyle() {
        for (i in 0 until box.size) {
            if (arrPayed.contains(i)) box[i].setTextColor(getColor(this, R.color.payed))
            else box[i].setTextColor(getColor(this, R.color.button))
            box[i].setBackgroundResource(R.drawable.box_shape_active)
        }
        gameHelperStyle()

    }

    private fun initIntKeyChild() {
        val count = binding.includeKeyboard.integratedKeyboard.childCount
        for (i in 0 until count) {
            val child = binding.includeKeyboard.integratedKeyboard.getChildAt(i)
            if (child is TextView) {
                intKey.add(child)
                intKey[i].text = ""
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
                box[i].setBackgroundResource(R.drawable.box_shape_active)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun inputDataQuestioner(
        position: Int,
        rowCount: Int,
        rowAvailable: List<Int>,
        colCount: Int,
        colAvailable: List<Int>,
    ) {

        val inflater =
            this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val bind = DialogInputSoalBinding.inflate(inflater)
        val builder = AlertDialog.Builder(this).setView(bind.root)
        val dialog = builder.create()

        extracted(dialog)

        dialog.window!!.attributes.windowAnimations = R.style.DialogFadeAnim
        dialog.window!!.attributes.gravity = Gravity.NO_GRAVITY
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        //TODO: INITIAL NEW
        val rowFilter = arrayOf<InputFilter>(InputFilter.LengthFilter(rowCount))
        val colFilter = arrayOf<InputFilter>(InputFilter.LengthFilter(colCount))
        bind.etNoInput.setText("${position}")
        bind.etAskInput.requestFocus()
        bind.etAskInput.hint = "Pertanyaan"
        bind.etAnswerInput.filters = rowFilter
        bind.etAnswerInput.hint = "tersedia ${rowCount} kotak"
        bind.tvSlotPreview.text = "${rowAvailable}"
        bind.swDirection.text = "Mendatar" //InputQuestionDirection.H.name

        bind.textView16.visibility = View.INVISIBLE
        bind.tvIdInput.visibility = View.INVISIBLE
        bind.tvSlotPreview.visibility = View.INVISIBLE

        val row = getRowId()
        val col = getColumnId()


        if (row.isNotEmpty() && col.isNotEmpty()) {
            Toast.makeText(this, "rowId dan colomId sudah terpenuhi", Toast.LENGTH_SHORT).show()
            return
        }

        if (row.isEmpty() && col.isNotEmpty()) {
            bind.swDirection.isChecked = false
            bind.swDirection.text = "Mendatar" //InputQuestionDirection.H.name
            bind.etAnswerInput.setText("")
            bind.etAnswerInput.hint = "tersedia ${rowCount} kotak"
            bind.etAnswerInput.filters = rowFilter
            bind.tvSlotPreview.text = "${rowAvailable}"
        }
        if (row.isNotEmpty() && col.isEmpty()) {
            bind.swDirection.isChecked = true
            bind.swDirection.text = "Menurun" //InputQuestionDirection.V.name
            bind.etAnswerInput.setText("")
            bind.etAnswerInput.hint = "tersedia ${colCount} kotak"
            bind.etAnswerInput.filters = colFilter
            bind.tvSlotPreview.text = "${colAvailable}"
        }

        //FIXME: ACTIONS LISTENER
        bind.swDirection.setOnClickListener {
            if (bind.swDirection.isChecked) {
                if (col.isEmpty()) {
                    bind.swDirection.text = "Menurun" //InputQuestionDirection.V.name
                    bind.etAnswerInput.setText("")
                    bind.etAnswerInput.hint = "tersedia ${colCount} kotak"
                    bind.etAnswerInput.filters = colFilter
                    bind.tvSlotPreview.text = "${colAvailable}"
                } else {
                    bind.swDirection.isChecked = false
                    Toast.makeText(this, "Vertical not allowed", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (row.isEmpty()) {
                    bind.swDirection.text = "Mendatar" //InputQuestionDirection.H.name
                    bind.etAnswerInput.setText("")
                    bind.etAnswerInput.hint = "tersedia ${rowCount} kotak"
                    bind.etAnswerInput.filters = rowFilter
                    bind.tvSlotPreview.text = "${rowAvailable}"
                } else {
                    bind.swDirection.isChecked = true
                    Toast.makeText(this, "Horizontal not allowed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        var harusnya = arrayListOf<String>()
        fun cekInputAnswer(): Boolean {
            val answer = bind.etAnswerInput.text.trim().trimStart().trimEnd()
            val slot = if (bind.swDirection.isChecked) colAvailable else rowAvailable

            val range = ArrayList<Int>()
            for (i in 0 until answer.length) {
                range.add(slot[i])
            }

            val mapAnswer = mutableMapOf<Int, String>()
            val isBox = arrayListOf<Int>()

            for (i in 0 until box.size) {
                if (box[i].text.toString().isNotEmpty()) isBox.add(i)
            }

            val ada = arrayListOf<Int>()
            for (i in 0 until isBox.size) {
                val key = isBox[i]
                if (key in range) {
                    ada.add(key)
                }
            }

            for (i in 0 until answer.length) {
                if (range[i] in ada) mapAnswer.put(range[i], answer[i].toString())
            }

            for (i in 0 until ada.size) {
                if (box[ada[i]].text != mapAnswer.getValue(ada[i])) {
                    harusnya.add(
                        "\n box:${ada[i]}=\"${box[ada[i]].text}\" -> \"${
                            mapAnswer.getValue(
                                ada[i]
                            )
                        }\" \n"
                    )
                }
            }

            return !harusnya.isNotEmpty()
        }

        bind.btnUpdateInput.setOnClickListener {
            if (bind.etAskInput.text.isEmpty()) {
                bind.etAskInput.error = "tidak boleh kosong"
                return@setOnClickListener
            }
            if (bind.etAnswerInput.text.isEmpty()) {
                bind.etAnswerInput.error = "tidak boleh kosong"
                return@setOnClickListener
            }
            if (!bind.etAnswerInput.text.all { it.isLetter() }) {
                bind.etAnswerInput.error =
                    "hanya huruf kapital, jangan ada spasi, angka atau simbol"
                return@setOnClickListener
            }

            if (!cekInputAnswer()) {
                bind.etAnswerInput.error = "char baru tidak sesuai \n$harusnya}"
                return@setOnClickListener
            }

            val number: Int = bind.etNoInput.text.toString().toInt()
            val asking: String = bind.etAskInput.text.toString().trim()
            val answer: String = bind.etAnswerInput.text.toString().uppercase()
            val direction: String =
                if (bind.swDirection.text == "Menurun") Direction.V.name else Direction.H.name

            val subDir = direction[0].toString()
            val id: String = Helper().generateQuestionId(currentLevel, number, subDir)
            lifecycle.coroutineScope.launch {
                val job1 = async {
                    addQuestion(
                        id,
                        number,
                        asking,
                        answer,
                        direction,
                        rowAvailable,
                        colAvailable
                    )
                }
                job1.await()
                val job2 =
                    async { addPartial(id, answer, direction, rowAvailable, colAvailable) }
                job2.await()

                fillText()
                setInputAnswerDirection()
                onClickBox()
                dialog.dismiss()
            }
        }

        bind.btnCancelInput.setOnClickListener {
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
        val data = listQuestion
        val levelId = currentLevel
        val box = if (direction == InputQuestionDirection.H.name) {
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
            position = slot[0]
            currentIndex = listQuestion.count { it.levelId == levelId }
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
        val levelId = currentLevel
        val part = listPartial
        val boxAvailable = if (direction == InputQuestionDirection.H.name) {
            rowAvailable
        } else {
            colAvailable
        }

        val slot = boxAvailable.subList(0, answerText.length)

        for (i in slot.indices) {
            val slotPart = part.indexOfFirst { it.charAt == slot[i] }
            var prevId = ""
            if (slotPart != -1) prevId =
                part[slotPart].rowQuestionId.ifEmpty { part[slotPart].colQuestionId }
                    .ifEmpty { "" }
            part.add(
                Data.Partial(
                    levelId = levelId,
                    id = UUID.randomUUID().toString(),
                    charAt = boxAvailable[i],
                    charStr = answerText[i].toString(),
                    rowQuestionId = if (direction == InputQuestionDirection.H.name) questionId else prevId,
                    colQuestionId = if (direction == InputQuestionDirection.V.name) questionId else prevId,
                )
            )
        }
    }
}