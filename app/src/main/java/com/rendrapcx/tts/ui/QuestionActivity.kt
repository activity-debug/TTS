package com.rendrapcx.tts.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.constant.Const.BoardSet
import com.rendrapcx.tts.constant.Const.Companion.boardSet
import com.rendrapcx.tts.constant.Const.Companion.currentLevel
import com.rendrapcx.tts.constant.Const.Companion.gameState
import com.rendrapcx.tts.constant.GameState
import com.rendrapcx.tts.databinding.ActivityQuestionBinding
import com.rendrapcx.tts.helper.Helper
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import kotlinx.coroutines.launch

class QuestionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionBinding
    private var adapter = QuestionAdapter()
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Helper().apply { hideSystemUI() }

        initRecyclerView()

        binding.apply {
            btnGoHome.setOnClickListener(){
                val i = Intent(this@QuestionActivity, MainActivity::class.java)
                startActivity(i)
            }
        }

    }

    private fun initRecyclerView() {
        binding.apply {
            rcViewQuestioner.layoutManager = LinearLayoutManager(this@QuestionActivity)
            rcViewQuestioner.adapter = adapter

            lifecycleScope.launch {
                adapter.setListItem(DB.getInstance(applicationContext).level().getAllLevel())
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

            adapter.setOnClickEdit {
                Toast.makeText(this@QuestionActivity, "EDITTTTT", Toast.LENGTH_SHORT).show()
            }

        }
    }
}

