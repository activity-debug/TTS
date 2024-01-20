package com.rendrapcx.tts.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.rendrapcx.tts.R
import com.rendrapcx.tts.databinding.ActivityTebakKataBinding
import com.rendrapcx.tts.helper.Helper
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data.Companion.listTebakKata
import kotlinx.coroutines.launch

class TebakKataActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTebakKataBinding
    private var sp = ArrayList<TextView>()
    private var kb = ArrayList<TextView>()
    private var countTBK = 0
    private var curId = ""
    private var curIndex = -1
    private var listPlayed = ArrayList<String>()

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTebakKataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Helper().apply { hideSystemUI() }

        initSp()
        initKb()

        playTBK()


        /*MULAI ACTION LISTENER*/

        /*Keyboard Action*/
        for (i in 0 until kb.size) {
            kb[i].setOnClickListener() {
                for (x in 0 until 45) {
                    YoYo.with(Techniques.Bounce)
                        .onEnd {
                            YoYo.with(Techniques.Swing).repeat(1)
                                .onEnd {
                                    kb[i].text = ""
                                    kb[i].isEnabled = false
                                }
                                .playOn(sp[x])
                        }
                        .playOn(kb[i])
                    if (sp[x].tag.toString() == kb[i].text.toString()) {
                        YoYo.with(Techniques.Bounce).duration(2000)
                            .onEnd {
                                YoYo.with(Techniques.Bounce).duration(2000)
                                    .onEnd { YoYo.with(Techniques.Tada).repeat(1).playOn(kb[i]) }
                                    .onEnd { YoYo.with(Techniques.Tada).playOn(sp[x]) }
                                    .onEnd { YoYo.with(Techniques.Bounce).playOn(sp[x]) }
                                    .onEnd { YoYo.with(Techniques.Landing).playOn(sp[x]) }
                                    .onEnd { sp[x].text = sp[x].tag.toString() }
                                    .playOn(sp[x])
                            }
                            .playOn(kb[i])
                    }
                }
            }
        }

        /*btnHint*/
        binding.apply {
            btnHint1.setOnClickListener() { activeHint(1) }
            btnHint2.setOnClickListener() { activeHint(2) }
            btnHint3.setOnClickListener() { activeHint(3) }
            btnHint4.setOnClickListener() { activeHint(4) }
            btnHint5.setOnClickListener() { activeHint(5) }
        }

        /*bottom actions*/
        binding.gameHelperBottom.apply {
            btnGetHint.setOnClickListener() { /* hints aktif dari reward */ }
            btnSuffleKey.setOnClickListener() {
                YoYo.with(Techniques.Shake)
                    .onEnd { shuffleKey(1) }
                    .playOn(btnSuffleKey)

            }
            btnHideEmpty.setOnClickListener() {
                for (i in 0 until sp.size) {
                    if (sp[i].tag !in Helper().abjadKapital()) {
                        YoYo.with(Techniques.Shake)
                            .onEnd { YoYo.with(Techniques.Shake)
                                .onEnd {
                                    sp[i].visibility = View.INVISIBLE
                                    btnHideEmpty.visibility = View.INVISIBLE
                                }
                                .playOn(sp[i]) }
                            .playOn(btnHideEmpty)
                    }
                }
            }
            btnShowPicture.setOnClickListener() {
                YoYo.with(Techniques.Flash)
                    .onEnd {
                        YoYo.with(Techniques.Bounce)
                            .onEnd {
                                binding.tvHider.setBackgroundColor(getColor(R.color.hider_intip))
                                btnShowPicture.visibility = View.INVISIBLE }
                            .playOn(btnShowPicture)
                    }
                    .playOn(btnShowPicture)
            }
        }

        binding.headerContent.apply {
            btnBack.setOnClickListener() {
                val i = Intent(this@TebakKataActivity, MainActivity::class.java)
                startActivity(i)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }
    }

    /*Tambahin argument parameter index, listtbk keluarin pisahin*/
    private fun playTBK() {
        lifecycleScope.launch {
            listTebakKata = DB.getInstance(applicationContext).tebakKata().getAllTbk()

            /* GetUserPlayed & Current */


            clearSpTagText()
            binding.tvHintSpan.text = ""

            if (curId.isEmpty()) curId = listTebakKata.first().id
            curIndex = listTebakKata.indexOfFirst { it.id == curId }

            val url = listTebakKata[curIndex].imageUrl
            if (url.isEmpty()) binding.imgSoal.setImageResource(R.drawable.terka_box)
            else binding.imgSoal.setImageURI(url.toUri())

            binding.tvQuestionTbk.text = listTebakKata[curIndex].asking

            val answer = listTebakKata[curIndex].answer
            for (i in answer.indices) {
                sp[i].tag = answer[i].toString().uppercase()
            }

            binding.btnHint1.tag = listTebakKata[curIndex].hint1
            binding.btnHint2.tag = listTebakKata[curIndex].hint2
            binding.btnHint3.tag = listTebakKata[curIndex].hint3
            binding.btnHint4.tag = listTebakKata[curIndex].hint4
            binding.btnHint5.tag = listTebakKata[curIndex].hint5

            binding.gameHelperBottom.btnShowPicture.visibility = View.VISIBLE
            binding.headerContent.tvLabelTop.text = binding.tvQuestionTbk.text

            shuffleKey(0)
            activeHint()
        }
    }

    private fun shuffleKey(int: Int) {
        when (int) {
            0 -> {
                val key = Helper().abjadKapital().shuffled()
                for (i in 0 until kb.size) {
                    kb[i].text = key[i]
                }
            }

            1 -> {
                val new = mutableListOf<String>()
                for (i in 0 until kb.size) {
                    if (kb[i].isEnabled) new.add(kb[i].text.toString())
                }
                new.shuffle()
                for (i in 0 until kb.size) {
                    if (kb[i].isEnabled) {
                        kb[i].text = new[0]
                        new.removeAt(0)
                        YoYo.with(Techniques.RotateIn).playOn(kb[i])
                    }
                }
            }
        }

    }

    private fun clearSpTagText() {
        for (i in 0 until sp.size) {
            sp[i].text = ""
            sp[i].tag = ""
        }
    }

    private fun activeHint(int: Int = 1) {
        when (int) {
            1 -> {
                binding.tvHintSpan.text = listTebakKata[curIndex].hint1
                binding.btnHint1.setBackgroundResource(R.drawable.box_shape_hint_selected)
                binding.btnHint2.setBackgroundResource(R.drawable.box_shape_hint)
                binding.btnHint3.setBackgroundResource(R.drawable.box_shape_hint)
                binding.btnHint4.setBackgroundResource(R.drawable.box_shape_hint)
                binding.btnHint5.setBackgroundResource(R.drawable.box_shape_hint)
            }

            2 -> {
                binding.tvHintSpan.text = listTebakKata[curIndex].hint2
                binding.btnHint1.setBackgroundResource(R.drawable.box_shape_hint)
                binding.btnHint2.setBackgroundResource(R.drawable.box_shape_hint_selected)
                binding.btnHint3.setBackgroundResource(R.drawable.box_shape_hint)
                binding.btnHint4.setBackgroundResource(R.drawable.box_shape_hint)
                binding.btnHint5.setBackgroundResource(R.drawable.box_shape_hint)
            }

            3 -> {
                binding.tvHintSpan.text = listTebakKata[curIndex].hint3
                binding.btnHint1.setBackgroundResource(R.drawable.box_shape_hint)
                binding.btnHint2.setBackgroundResource(R.drawable.box_shape_hint)
                binding.btnHint3.setBackgroundResource(R.drawable.box_shape_hint_selected)
                binding.btnHint4.setBackgroundResource(R.drawable.box_shape_hint)
                binding.btnHint5.setBackgroundResource(R.drawable.box_shape_hint)
            }

            4 -> {
                binding.tvHintSpan.text = listTebakKata[curIndex].hint4
                binding.btnHint1.setBackgroundResource(R.drawable.box_shape_hint)
                binding.btnHint2.setBackgroundResource(R.drawable.box_shape_hint)
                binding.btnHint3.setBackgroundResource(R.drawable.box_shape_hint)
                binding.btnHint4.setBackgroundResource(R.drawable.box_shape_hint_selected)
                binding.btnHint5.setBackgroundResource(R.drawable.box_shape_hint)
            }

            5 -> {
                binding.tvHintSpan.text = listTebakKata[curIndex].hint5
                binding.btnHint1.setBackgroundResource(R.drawable.box_shape_hint)
                binding.btnHint2.setBackgroundResource(R.drawable.box_shape_hint)
                binding.btnHint3.setBackgroundResource(R.drawable.box_shape_hint)
                binding.btnHint4.setBackgroundResource(R.drawable.box_shape_hint)
                binding.btnHint5.setBackgroundResource(R.drawable.box_shape_hint_selected)
            }
        }
    }

    private fun initKb() {
        val count = binding.includeKeyboardAbc.keyboardAbc.childCount
        for (i in 0 until count) {
            val child = binding.includeKeyboardAbc.keyboardAbc.getChildAt(i)
            if (child is TextView) {
                kb.add(child)
            }
        }
    }

    private fun initSp() {
        val count = binding.includeSiapa.boardSiapa.childCount
        for (i in 0 until count) {
            val child = binding.includeSiapa.boardSiapa.getChildAt(i)
            if (child is TextView) {
                sp.add(child)
            }
        }
    }
}