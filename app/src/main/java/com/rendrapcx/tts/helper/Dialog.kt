package com.rendrapcx.tts.helper

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.databinding.ActivityBoardBinding
import com.rendrapcx.tts.databinding.CustomDialog1Binding
import com.rendrapcx.tts.databinding.DialogInputDescriptionBinding
import com.rendrapcx.tts.databinding.DialogLoginBinding
import com.rendrapcx.tts.databinding.DialogSettingBinding
import com.rendrapcx.tts.databinding.DialogUserProfileBinding
import com.rendrapcx.tts.databinding.DialogWinBinding
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.ui.QuestionActivity


open class Dialog {
    @RequiresApi(Build.VERSION_CODES.R)
    fun Context.exitDialog(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = CustomDialog1Binding.inflate(inflater)
        val builder = AlertDialog.Builder(context).setView(binding.root)
        val dialog = builder.create()

        extracted(dialog)

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

        extracted(dialog)

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

        extracted(dialog)

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

        extracted(dialog)

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
        extracted(dialog)

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

    @RequiresApi(Build.VERSION_CODES.R)
    fun Context.inputDescription(boardBinding: ActivityBoardBinding) {
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = DialogInputDescriptionBinding.inflate(inflater)
        val builder = AlertDialog.Builder(this).setView(binding.root)
        val dialog = builder.create()
        extracted(dialog)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        Data.listLevel.filter { it.id == Const.currentLevel }.forEach() {
            binding.editLevelId.setText(it.id)
            binding.editCategory.setText(it.category)
            binding.editTitle.setText(it.title)
            binding.editCreator.setText(it.userId)
        }

        fun updateList() {
            Data.listLevel.filter { it.id == Const.currentLevel }.map { it }.forEach() {
                boardBinding.apply {
                    it.id = binding.editLevelId.text.toString()
                    it.category = binding.editCategory.text.toString()
                    it.title = binding.editTitle.text.toString()
                    it.userId = binding.editCreator.text.toString()
                }
            }
        }

        fun fillText() {
            Data.listLevel.filter { it.id == Const.currentLevel }.forEach() {
                boardBinding.includeEditor.apply {
                    textLevelId.text = it.id
                    textCategory.text = it.category
                    textTitle.text = it.title
                    textCreator.text = it.userId
                }
            }
        }

        binding.btnSubmit.setOnClickListener() {
            updateList()
            fillText()
            dialog.dismiss()
        }

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun extracted(dialog: AlertDialog) {
        val window = dialog.window

        val windowInsetsController =
            WindowCompat.getInsetsController(window!!, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
            if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
                || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())
            ) {
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            }
            view.onApplyWindowInsets(windowInsets)
        }
    }

}