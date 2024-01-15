package com.rendrapcx.tts.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.databinding.ActivityMainBinding
import com.rendrapcx.tts.helper.Dialog
import com.rendrapcx.tts.helper.Helper
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.model.Data.Companion.listLevel
import com.rendrapcx.tts.ui.dlg.playMenu
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        installSplashScreen()

        Helper().apply { hideSystemUI() }

        getData()

        binding.apply {
            btnGoListQuestion.setOnClickListener() {
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
            btnGoTTS.setOnClickListener(){
                playMenu(this@MainActivity, lifecycle)
//                Dialog().apply { playMenu(this@MainActivity, lifecycle) }
//                val i = Intent(this@MainActivity, PlayMenuActivity::class.java)
//                startActivity(i)
            }
        }
    }

    fun getData(){
        lifecycleScope.launch {
            try {
                listLevel = DB.getInstance(applicationContext).level().getAllLevel()
                    .ifEmpty { return@launch }
            } finally {
                binding.btnLogin.text = "EMPTY"
            }
        }
    }

}