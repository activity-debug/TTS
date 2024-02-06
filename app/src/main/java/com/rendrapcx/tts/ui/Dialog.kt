package com.rendrapcx.tts.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.text.InputFilter
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.firebase.components.Component
import com.rendrapcx.tts.R
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.constant.Const.Companion.currentTrackOffline
import com.rendrapcx.tts.constant.Const.Companion.currentTrackOnline
import com.rendrapcx.tts.constant.Const.Companion.isOnlinePlay
import com.rendrapcx.tts.constant.Const.Companion.isPlay
import com.rendrapcx.tts.constant.Const.Companion.playTitleOffline
import com.rendrapcx.tts.constant.Const.Companion.playTitleOnline
import com.rendrapcx.tts.constant.Const.Companion.player
import com.rendrapcx.tts.databinding.ActivityBoardBinding
import com.rendrapcx.tts.databinding.ComponentLoadingBinding
import com.rendrapcx.tts.databinding.DialogAboutBinding
import com.rendrapcx.tts.databinding.DialogInputDescriptionBinding
import com.rendrapcx.tts.databinding.DialogSettingBinding
import com.rendrapcx.tts.databinding.DialogYesNoBinding
import com.rendrapcx.tts.helper.CheckNetwork
import com.rendrapcx.tts.helper.MPlayer
import com.rendrapcx.tts.helper.Sora
import com.rendrapcx.tts.helper.UserRef
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


open class Dialog {

