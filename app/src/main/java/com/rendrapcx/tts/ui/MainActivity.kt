package com.rendrapcx.tts.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.rendrapcx.tts.constant.Const.BoardSet
import com.rendrapcx.tts.constant.Const.Companion.boardSet
import com.rendrapcx.tts.databinding.ActivityMainBinding
import com.rendrapcx.tts.helper.Dialog
import com.rendrapcx.tts.helper.Helper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private val login_username = stringPreferencesKey("login_username")
    private val login_password = stringPreferencesKey("login_password")
    private val isRemember = booleanPreferencesKey("isRemember")

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        installSplashScreen()

        validateLogin()

        Helper().apply { hideSystemUI() }

        binding.apply {
            btnGoCreator.setOnClickListener() {
                boardSet = BoardSet.EDITOR
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
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun validateLogin() {
        lifecycleScope.launch {
            val data = applicationContext.dataStore.data.first()
            val username = data[login_username]!!
            val password = data[login_password]!!
            val isRemember = data[isRemember]!!

            if (username.isEmpty() && password.isEmpty() && isRemember == false) {
                val intent = Intent(this@MainActivity, RegisterActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun isLogin() {
        lifecycleScope.launch {
            val data = applicationContext.dataStore.data.first()
            val user = data[login_username]!!
            val pass = data[login_password]!!
            if (user.isEmpty() && pass.isEmpty()) {
                // TODO:
            }
        }
    }

//    @RequiresApi(Build.VERSION_CODES.R)
//    private fun hideSystemUI() {
//        val windowInsetsController =
//            WindowCompat.getInsetsController(window, window.decorView)
//        windowInsetsController.systemBarsBehavior =
//            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//
//        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
//            if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
//                || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())) {
//                    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
//            }
//            view.onApplyWindowInsets(windowInsets)
//        }
//    }

}