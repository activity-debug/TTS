package com.rendrapcx.tts.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.InputFilter
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.constant.Direction
import com.rendrapcx.tts.constant.InputMode
import com.rendrapcx.tts.databinding.ActivityBoardBinding
import com.rendrapcx.tts.databinding.CustomDialog1Binding
import com.rendrapcx.tts.databinding.DialogInputSoalBinding
import com.rendrapcx.tts.databinding.DialogLoginBinding
import com.rendrapcx.tts.databinding.DialogSettingBinding
import com.rendrapcx.tts.databinding.DialogUserProfileBinding
import com.rendrapcx.tts.databinding.DialogWinBinding
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.ui.QuestionActivity
import kotlinx.coroutines.launch
import java.util.UUID


open class Dialog {
    @RequiresApi(Build.VERSION_CODES.R)
    fun Context.exitDialog(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = CustomDialog1Binding.inflate(inflater)
        val builder = AlertDialog.Builder(context).setView(binding.root)
        val dialog = builder.create()

        val window = dialog.window

        val windowInsetsController =
            WindowCompat.getInsetsController(window!!, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
            if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
                || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())) {
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            }
            view.onApplyWindowInsets(windowInsets)
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        binding.btnOK.setOnClickListener() {
            Toast.makeText(this, "OKEH BYE!!", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        binding.btnCancel.setOnClickListener() {
            Toast.makeText(this, "Nah gotu donk", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun Context.winDialog(
        context: Context,
        boardBinding: ActivityBoardBinding
    ) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = DialogWinBinding.inflate(inflater)
        val builder = AlertDialog.Builder(context).setView(binding.root)
        val dialog = builder.create()

        val window = dialog.window

        val windowInsetsController =
            WindowCompat.getInsetsController(window!!, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
            if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
                || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())) {
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            }
            view.onApplyWindowInsets(windowInsets)
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        binding.btnNext.setOnClickListener() {
            Toast.makeText(context.applicationContext, "NEXT", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        binding.btnBack.setOnClickListener() {
            val i = Intent(context.applicationContext, QuestionActivity::class.java)
            startActivity(i)
            dialog.dismiss()
        }

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun Context.settingDialog(
        context: Context,
    ) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = DialogSettingBinding.inflate(inflater)
        val builder = AlertDialog.Builder(context).setView(binding.root)
        val dialog = builder.create()

        val window = dialog.window

        val windowInsetsController =
            WindowCompat.getInsetsController(window!!, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
            if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
                || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())) {
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            }
            view.onApplyWindowInsets(windowInsets)
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        binding.btnSettingClose.setOnClickListener() {
            dialog.dismiss()
        }

        dialog.show()
    }



    @RequiresApi(Build.VERSION_CODES.R)
    fun Context.userProfile(
        context: Context
    ) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = DialogUserProfileBinding.inflate(inflater)
        val builder = AlertDialog.Builder(context).setView(binding.root)
        val dialog = builder.create()

        val window = dialog.window

        val windowInsetsController =
            WindowCompat.getInsetsController(window!!, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
            if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
                || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())) {
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            }
            view.onApplyWindowInsets(windowInsets)
        }

        dialog.window?.attributes?.gravity = Gravity.BOTTOM
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        binding.btnSaveProgress.setOnClickListener() {
            Toast.makeText(this, "Save Progress", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun Context.loginDialog(
        context: Context,
    ) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = DialogLoginBinding.inflate(inflater)
        val builder = AlertDialog.Builder(context).setView(binding.root)
        val dialog = builder.create()
        val window = dialog.window

        val windowInsetsController =
            WindowCompat.getInsetsController(window!!, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
            if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
                || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())) {
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            }
            view.onApplyWindowInsets(windowInsets)
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        binding.btnLogin.setOnClickListener() {
            dialog.dismiss()
        }

        binding.btnLoginGuest.setOnClickListener() {
            dialog.dismiss()
        }

        dialog.show()
    }

}