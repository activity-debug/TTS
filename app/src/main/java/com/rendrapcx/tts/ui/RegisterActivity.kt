package com.rendrapcx.tts.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.rendrapcx.tts.R
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.databinding.ActivityRegisterBinding
import com.rendrapcx.tts.helper.Helper
import kotlinx.coroutines.launch
import java.util.UUID

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Helper().apply { hideSystemUI() }

        tabLoginView()


        binding.apply {
            btnSignIn.setOnClickListener() {
                gotoMain()
            }
            btnSignAsGuest.setOnClickListener() {
                // TODO: Generate Guest User dulu
                gotoMain()
            }
        }

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

}