package com.rendrapcx.tts.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.rendrapcx.tts.constant.Const.BoardSet
import com.rendrapcx.tts.constant.Const.Companion.boardSet
import com.rendrapcx.tts.constant.Const.Companion.isSignedIn
import com.rendrapcx.tts.databinding.ActivityMainBinding
import com.rendrapcx.tts.helper.Dialog
import com.rendrapcx.tts.helper.Helper
import kotlinx.coroutines.launch
import kotlin.math.log

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        installSplashScreen()

        Helper().apply { hideSystemUI() }

        binding.apply {
            btnGoCreator.setOnClickListener() {
                boardSet = BoardSet.EDITOR_NEW
                val i = Intent(this@MainActivity, BoardActivity::class.java)
                startActivity(i)
            }
            btnGoListQuestion.setOnClickListener() {
                boardSet = BoardSet.PLAY
                val i = Intent(this@MainActivity, QuestionActivity::class.java)
                startActivity(i)
            }
            btnSettingMain.setOnClickListener() {
                Dialog().apply { settingDialog(this@MainActivity) }
            }
            btnUserSecret.setOnClickListener() {
                Dialog().apply { userProfile(this@MainActivity) }
            }
            btnLogin.setOnClickListener(){
                Dialog().apply { loginDialog(this@MainActivity) }
            }
        }
    }

}