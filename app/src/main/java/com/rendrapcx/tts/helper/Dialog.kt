package com.rendrapcx.tts.helper

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.InputFilter
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.rendrapcx.tts.R
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.constant.Const.Companion.currentUser
import com.rendrapcx.tts.databinding.ActivityBoardBinding
import com.rendrapcx.tts.databinding.DialogInputDescriptionBinding
import com.rendrapcx.tts.databinding.DialogSettingBinding
import com.rendrapcx.tts.databinding.DialogUserProfileBinding
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.model.Data.Companion.listUser


open class Dialog {

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

        UserRef().checkUserPref(context, lifecycle)

        var isSound = UserRef().getIsSound()
        if (isSound) binding.imgBtnSound.setBackgroundResource(R.drawable.button_image_enable)
        else binding.imgBtnSound.setBackgroundResource(R.drawable.button_image_disable)
        binding.imgBtnSound.setOnClickListener() {
            if (isSound) {
                binding.imgBtnSound.setBackgroundResource(R.drawable.button_image_disable)
                UserRef().setIsSound("0", false, context, lifecycle)
            } else {
                binding.imgBtnSound.setBackgroundResource(R.drawable.button_image_enable)
                UserRef().setIsSound("0", true, context, lifecycle)
            }
            isSound = UserRef().getIsSound()
            Sound().soundClickSetting(context)
            YoYo.with(Techniques.Bounce).playOn(it)
        }

        binding.swSettingKeyboard.isChecked = UserRef().getIntKey()
        binding.swSettingKeyboard.setOnClickListener() {
            UserRef().setIntKey("0", binding.swSettingKeyboard.isChecked, context, lifecycle)
            Sound().soundClickSetting(context)
            YoYo.with(Techniques.Wave).playOn(it)
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

        fun activeTabs(int: Int){
            when(int){
                0 -> {
                    binding.btnTabTTS.setBackgroundResource(R.drawable.tabs_active_shape)
                    binding.btnTabTBK.setBackgroundResource(R.drawable.tabs_disable_shape)
                    binding.btnTabWIW.setBackgroundResource(R.drawable.tabs_disable_shape)
                }
                1 -> {
                    binding.btnTabTTS.setBackgroundResource(R.drawable.tabs_disable_shape)
                    binding.btnTabTBK.setBackgroundResource(R.drawable.tabs_active_shape)
                    binding.btnTabWIW.setBackgroundResource(R.drawable.tabs_disable_shape)
                }
                2 -> {
                    binding.btnTabTTS.setBackgroundResource(R.drawable.tabs_disable_shape)
                    binding.btnTabTBK.setBackgroundResource(R.drawable.tabs_disable_shape)
                    binding.btnTabWIW.setBackgroundResource(R.drawable.tabs_active_shape)
                }
            }
        }

        // Init
        activeTabs(0)

        binding.apply {
            textUserId.text = listUser[0].id
            textUsername.text = listUser[0].username
        }

        //Actions
        binding.btnSaveProgress.setOnClickListener() {
            Toast.makeText(context, "Save Progress", Toast.LENGTH_SHORT).show()
        }

        binding.btnCloseProfile.setOnClickListener() {
            dialog.dismiss()
        }

        binding.btnTabTTS.setOnClickListener(){
            activeTabs(0)
        }

        binding.btnTabTBK.setOnClickListener(){
            activeTabs(1)
        }

        binding.btnTabWIW.setOnClickListener(){
            activeTabs(2)
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

        dialog.window!!.attributes.windowAnimations = R.style.DialogTopAnim
        dialog.window!!.attributes.gravity = Gravity.TOP
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        /* INIT KOMPONEN */
        binding.apply {
            editCategory.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(50))
            editTitle.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(50))
            editCreator.setText(currentUser)
        }

        Data.listLevel.filter { it.id == Const.currentLevel }.forEach() {
            binding.editCategory.setText(it.category)
            binding.editTitle.setText(it.title)
            binding.editCreator.setText(it.userId)
        }

        fun updateList() {
            Data.listLevel.filter { it.id == Const.currentLevel }.map { it }.forEach() {
                boardBinding.apply {
                    it.category = binding.editCategory.text.toString()
                    it.title = binding.editTitle.text.toString()
                    it.userId = binding.editCreator.text.toString()
                }
            }
        }

        fun fillText() {
            Data.listLevel.filter { it.id == Const.currentLevel }.forEach() {
                boardBinding.includeEditor.apply {
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
    fun showDialog(context: Context, msg: String, title: String = "Information") {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage(msg)
            .setTitle(title)

        val dialog: AlertDialog = builder.create()
        extracted(dialog)

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
