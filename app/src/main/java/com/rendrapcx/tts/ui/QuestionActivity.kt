package com.rendrapcx.tts.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SnapHelper
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import com.rendrapcx.tts.R
import com.rendrapcx.tts.constant.Const.BoardSet
import com.rendrapcx.tts.constant.Const.Companion.boardSet
import com.rendrapcx.tts.constant.Const.Companion.currentLevel
import com.rendrapcx.tts.constant.Const.Companion.qrAction
import com.rendrapcx.tts.constant.Const.QrAction
import com.rendrapcx.tts.databinding.ActivityQuestionBinding
import com.rendrapcx.tts.helper.Dialog
import com.rendrapcx.tts.helper.Helper
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.serializer

class QuestionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionBinding
    private var adapter = QuestionAdapter()

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.progressBar2.visibility = View.GONE

        Helper().apply { hideSystemUI() }

        initRecyclerView()

        binding.btnNewLevel.setOnClickListener() {
            boardSet = BoardSet.EDITOR_NEW
            val i = Intent(this, BoardActivity::class.java)
            startActivity(i)
        }

        binding.btnSearch.setOnClickListener(){
            val mp = MediaPlayer.create(applicationContext, R.raw.crowd_applause)
            mp.start()
            Toast.makeText(this, "${Data.listUserPreferences}", Toast.LENGTH_SHORT).show()
        }

        binding.headerPanel.apply {
            tvLabelTop.text = "Questioner"
            btnBack.setOnClickListener() {
                val i = Intent(this@QuestionActivity, MainActivity::class.java)
                startActivity(i)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }

        binding.switch1.isChecked = Data.listUserPreferences[0].showFinished
        binding.switch1.setOnClickListener(){
            lifecycleScope.launch {
                val data = binding.switch1.isChecked
                Data.listUserPreferences[0].showFinished = data
                DB.getInstance(applicationContext).userPreferences()
                    .updateShowFinished("0", data)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun initRecyclerView() {
        binding.apply {
            rcViewQuestioner.layoutManager = LinearLayoutManager(this@QuestionActivity)
            rcViewQuestioner.adapter = adapter

            Data.listLevel.clear()
            lifecycleScope.launch {
                try {
                    Data.listLevel = DB.getInstance(applicationContext).level().getAllLevel()
                        .ifEmpty { return@launch }
                } finally {
                    binding.etSearch.hint = "Data Kosong"
                }
                var temp = mutableListOf<Data.Level>()

                adapter.setListItem(Data.listLevel)
                binding.etSearch.hint = adapter.itemCount.toString()
            }

            adapter.setOnClickView { it ->
                lifecycleScope.launch {
                    boardSet = BoardSet.PLAY
                    currentLevel = it.id

                    Data.listLevel =
                        DB.getInstance(applicationContext).level().getLevel(currentLevel)
                    Data.listQuestion =
                        DB.getInstance(applicationContext).question().getQuestion(currentLevel)
                    Data.listPartial = DB.getInstance(applicationContext).partial().getPartial(
                        currentLevel
                    )

                    val i = Intent(this@QuestionActivity, BoardActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }

            adapter.setOnClickDelete {
                lifecycleScope.launch {

                    val levelId = it.id
                    binding.progressBar2.visibility = View.VISIBLE
                    lifecycleScope.launch {
                        DB.getInstance(applicationContext).level().deleteLevelById(levelId)
                    }
                    lifecycleScope.launch {
                        DB.getInstance(applicationContext).question()
                            .deleteQuestionByLevelId(levelId)
                    }
                    lifecycleScope.launch {
                        DB.getInstance(applicationContext).partial().deletePartialByLevelId(levelId)
                    }
                    lifecycleScope.launch {
                        Data.listLevel.clear()
                        Data.listLevel = DB.getInstance(applicationContext).level().getAllLevel()
                        adapter.setListItem(Data.listLevel)
                        adapter.notifyDataSetChanged()
                        binding.etSearch.hint = adapter.itemCount.toString()
                    }
                    binding.progressBar2.visibility = View.GONE
                    Snackbar.make(binding.questionLayout, "Deleted", Snackbar.LENGTH_SHORT)
                        .setAction("Undo", View.OnClickListener {
                            Toast.makeText(this@QuestionActivity, "KASIH KODE DISINI", Toast.LENGTH_SHORT).show()
                        })
                        .show()
                }
            }

            adapter.setOnClickEdit { it ->
                lifecycleScope.launch {
                    boardSet = BoardSet.EDITOR_EDIT
                    currentLevel = it.id

                    Data.listLevel =
                        DB.getInstance(applicationContext).level().getLevel(currentLevel)
                    Data.listQuestion =
                        DB.getInstance(applicationContext).question().getQuestion(currentLevel)
                    Data.listPartial = DB.getInstance(applicationContext).partial().getPartial(
                        currentLevel
                    )

                    val i = Intent(this@QuestionActivity, BoardActivity::class.java)
                    startActivity(i)
                }
            }

            adapter.setOnClickUpload {

            }

            adapter.setOnClickShare {value ->
                lifecycleScope.launch {
                    qrAction = QrAction.CREATE
                    currentLevel = value.id

                    Data.listLevel =
                        DB.getInstance(applicationContext).level().getLevel(currentLevel)
                    Data.listQuestion =
                        DB.getInstance(applicationContext).question().getQuestion(currentLevel)
                    Data.listPartial = DB.getInstance(applicationContext).partial().getPartial(
                        currentLevel
                    )

                    val a = Data.listLevel
                    val b = Data.listQuestion
                    val c = Data.listPartial
                    val d = a + "#" + b + "#" + c
                    val content = a.toString()

                    Dialog().apply { shareQRDialog(this@QuestionActivity, content) }
                }
            }

        }
    }
}

