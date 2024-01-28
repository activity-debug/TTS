package com.rendrapcx.tts.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
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
import androidx.lifecycle.coroutineScope
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.rendrapcx.tts.R
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.constant.Const.Companion.currentUser
import com.rendrapcx.tts.databinding.ActivityBoardBinding
import com.rendrapcx.tts.databinding.DialogAboutBinding
import com.rendrapcx.tts.databinding.DialogInputDescriptionBinding
import com.rendrapcx.tts.databinding.DialogSettingBinding
import com.rendrapcx.tts.databinding.DialogUserProfileBinding
import com.rendrapcx.tts.databinding.DialogYesNoBinding
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.model.Data.Companion.listUser
import com.rendrapcx.tts.ui.BoardActivity
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

        YoYo.with(Techniques.FlipInY).duration(3000).playOn(binding.textView19)


        YoYo.with(Techniques.RubberBand).repeat(5).playOn(binding.btnHireMe)


        binding.btnHireMe.setOnClickListener() {
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
            dialog.dismiss()
        }

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun settingDialog(
        context: Context,
        lifecycle : Lifecycle
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

        binding.btnSettingDisableAds.visibility = View.INVISIBLE

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

            var i = 0
            var isEditor = UserRef().getIsEditor()
            if (isEditor) binding.btnEditorShow.setBackgroundResource(R.drawable.button_image_enable)
            else binding.btnEditorShow.setBackgroundResource(R.drawable.button_image_disable)
            binding.btnEditorShow.setOnClickListener() {
                ++i
                if (i in 7..9) {
                    Toast.makeText(context, "${10 - i} kali lagi", Toast.LENGTH_SHORT).show()
                }
                if (i > 9) {
                    isEditor = true
                }
                if (isEditor) {
                    binding.btnEditorShow.setBackgroundResource(R.drawable.button_image_enable)
                    UserRef().setIsEditor("0", true, context, lifecycle)
                }
                isEditor = UserRef().getIsEditor()
                Sound().soundClickSetting(context)
                YoYo.with(Techniques.RubberBand).playOn(it)
            }
            binding.btnEditorShow.setOnLongClickListener() {
                isEditor = false
                binding.btnEditorShow.setBackgroundResource(R.drawable.button_image_disable)
                UserRef().setIsEditor("0", false, context, lifecycle)
                return@setOnLongClickListener true
            }

            binding.btnSettingDisableAds.setOnClickListener() {
                lifecycle.coroutineScope.launch {
                    DB.getInstance(context.applicationContext).user().insertUser(
                        Data.User(
                            id = "Admin2024",
                            username = "Andra",
                            password = "bismillah",
                            isGuest = false
                        ),
                    )
                }
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

        fun activeTabs(int: Int) {
            when (int) {
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
            textUserId.text = listUser[currentUser].id
            textUsername.text = listUser[currentUser].username
        }

        //Actions
        binding.btnSaveProgress.setOnClickListener() {
            Toast.makeText(context, "Save Progress", Toast.LENGTH_SHORT).show()
        }

        binding.btnCloseProfile.setOnClickListener() {
            dialog.dismiss()
        }

        binding.btnTabTTS.setOnClickListener() {
            activeTabs(0)
        }

        binding.btnTabTBK.setOnClickListener() {
            activeTabs(1)
        }

        binding.btnTabWIW.setOnClickListener() {
            activeTabs(2)
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
    fun Activity.showDialogYesNo(
        title: String,
        msg: String,
        btnOneTitle: String = "Tidak",
        btnTwoTitle: String = "Ya",
        lifecycle: Lifecycle
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

        binding.btnOne.setOnClickListener(){
            dialog.dismiss()
        }

        binding.btnTwo.setOnClickListener(){
            Progress().updateUserAnswer(Const.AnswerStatus.PROGRESS, this, lifecycle)
            Const.progress = Progress().getUserProgress(this, lifecycle)
            val i = Intent(this, BoardActivity::class.java)
            startActivity(i)
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