    @RequiresApi(Build.VERSION_CODES.R)
    fun aboutDialog(
        context: Context
    ) {
        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = DialogAboutBinding.inflate(inflater)
        val builder = AlertDialog.Builder(context).setView(binding.root)
        val dialog = builder.create()

        extracted(dialog)

        dialog.window!!.attributes.windowAnimations = R.style.DialogTopAnim
        dialog.window!!.attributes.gravity = Gravity.NO_GRAVITY
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        binding.imageView4.setImageResource(R.drawable.rendrapcx3)

        YoYo.with(Techniques.Bounce).duration(1500)
            .onEnd {
                binding.imageView4.setImageResource(R.drawable.rendrapcx2)
                YoYo.with(Techniques.Wave).duration(1500)
                    .onEnd {
                        YoYo.with(Techniques.Flash).duration(1500).playOn(binding.imageView4)
                        binding.imageView4.setImageResource(R.drawable.rendrapcx)
                    }
                    .playOn(binding.imageView4)
            }
            .playOn(binding.imageView4)

        YoYo.with(Techniques.FlipInY).duration(1000)
            .onEnd { Const.isEnableClick = true }
            .playOn(binding.textView19)

        YoYo.with(Techniques.RubberBand).repeat(5).playOn(binding.btnHireMe)

        binding.btnHireMe.setOnClickListener {
            val emailIntent = Intent(
                Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "rendrapc33@gmail.com", null
                )
            )
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Terka TTS")
            emailIntent.putExtra(
                Intent.EXTRA_TEXT,
                "Hi Am a Terka TTS User, Im Interest to Hire You"
            )
            context.startActivity(Intent.createChooser(emailIntent, "Please select App"))
            Const.isEnableClick = true
            dialog.dismiss()
        }

        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.R)
    fun settingDialog(
        context: Context,
        lifecycle: Lifecycle
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

        YoYo.with(Techniques.FlipInY)
            .onEnd { Const.isEnableClick = true }
            .playOn(binding.textView3)

        lifecycle.coroutineScope.launch {
            val job1 = async {
                UserRef().checkUserPref(context, lifecycle)
                Data.userPreferences =
                    DB.getInstance(context.applicationContext).userPreferences()
                        .getAllUserPreferences()
            }
            job1.await()

            var isSound = UserRef().getIsSound()
            if (isSound) binding.imgBtnSound.setBackgroundResource(R.drawable.button_image_enable)
            else binding.imgBtnSound.setBackgroundResource(R.drawable.button_image_disable)
            binding.imgBtnSound.setOnClickListener {
                if (isSound) {
                    binding.imgBtnSound.setBackgroundResource(R.drawable.button_image_disable)
                    UserRef().setIsSound(false, context, lifecycle)
                } else {
                    binding.imgBtnSound.setBackgroundResource(R.drawable.button_image_enable)
                    UserRef().setIsSound(true, context, lifecycle)
                }
                isSound = UserRef().getIsSound()
                MPlayer().sound(context.applicationContext, Sora.SETTING)
                YoYo.with(Techniques.Bounce).playOn(it)
            }

            binding.swSettingKeyboard.isChecked = UserRef().getIntKey()
            if (binding.swSettingKeyboard.isChecked) {
                binding.swSettingKeyboard.setBackgroundResource(R.drawable.button_image_enable)
                binding.swSettingKeyboard.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                )
            } else {
                binding.swSettingKeyboard.setBackgroundResource(R.drawable.button_image_disable)
                binding.swSettingKeyboard.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.button
                    )
                )
            }
            binding.swSettingKeyboard.setOnClickListener {
                if (binding.swSettingKeyboard.isChecked) {
                    binding.swSettingKeyboard.setBackgroundResource(R.drawable.button_image_enable)
                    binding.swSettingKeyboard.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.white
                        )
                    )
                } else {
                    binding.swSettingKeyboard.setBackgroundResource(R.drawable.button_image_disable)
                    binding.swSettingKeyboard.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.button
                        )
                    )
                }
                UserRef().setIntKey("0", binding.swSettingKeyboard.isChecked, context, lifecycle)
                MPlayer().sound(context.applicationContext, Sora.SETTING)
                YoYo.with(Techniques.Wave).playOn(it)
            }


            binding.switch1.isChecked = isOnlinePlay

            val isInetConnect = CheckNetwork().isConnected(context.applicationContext)
            if (!isInetConnect && isOnlinePlay) binding.switch1.isChecked = false

            if (binding.switch1.isChecked) binding.switch1.text = "Online playlist"
            else binding.switch1.text = "Offline playlist"

            binding.switch1.setOnClickListener() {
                if (!isInetConnect && binding.switch1.isChecked) {
                    Toast.makeText(
                        context,
                        "Kamu Lagi Offline, aktifkan dulu datanya",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.switch1.isChecked = false
                }
                isOnlinePlay = binding.switch1.isChecked
                if (binding.switch1.isChecked) {
                    binding.switch1.text = "Online playlist"
                } else {
                    binding.switch1.text = "Offline playlist"
                }

                binding.tvMusikTitle.text = ""

                if (player.isPlaying) {
                    isPlay = false
                    player.stop()
                    player.reset()
                    binding.btnMusikPlay.setImageResource(R.drawable.play_solid)
                }
            }

            binding.tvMusikTitle.text = if (binding.switch1.isChecked) playTitleOnline else playTitleOffline
            if (isPlay) {
                binding.btnMusikPlay.setImageResource(R.drawable.pause_solid)
            } else {
                binding.btnMusikPlay.setImageResource(R.drawable.play_solid)
            }

            binding.btnMusikPlay.setOnClickListener() {
                if (isPlay) {
                    if (player.isPlaying) {
                        isPlay = false
                        player.stop()
                        player.reset()
                        binding.btnMusikPlay.setImageResource(R.drawable.play_solid)
                    }
                } else {
                    var err = false
                    try {
                        if (binding.switch1.isChecked) MPlayer().playNextOnline(context.applicationContext)
                        else MPlayer().playNext(context.applicationContext)
                    } catch (e: Exception) {
                        err = true
                    }
                    if (err) {
                        isPlay = false
                        binding.btnMusikPlay.setImageResource(R.drawable.play_solid)
                        binding.tvMusikTitle.text = "error"
                    } else {
                        isPlay = true
                        binding.btnMusikPlay.setImageResource(R.drawable.pause_solid)
                        binding.tvMusikTitle.text = if (binding.switch1.isChecked) {
                            playTitleOnline.ifEmpty { "loading" }
                        } else {
                            playTitleOffline.ifEmpty { "loading" }
                        }
                    }
                    MPlayer().sound(context.applicationContext, Sora.SETTING)
                    YoYo.with(Techniques.Bounce).playOn(it)
                }
            }

            binding.btnMusikNext.setOnClickListener() {
                var err = false
                try {
                    if (player.isPlaying) {
                        player.stop()
                        player.reset()
                        binding.btnMusikPlay.setImageResource(R.drawable.play_solid)
                    }
                    if (binding.switch1.isChecked) MPlayer().playNextOnline(context.applicationContext)
                    else MPlayer().playNext(context.applicationContext)
                } catch (e: Exception) {
                    err = true
                }
                if (err) {
                    isPlay = false
                    binding.btnMusikPlay.setImageResource(R.drawable.play_solid)
                    binding.tvMusikTitle.text = "error"
                } else {
                    isPlay = true
                    binding.btnMusikPlay.setImageResource(R.drawable.pause_solid)
                    binding.tvMusikTitle.text = if (binding.switch1.isChecked) {
                        playTitleOnline.ifEmpty { "loading" }
                    } else {
                        playTitleOffline.ifEmpty { "loading" }
                    }
                }
                MPlayer().sound(context.applicationContext, Sora.SETTING)
                YoYo.with(Techniques.Bounce).playOn(it)
            }

        }
        dialog.show()
    }


    @SuppressLint("SetTextI18n")
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
        }

        Data.listLevel.filter { it.id == Const.currentLevel }.forEach {
            binding.editCategory.setText(it.category)
            binding.editTitle.setText(it.title)
            binding.editCreator.setText(it.userId)
        }

        fun updateList() {
            Data.listLevel.filter { it.id == Const.currentLevel }.map { it }.forEach {
                boardBinding.apply {
                    it.category = binding.editCategory.text.toString()
                    it.title = binding.editTitle.text.toString()
                    it.userId = binding.editCreator.text.toString()
                }
            }
        }

        fun fillText() {
            Data.listLevel.filter { it.id == Const.currentLevel }.forEach {
                boardBinding.includeEditor.apply {
                    textCategory.text = it.category
                    textTitle.text = it.title
                    textCreator.text = it.userId
                }
            }
        }

        binding.btnSubmit.setOnClickListener {
            updateList()
            fillText()
            dialog.dismiss()
        }

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun Activity.showDialogYesNo(
        title: String,
        msg: String,
        btnOneTitle: String = "Batal",
        btnTwoTitle: String = "",
        action : Int = 0
    ) {
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = DialogYesNoBinding.inflate(inflater)
        val builder = AlertDialog.Builder(this).setView(binding.root)
        val dialog = builder.create()
        extracted(dialog)

        dialog.window!!.attributes.windowAnimations = R.style.DialogFadeAnim
        dialog.window!!.attributes.gravity = Gravity.NO_GRAVITY
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        binding.tvTitle.text = title
        binding.tvMessage.text = msg
        binding.btnOne.text = btnOneTitle
        binding.btnTwo.text = btnTwoTitle
        if (btnTwoTitle.isEmpty()) binding.btnTwo.visibility = View.GONE
        if (btnOneTitle.isEmpty()) binding.btnOne.visibility = View.GONE

        binding.btnOne.setOnClickListener {
            dialog.dismiss()
        }

        binding.btnTwo.setOnClickListener {
            when(action) {
                0 -> {
                    val i = Intent(this, BoardActivity::class.java)
                    startActivity(i)
                    finish()
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    dialog.dismiss()
                }
            }
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
