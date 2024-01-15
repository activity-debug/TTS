package com.rendrapcx.tts.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.rendrapcx.tts.constant.Const.BoardSet
import com.rendrapcx.tts.constant.Const.Companion.boardSet
import com.rendrapcx.tts.constant.Const.Companion.currentLevel
import com.rendrapcx.tts.databinding.ActivityQuestionBinding
import com.rendrapcx.tts.helper.Helper
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import kotlinx.coroutines.launch

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

        binding.headerPanel.apply {
            tvLabelTop.text = "Questioner"
            btnBack.setOnClickListener() {
                val i = Intent(this@QuestionActivity, MainActivity::class.java)
                startActivity(i)
            }
        }
    }

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
                    Toast.makeText(this@QuestionActivity, "Deleted", Toast.LENGTH_SHORT).show()
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

        }
    }
}

