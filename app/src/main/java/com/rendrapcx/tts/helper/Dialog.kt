package com.rendrapcx.tts.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.InputFilter
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.rendrapcx.tts.R
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.constant.Const.Companion.currentUser
import com.rendrapcx.tts.databinding.ActivityBoardBinding
import com.rendrapcx.tts.databinding.DialogExitAppBinding
import com.rendrapcx.tts.databinding.DialogInputDescriptionBinding
import com.rendrapcx.tts.databinding.DialogSettingBinding
import com.rendrapcx.tts.databinding.DialogShareQrcodeBinding
import com.rendrapcx.tts.databinding.DialogUserProfileBinding
import com.rendrapcx.tts.databinding.DialogWinBinding
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.model.Data.Companion.listUser
import com.rendrapcx.tts.model.Data.Companion.listUserPreferences
import com.rendrapcx.tts.ui.MainActivity
import kotlinx.coroutines.launch
import kotlin.system.exitProcess


open class Dialog {

    @RequiresApi(Build.VERSION_CODES.R)
    fun Context.shareQRDialog(context: Context, content: String) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = DialogShareQrcodeBinding.inflate(inflater)
        val builder = AlertDialog.Builder(context).setView(binding.root)
        val dialog = builder.create()

        extracted(dialog)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        //Show To Image
        Utils().apply {
            binding.imgQR.setImageBitmap(getBitmapEncoder(content))
        }

        binding.btnShareQr.setOnClickListener() {
            dialog.dismiss()
        }

        binding.btnSaveQr.setOnClickListener() {
            Utils().apply { saveBitmapEncoder(content) }
            dialog.dismiss()
        }

        dialog.show()
    }


    @RequiresApi(Build.VERSION_CODES.R)
    fun exitDialog(context: Context) {
        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = DialogExitAppBinding.inflate(inflater)
        val builder = AlertDialog.Builder(context).setView(binding.root)
        val dialog = builder.create()

        extracted(dialog)

        dialog.window!!.attributes.windowAnimations = R.style.DialogFadeAnim
        dialog.window!!.attributes.gravity = Gravity.NO_GRAVITY
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        binding.btnOK.setOnClickListener() {
            exitProcess(0)
        }

        binding.btnCancel.setOnClickListener() {
            dialog.dismiss()
        }

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun settingDialog(
        context: Context, lifecycle: Lifecycle
    ) {
        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = DialogSettingBinding.inflate(inflater)
        val builder = AlertDialog.Builder(context).setView(binding.root)
        val dialog = builder.create()

        extracted(dialog)

        dialog.window!!.attributes.windowAnimations = R.style.DialogTopAnim
        dialog.window!!.attributes.gravity = Gravity.TOP
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        binding.apply {
            swSettingKeyboard.isChecked = listUserPreferences[0].integratedKeyboard
        }

        binding.swSettingKeyboard.setOnClickListener() {
            lifecycle.coroutineScope.launch {
                val data = binding.swSettingKeyboard.isChecked
                listUserPreferences[0].integratedKeyboard = data
                DB.getInstance(context.applicationContext).userPreferences()
                    .updateIntegratedKeyboard("0", data)
            }
        }

        dialog.show()
    }


    @RequiresApi(Build.VERSION_CODES.R)
    fun userProfile(
        context: Context
    ) {
        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = DialogUserProfileBinding.inflate(inflater)
        val builder = AlertDialog.Builder(context).setView(binding.root)
        val dialog = builder.create()

        extracted(dialog)

        dialog.window!!.attributes.windowAnimations = R.style.DialogBottomAnim
        dialog.window!!.attributes.gravity = Gravity.BOTTOM
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        binding.apply {
            textUserId.text = listUser[0].id
            textUsername.text = listUser[0].username
        }

        binding.btnSaveProgress.setOnClickListener() {
            Toast.makeText(context, "Save Progress", Toast.LENGTH_SHORT).show()
        }

        binding.btnCloseProfile.setOnClickListener() {
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

        /* INIT KOMPONEN */
        binding.apply {
            editLevelId.isEnabled = false
            editCategory.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(50))
            editTitle.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(50))
            editCreator.setText(currentUser)
        }


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
