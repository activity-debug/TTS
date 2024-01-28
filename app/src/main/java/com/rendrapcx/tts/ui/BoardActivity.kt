package com.rendrapcx.tts.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.view.Gravity
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
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.rendrapcx.tts.R
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.constant.Const.BoardSet
import com.rendrapcx.tts.constant.Const.Companion.boardSet
import com.rendrapcx.tts.constant.Const.Companion.currentCategory
import com.rendrapcx.tts.constant.Const.Companion.currentIndex
import com.rendrapcx.tts.constant.Const.Companion.currentLevel
import com.rendrapcx.tts.constant.Const.Companion.position
import com.rendrapcx.tts.constant.Const.Companion.progress
import com.rendrapcx.tts.constant.Const.FilterStatus
import com.rendrapcx.tts.constant.Const.InputAnswerDirection
import com.rendrapcx.tts.constant.Const.InputQuestionDirection
import com.rendrapcx.tts.constant.Direction
import com.rendrapcx.tts.constant.InputMode
import com.rendrapcx.tts.constant.SelectRequest
import com.rendrapcx.tts.databinding.ActivityBoardBinding
import com.rendrapcx.tts.databinding.DialogInputSoalBinding
import com.rendrapcx.tts.databinding.DialogWinBinding
import com.rendrapcx.tts.helper.Dialog
import com.rendrapcx.tts.helper.Helper
import com.rendrapcx.tts.helper.Keypad
import com.rendrapcx.tts.helper.Progress
import com.rendrapcx.tts.helper.Sound
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
    private var telunjuk = 0
    private var robot = 0

    private var isNext = false
    private var indexOfCategory = ""

    private lateinit var mAdView: AdView

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Helper().apply { hideSystemUI() }

        loadBannerAds()

        initBoardChild()
        initIntKeyChild()

        when (boardSet) {
            BoardSet.EDITOR_NEW -> {
                lifecycleScope.launch {
                    val job = async {
                        currentLevel = UUID.randomUUID().toString().substring(0, 20)
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
                        includeHeader.tvLabelTop.text =
                            listLevel.first() { it.id == currentLevel }.title
                    }

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

            BoardSet.EDITOR_EDIT -> {
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
                        includeHeader.tvLabelTop.text =
                            listLevel.first() { it.id == currentLevel }.title
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

            BoardSet.PLAY, BoardSet.PLAY_USER -> {
                // FIXME: kalo di declare di menu title, progress ini nanti hapus
                //Progress().updateUserAnswer(AnswerStatus.PROGRESS, this, lifecycleScope)

                playNext()
            }

            BoardSet.PLAY_NEXT -> {

            }

            BoardSet.PLAY_RANDOM -> {
                playRandom()
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
                Sound().soundTyping(this@BoardActivity)
                YoYo.with(Techniques.Landing)
                    .duration(500)
                    .playOn(btnBackSpace);
            }
            btnShuffle.setOnClickListener() {
                for (i in 0 until intKey.size) {
                    YoYo.with(Techniques.Hinge).duration(1000)
                        .onEnd {
                            YoYo.with(Techniques.RotateInDownLeft).duration(300).playOn(intKey[i])
                            showAnswerKeypad()
                        }
                        .playOn((intKey[i]))

                }
                Sound().soundShuffle(this@BoardActivity)
                YoYo.with(Techniques.Landing)
                    .duration(500)
                    .playOn(btnShuffle);
            }

            /* KEYBOARD PRESS */
            for (i in 0 until intKey.size) {
                intKey[i].setOnTouchListener(View.OnTouchListener() { _, motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            box[position].text = intKey[i].text
                            YoYo.with(Techniques.Landing)
                                .duration(1000)
                                .playOn(box[position]);
                            onPressAbjabMove()
                            checkWinCondition(false)
                            Sound().soundTyping(this@BoardActivity)
                            upsertUserSlot(position, intKey[i].text.toString())
                        }

                        MotionEvent.ACTION_UP -> {
                            YoYo.with(Techniques.Landing)
                                .playOn(intKey[i]);
                        }
                    }
                    return@OnTouchListener true
                })
            }
        }

        /* HEADER ACTIONS*/
        binding.includeHeader.apply {
            btnBack.setOnClickListener() {
                val intent = if (boardSet == BoardSet.PLAY_USER || boardSet == BoardSet.PLAY_RANDOM)
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
                    Sound().soundOnClickBox(this)
                    pickByArrow = false
                    position = i
                    setInputAnswerDirection()
                    onClickBox()

                    when (boardSet) {
                        BoardSet.PLAY, BoardSet.PLAY_USER, BoardSet.PLAY_RANDOM -> {
                            if (userPreferences[0].integratedKeyboard) {
                                Keypad().showSoftKeyboard(window, it)
                            }
                        }

                        else -> {}
                    }
                }
            }
        }

        /* SELECT QUESTION BY ARROW */
        binding.includeQuestionSpan.apply {
            /*ARROW NEXT QUESTION*/
            btnNextQuestion.setOnClickListener() {
                if (listPartial.isEmpty()) return@setOnClickListener
                Sound().soundNextQuestion(this@BoardActivity)
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
            btnPrevQuestion.setOnClickListener() {
                if (listPartial.isEmpty()) return@setOnClickListener
                Sound().soundNextQuestion(this@BoardActivity)
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

            tvSpanQuestion.setOnClickListener() {
                Sound().soundOnClickBox(this@BoardActivity)
                YoYo.with(Techniques.RubberBand).duration(1300).playOn(tvSpanQuestion)
                //pickByArrow = false
                //onClickBox()
                setColorizeRange(position, currentRange)
            }
        }

        /*GAME HELPER ACTIONS*/
        binding.includeGameHelperBottom.apply {
            /*NINJA*/
            btnNinja.setOnClickListener() {
                lifecycleScope.launch {
                    skipActions(0)
                    //btnNinja.isEnabled = true
                    val job = async {
                        Sound().soundCheckBoxPass(this@BoardActivity)
                        checkWinCondition(color = true)
                        YoYo.with(Techniques.Shake).duration(1000)
                            .onEnd {
                                YoYo.with(Techniques.RotateIn)
                                    .onEnd { skipActions(1) }
                                    .playOn(btnNinja)
                            }
                            .playOn(btnNinja)
                    }
                    job.await()
                }
            }

            /*CURSOR FIRST OR LAST*/
            btnCursor.setOnClickListener() {
                cursorFirstOrLast()
                YoYo.with(Techniques.RotateIn).playOn(btnCursor)
                Sound().soundShuffle(this@BoardActivity)
            }
            /*ISI SOAL*/
            btnRobot.setOnClickListener() {
                robot++
                if (robot > 3) {
                    YoYo.with(Techniques.Shake).playOn(btnRobot)
                    return@setOnClickListener
                }

                if (robot > 2) {
                    btnRobot.setBackgroundResource(R.drawable.shape_game_helper_not_active)
                    btnRobot.setImageResource(R.drawable.robot_solid_not_active)
                }
                Sound().soundOnRandomFill(this@BoardActivity)
                YoYo.with(Techniques.Wave).duration(1000)
                    .onEnd {
                        randomFillAText()
                        YoYo.with(Techniques.Bounce)
                            .onEnd {

                            }
                            .playOn(btnRobot)
                    }
                    .playOn(btnRobot)
            }
            /*Kasih tau jawaban 1 row atau kolom*/
            btnGetHint.setOnClickListener() {
                telunjuk++
                if (telunjuk > 1) {
                    YoYo.with(Techniques.Shake).playOn(btnGetHint)
                    return@setOnClickListener
                }
                btnGetHint.setBackgroundResource(R.drawable.shape_game_helper_not_active)
                btnGetHint.setImageResource(R.drawable.hand_point_up_solid_not_active)
                Sound().soundDingDong(this@BoardActivity)
                YoYo.with(Techniques.Wave)
                    .onEnd {
                        randomFillAQuestion()
                        YoYo.with(Techniques.RotateIn).playOn(btnGetHint)
                    }
                    .playOn(btnGetHint)
            }
        }

        /* EDITOR BINDING ACTION                                                                 */
        binding.includeEditor.apply {

            if (boardSet == BoardSet.PLAY || boardSet == BoardSet.PLAY_USER || boardSet == BoardSet.PLAY_RANDOM) return

            /*SAVE QUESTION*/
            btnSave.setOnClickListener() {
                if (listQuestion.isEmpty()) {
                    Dialog().showDialog(this@BoardActivity, "Data masih kosong")
                } else {
                    saveAndApply()
                }
            }

            /* CLEAR BOXES RESET QUESTION*/
            btnClear.setOnClickListener() {
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


    } //onCreate


    /*APA INIIIIIIIIIIIII*/
    private fun loadUserProgress() {

    }


    private fun upsertUserSlot(charAt: Int, charStr: String) {
        lifecycleScope.launch {
            val answerSlot = mutableMapOf<Int, String>()
            answerSlot.put(charAt, charStr)
            DB.getInstance(applicationContext).userAnswerSlot().upsertSlot(
                Data.UserAnswerSlot(
                    id = currentLevel + "-at-" + charAt,
                    levelId = currentLevel,
                    answerSlot = answerSlot
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
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                //Toast.makeText(this@BoardActivity, "${adError.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            override fun onAdLoaded() {
                //Toast.makeText(this@MainActivity, "adLoaded", Toast.LENGTH_SHORT).show()
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
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
                    Sound().soundTyping(this@BoardActivity)
                    if (inputAnswerDirection == InputAnswerDirection.ROW) {
                        YoYo.with(Techniques.Hinge)
                            .onEnd {
                                position = x
                                setOnSelectedColor()
                                box[x].text = box[x].tag.toString()
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

            YoYo.with(Techniques.Wave).repeat(1).duration(300)
                .onEnd {
                    Sound().soundOnFinger(this@BoardActivity)
                    YoYo.with(Techniques.RubberBand)
                        .onEnd {
                            pickByArrow = false
                            setInputAnswerDirection()
                            onClickBox()
                            checkWinCondition(color = false)
                        }
                        .playOn(box[position])
                }
                .playOn(binding.includeGameHelperBottom.btnRobot)
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
                Sound().soundOnRandom(this@BoardActivity)
                for (i in tag.indices) {
                    resetBoxStyle()
                    position = tag[i]
                    setOnSelectedColor()
                    if (box[tag[i]].text != box[tag[i]].tag) arr.add(tag[i])

                    delay(100)
                }
            }
            job.await()

            Sound().soundOnGetRandomValue(this@BoardActivity)

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

            YoYo.with(Techniques.Wave).repeat(1).duration(300)
                .onEnd {
                    Sound().soundSuccess(this@BoardActivity)
                    position = x
                    box[x].text = box[x].tag.toString()
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

        if (boardSet == BoardSet.PLAY || boardSet == BoardSet.PLAY_USER || boardSet == BoardSet.PLAY_RANDOM) {
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
                        rowQuestionId = if (soal[i].direction == InputQuestionDirection.HORIZONTAL.name) soal[i].id
                        else prevId,
                        colQuestionId = if (soal[i].direction == InputQuestionDirection.VERTICAL.name) soal[i].id
                        else prevId,
                    )
                )
            }
        }

        return part
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
            }
            job2.await()

            Dialog().showDialog(this@BoardActivity, "Data berhasil disimpan")
        }

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
                YoYo.with(Techniques.Landing)
                    .duration(1000)
                    .playOn(box[x]);
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
                YoYo.with(Techniques.Landing)
                    .duration(1000)
                    .playOn(box[x]);
                setOnSelectedColor()
            } else position = currentRange[0]
        }
        if (inputAnswerDirection == InputAnswerDirection.COLUMN) {
            position = x - xLen
            if (position in currentRange) {
                YoYo.with(Techniques.Landing)
                    .duration(1000)
                    .playOn(box[x]);
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
                    .playOn(box[x]);
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

        //onClickBox()
        if (pass) {
            if (boardSet != BoardSet.PLAY_RANDOM) {
                Progress().updateUserAnswer(
                    Const.AnswerStatus.DONE, this, lifecycle
                )
                progress = Progress().getUserProgress(this, lifecycle)
                Toast.makeText(this, "${progress.count().toString()}", Toast.LENGTH_SHORT).show()
            }
            Sound().soundWinning(this)
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

        dialog.window!!.attributes.windowAnimations = R.style.DialogFadeAnim
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        lifecycleScope.launch {
            if (boardSet == BoardSet.PLAY_RANDOM)
                bind.tvSelamat.text = "Lanjutkan bermain?"
            else bind.tvSelamat.text = "Lolos Level $indexOfCategory di kategori $currentCategory"

            // FIXME: Data level coba pake fungsi aja, soalnya dipake dua tempat sementara gini dulu
            var dataLevel = mutableListOf<Data.Level>()
            val job = async {
                dataLevel = DB.getInstance(applicationContext).level().getAllByCategory(
                    currentCategory
                )
            }
            job.await()

            if (boardSet != BoardSet.PLAY_RANDOM) {
                val ct = dataLevel.map { it.id }
                if (progress.containsAll(ct)) {
                    bind.btnNext.visibility = View.GONE
                }
            }

            bind.btnNext.setOnClickListener() {
                if (boardSet == BoardSet.PLAY_RANDOM) {
                    playRandom()
                    dialog.dismiss()
                } else {
                    isNext = true
                    playNext()
                    dialog.dismiss()
                }
            }

            bind.btnBack.setOnClickListener() {
                val i = Intent(context.applicationContext, MainActivity::class.java)
                startActivity(i)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    /* asdsd ad                                                                              */

    private fun playRandom() {
        lifecycleScope.launch {
            boardSet = BoardSet.PLAY_RANDOM

            val jobLevel = async {
                listLevel = DB.getInstance(applicationContext).level().getAllLevel()
                val count = listLevel.size
                val index = (0 until count).random()
                currentLevel = listLevel[index].id
            }
            jobLevel.await()
            val jobQuest = async {
                listQuestion =
                    DB.getInstance(applicationContext).question().getQuestion(currentLevel)
            }
            jobQuest.await()

            val jobPart = async { listPartial = getPartialData() }
            jobPart.await()

            Sound().soundStartGame(this@BoardActivity)
            binding.apply {
                includeEditor.mainContainer.visibility = View.GONE
                val index = listLevel.indexOfFirst { it.id == currentLevel }
                val category = listLevel[index].category
                val msg = "Level:  ${Helper().format(index + 1)} \n" +
                        "Category: ${category}"
                includeHeader.tvLabelTop.text = msg
            }

            for (i in 0 until box.size) {
                box[i].text = ""
                box[i].tag = ""
                box[i].visibility = View.VISIBLE
            }

            telunjuk = 0
            robot = 0
            binding.includeGameHelperBottom.apply {
                btnGetHint.setImageResource(R.drawable.hand_point_up_solid)
                btnRobot.setImageResource(R.drawable.robot_solid)
                btnGetHint.setBackgroundResource(R.drawable.shape_game_helper_active)
                btnRobot.setBackgroundResource(R.drawable.shape_game_helper_active)
            }
            setBoxTagText()
            position = listPartial.first { it.levelId == currentLevel }.charAt
            pickByArrow = false
            setInputAnswerDirection()
            onClickBox()
        }
    }


    private fun playNext() {
        lifecycleScope.launch {
            //Get Data Level by Category on Play
            var dataLevel = mutableListOf<Data.Level>()
            val job = async {
                dataLevel = DB.getInstance(applicationContext).level().getAllByCategory(
                    currentCategory
                )
            }
            job.await()

            val ct = dataLevel.map { it.id }

            if (isNext) {
                for (i in ct) {
                    if (!progress.contains(i)) {
                        currentLevel = i
                        break
                    }
                }
            }

            //GET DATA from New currentLevel
            boardSet = BoardSet.PLAY_USER

            listLevel.clear()
            listQuestion.clear()
            listPartial.clear()

            val jobGetDB = async {
                listLevel =
                    DB.getInstance(applicationContext).level().getLevel(currentLevel)
                listQuestion =
                    DB.getInstance(applicationContext).question()
                        .getQuestion(currentLevel)
            }
            jobGetDB.await()

            val jobExtract = async { listPartial = getPartialData() }
            jobExtract.await()

            Sound().soundStartGame(this@BoardActivity)
            binding.apply {
                includeEditor.mainContainer.visibility = View.GONE

                val index = dataLevel.indexOfFirst { it.id == currentLevel }
                //val title = dataLevel[index].title
                //Dialog().showDialog(this@BoardActivity, "$index")
                indexOfCategory = Helper().format(index + 1)
                val category = currentCategory
                val msg =          //"${index +1} Title:  ${title} \n" +
                    "Level ke: ${indexOfCategory} \n" +
                            "Category: ${category}"
                includeHeader.tvLabelTop.text = msg
            }

            position = listPartial.first { it.levelId == currentLevel }.charAt

            for (i in 0 until box.size) {
                box[i].text = ""
                box[i].tag = ""
                box[i].visibility = View.VISIBLE
            }

            telunjuk = 0
            robot = 0
            binding.includeGameHelperBottom.apply {
                btnGetHint.setImageResource(R.drawable.hand_point_up_solid)
                btnRobot.setImageResource(R.drawable.robot_solid)
                btnGetHint.setBackgroundResource(R.drawable.shape_game_helper_active)
                btnRobot.setBackgroundResource(R.drawable.shape_game_helper_active)
            }
            setBoxTagText()
            position = listPartial.first { it.levelId == currentLevel }.charAt
            pickByArrow = false
            setInputAnswerDirection()
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

        inputAnswerDirection =
            if (dir == Direction.HORIZONTAL.name) InputAnswerDirection.ROW
            else InputAnswerDirection.COLUMN

        currentQuestId = reqId
        currentRange = range
    }


    private fun hasBothId(): Boolean {
        val row = listPartial.filter { it.charAt == position }.first().rowQuestionId
        val col = listPartial.filter { it.charAt == position }.first().colQuestionId
        if (row.isNotEmpty() && col.isNotEmpty()) return true
        else return false
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

            BoardSet.PLAY, BoardSet.PLAY_USER, BoardSet.PLAY_RANDOM -> {
                tag.clear()
                listPartial.filter { it.levelId == currentLevel }.forEach() {
                    for (i in 0 until box.size) {
                        if (i == it.charAt) {
                            box[i].text = ""
                            box[i].tag = it.charStr
                            tag.add(it.charAt)
                        }
                    }
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

    private fun resetBoxStyle() {
        for (i in 0 until box.size) {
            if (box[i].tag == "") {
                if (boardSet == BoardSet.PLAY_USER || boardSet == BoardSet.PLAY || boardSet == BoardSet.PLAY_RANDOM) {
                    box[i].visibility = View.INVISIBLE
                }
            }
            box[i].setTextColor(getColor(this, R.color.button))
            box[i].setBackgroundResource(R.drawable.box_shape_active)
        }
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
                box[i].setBackgroundResource(R.drawable.box_shape_active)
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
        bind.etAnswerInput.filters = rowFilter
        bind.etAnswerInput.hint = "available ${rowCount} boxes"
        bind.tvSlotPreview.text = "${rowAvailable}"
        bind.swDirection.text = InputQuestionDirection.HORIZONTAL.name

        bind.textView16.visibility = View.INVISIBLE
        bind.tvIdInput.visibility = View.INVISIBLE
        bind.tvSlotPreview.visibility = View.INVISIBLE

//        val x = listPartial.indexOfFirst { it.charAt == position }
        val row = getRowId() //if (x != -1) listPartial[x].rowQuestionId else ""
        val col = getColumnId() // if (x != -1) listPartial[x].colQuestionId else ""

//        Toast.makeText(this, "$row | $col", Toast.LENGTH_SHORT).show()

        if (row.isNotEmpty() && col.isNotEmpty()) {
            Toast.makeText(this, "rowId dan colomId sudah terpenuhi", Toast.LENGTH_SHORT).show()
            //dialog.dismiss()
            return
        }
//        val firstChar = box[position].text.toString()

        if (row.isEmpty() && col.isNotEmpty()) {
            bind.swDirection.isChecked = false
            bind.swDirection.text = InputQuestionDirection.HORIZONTAL.name
            bind.etAnswerInput.setText("")
            bind.etAnswerInput.hint = "available ${rowCount} boxes"
            bind.etAnswerInput.filters = rowFilter
            bind.tvSlotPreview.text = "${rowAvailable}"
        }
        if (row.isNotEmpty() && col.isEmpty()) {
            bind.swDirection.isChecked = true
            bind.swDirection.text = InputQuestionDirection.VERTICAL.name
            bind.etAnswerInput.setText("")
            bind.etAnswerInput.hint = "available ${colCount} boxes"
            bind.etAnswerInput.filters = colFilter
            bind.tvSlotPreview.text = "${colAvailable}"
        }

        //FIXME: ACTIONS LISTENER
        bind.swDirection.setOnClickListener() {
            if (bind.swDirection.isChecked) {
                if (col.isEmpty()) {
                    bind.swDirection.text = InputQuestionDirection.VERTICAL.name
                    bind.etAnswerInput.setText("")
                    bind.etAnswerInput.hint = "available ${colCount} boxes"
                    bind.etAnswerInput.filters = colFilter
                    bind.tvSlotPreview.text = "${colAvailable}"
                } else {
                    bind.swDirection.isChecked = false
                    Toast.makeText(this, "Vertical not allowed", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (row.isEmpty()) {
                    bind.swDirection.text = InputQuestionDirection.HORIZONTAL.name
                    bind.etAnswerInput.setText("")
                    bind.etAnswerInput.hint = "available ${rowCount} boxes"
                    bind.etAnswerInput.filters = rowFilter
                    bind.tvSlotPreview.text = "${rowAvailable}"
                } else {
                    bind.swDirection.isChecked = true
                    Toast.makeText(this, "Horizontal not allowed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        /* Harus ada Checking kalo input berada dalam range, dan nilainya berbeda*/
        //Toast.makeText(this, "${currentRange}", Toast.LENGTH_SHORT).show()

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

            //val ada = mutableMapOf<Int, String>()
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

            //val mapBeda = mutableMapOf<Int, String>()
            //val mapBefore = mutableMapOf<Int, String>()
            for (i in 0 until ada.size) {
                if (box[ada[i]].text != mapAnswer.getValue(ada[i])) {
                    //mapBeda.put(ada[i], mapAnswer.getValue(ada[i]))
                    //mapBefore.put(ada[i], box[ada[i]].text.toString())
                    harusnya.add(
                        "\n box:${ada[i]}=\"${box[ada[i]].text}\" -> \"${
                            mapAnswer.getValue(
                                ada[i]
                            )
                        }\" \n"
                    )
                }
            }


            if (harusnya.isNotEmpty()) return false
            else return true
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
                bind.etAnswerInput.error =
                    "hanya huruf kapital, jangan ada spasi, angka atau simbol"
                return@setOnClickListener
            }


            if (!cekInputAnswer()) {
                bind.etAnswerInput.error = "char baru tidak susuai \n ${harusnya.toString()}}"
                return@setOnClickListener
            }

            val id: String = UUID.randomUUID().toString()
            val number: Int = bind.etNoInput.text.toString().toInt()
            val asking: String = bind.etAskInput.text.toString().trim()
            val answer: String = bind.etAnswerInput.text.toString().uppercase()
            val direction: String = bind.swDirection.text.toString()

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
        val levelId = currentLevel
        val part = listPartial
        val boxAvailable = if (direction == InputQuestionDirection.HORIZONTAL.name) {
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
                    rowQuestionId = if (direction == InputQuestionDirection.HORIZONTAL.name) questionId else prevId,
                    colQuestionId = if (direction == InputQuestionDirection.VERTICAL.name) questionId else prevId,
                )
            )
        }
    }
}