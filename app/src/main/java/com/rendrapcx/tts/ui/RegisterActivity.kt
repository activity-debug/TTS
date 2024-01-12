package com.rendrapcx.tts.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.rendrapcx.tts.R
import com.rendrapcx.tts.databinding.ActivityRegisterBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class TabReg { MASUK, REGISTER, INFORMATION }
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private val login_username = stringPreferencesKey("login_username")
    private val login_password = stringPreferencesKey("login_password")
    private val isRemember = booleanPreferencesKey("isRemember")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tabLoginView()
        loadUserPref()



        binding.apply {
            tabLogin.setOnClickListener() {
                tabLoginView()
            }
            tabRegister.setOnClickListener() {
                tabRegisterView()
            }
            tabInfo.setOnClickListener() {
                tabInformationView()
            }
        }

        binding.apply {
            lifecycleScope.launch {
                applicationContext.dataStore.edit { settings ->
                    settings[login_username] = binding.edUIDLogin.text.toString()
                    settings[login_password] = binding.edPasswordLogin.text.toString()
                }
            }
        }

        binding.apply {
            btnSignIn.setOnClickListener() {
                // TODO: Cek dulu datanya di DB 
                saveUserLogin()
                gotoMain()
            }
            btnSignAsGuest.setOnClickListener() {
                // TODO: Generate Guest User dulu 
                saveUserLogin()
                gotoMain()
            }
        }

    }

    private fun loadUserPref() {
        lifecycleScope.launch {
            val data = applicationContext.dataStore.data.first()
            binding.edUIDLogin.setText(data[login_username].toString())
            binding.edPasswordLogin.setText(data[login_password].toString())
        }
    }

    private fun tabInformationView() {
        binding.apply {
            tabLogin.setTextColor(getColor(this@RegisterActivity, R.color.black))
            tabLogin.setBackgroundColor(getColor(this@RegisterActivity, R.color.white))
            tabRegister.setTextColor(ContextCompat.getColor(this@RegisterActivity, R.color.black))
            tabRegister.setBackgroundColor(getColor(this@RegisterActivity, R.color.white))
            tabInfo.setTextColor(
                ContextCompat.getColor(
                    this@RegisterActivity,
                    R.color.button2_text
                )
            )
            tabInfo.setBackgroundColor(
                ContextCompat.getColor(
                    this@RegisterActivity,
                    R.color.button2_color
                )
            )
            binding.containerLogin.visibility = View.GONE
            binding.containerRegister.visibility = View.GONE
            binding.containerInformation.visibility = View.VISIBLE
        }
    }

    private fun tabRegisterView() {
        binding.apply {
            tabLogin.setTextColor(getColor(this@RegisterActivity, R.color.black))
            tabLogin.setBackgroundColor(getColor(this@RegisterActivity, R.color.white))
            tabRegister.setTextColor(getColor(this@RegisterActivity, R.color.button2_text))
            tabRegister.setBackgroundColor(getColor(this@RegisterActivity, R.color.button2_color))
            tabInfo.setTextColor(getColor(this@RegisterActivity, R.color.black))
            tabInfo.setBackgroundColor(getColor(this@RegisterActivity, R.color.white))
            binding.containerLogin.visibility = View.GONE
            binding.containerRegister.visibility = View.VISIBLE
            binding.containerInformation.visibility = View.GONE
        }
    }

    private fun tabLoginView() {
        binding.apply {
            tabLogin.setTextColor(getColor(this@RegisterActivity, R.color.button2_text))
            tabLogin.setBackgroundColor(getColor(this@RegisterActivity, R.color.button2_color))
            tabRegister.setTextColor(getColor(this@RegisterActivity, R.color.black))
            tabRegister.setBackgroundColor(getColor(this@RegisterActivity, R.color.white))
            tabInfo.setTextColor(getColor(this@RegisterActivity, R.color.black))
            tabInfo.setBackgroundColor(getColor(this@RegisterActivity, R.color.white))
            binding.containerLogin.visibility = View.VISIBLE
            binding.containerRegister.visibility = View.GONE
            binding.containerInformation.visibility = View.GONE
        }
    }

    private fun gotoMain() {
        val i = Intent(this@RegisterActivity, MainActivity::class.java)
        startActivity(i)
        finish()
    }

    private fun saveUserLogin() {
        // TODO: SAVE JUGA KE DATABASE
        binding.apply {
            lifecycleScope.launch {
                applicationContext.dataStore.edit { settings ->
                    settings[login_username] = binding.edUIDLogin.text.toString()
                    settings[login_password] = binding.edPasswordLogin.text.toString()
                    settings[isRemember] = binding.ckRememberMe.isChecked
                }
            }
        }
    }
}