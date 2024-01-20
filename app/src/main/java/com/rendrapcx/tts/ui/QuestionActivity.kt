package com.rendrapcx.tts.ui

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.rendrapcx.tts.R
import com.rendrapcx.tts.R.drawable.*
import com.rendrapcx.tts.constant.Const.BoardSet
import com.rendrapcx.tts.constant.Const.Companion.boardSet
import com.rendrapcx.tts.constant.Const.Companion.currentLevel
import com.rendrapcx.tts.databinding.ActivityQuestionBinding
import com.rendrapcx.tts.databinding.DialogInputTbkBinding
import com.rendrapcx.tts.databinding.DialogSelectInputBinding
import com.rendrapcx.tts.databinding.DialogShareQrcodeBinding
import com.rendrapcx.tts.helper.Helper
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.model.Data.Companion.listLevel
import com.rendrapcx.tts.model.Data.Companion.listTebakKata
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.UUID

class QuestionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionBinding
    private var questionAdapter = QuestionAdapter()
    private var tebakKataAdapter = TebakKataAdapter()

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Helper().apply { hideSystemUI() }

        binding.apply {
            rcViewQuestioner.layoutManager = LinearLayoutManager(this@QuestionActivity)
            rcViewQuestioner.adapter = questionAdapter
        }

        /* Adapter Data and Actions */
        questionAdapterActions()

        binding.btnNewLevel.setOnClickListener() {
            createSoalDialog(this)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filter(s.toString())
            }
        })


        binding.headerPanel.apply {
            tvLabelTop.text = "Questioner"
            btnBack.setOnClickListener() {
                val i = Intent(this@QuestionActivity, MainActivity::class.java)
                startActivity(i)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }

        binding.switch1.isChecked = Data.listUserPreferences[0].showFinished
        binding.switch1.setOnClickListener() {
            lifecycleScope.launch {
                val data = binding.switch1.isChecked
                Data.listUserPreferences[0].showFinished = data
                DB.getInstance(applicationContext).userPreferences()
                    .updateShowFinished("0", data)
            }
        }

        /*Tabs Switch*/
        binding.apply {
            tabTts.setOnClickListener() {
                activeTab(0)
                questionAdapterActions()
                binding.rcViewQuestioner.adapter = questionAdapter
            }
            tabTbk.setOnClickListener() {
                activeTab(1)
                tebakKataAdapterActions()
                binding.rcViewQuestioner.adapter = tebakKataAdapter
            }
            tabWiw.setOnClickListener() {
                activeTab(2)
            }
        }
    }

    private fun activeTab(tab: Int = 0) {
        binding.apply {
            when (tab) {
                0 -> {
                    tabTts.setBackgroundResource(tabs_active_shape)
                    tabTbk.setBackgroundResource(tabs_disable_shape)
                    tabWiw.setBackgroundResource(tabs_disable_shape)
                }

                1 -> {
                    tabTts.setBackgroundResource(tabs_disable_shape)
                    tabTbk.setBackgroundResource(tabs_active_shape)
                    tabWiw.setBackgroundResource(tabs_disable_shape)
                }

                2 -> {
                    tabTts.setBackgroundResource(tabs_disable_shape)
                    tabTbk.setBackgroundResource(tabs_disable_shape)
                    tabWiw.setBackgroundResource(tabs_active_shape)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun tebakKataAdapterActions() {
        binding.apply {
            listTebakKata.clear()
            lifecycleScope.launch {
                try {
                    listTebakKata = DB.getInstance(applicationContext).tebakKata().getAllTbk()
                        .ifEmpty { return@launch }
                } finally {
                    etSearch.hint = "Data Kosong"
                }
                tebakKataAdapter.setListItem(listTebakKata)
                etSearch.hint = tebakKataAdapter.itemCount.toString()
            }

            tebakKataAdapter.setOnClickShare {
                val string =
                    it.id + ";" + it.imageUrl + ";" + it.answer + ";" + it.hint1 + ";" + it.hint2 + ";" + it.hint3 + ";" + it.hint4 + ";" + it.hint5
                shareQRDialog(this@QuestionActivity, string)
            }

            tebakKataAdapter.setOnClickDelete {tbk->
                lifecycleScope.launch {
                    val id = tbk.id
                    lifecycleScope.launch {
                        DB.getInstance(applicationContext).tebakKata().deleteTbkById(id)
                    }
                    lifecycleScope.launch {
                        listTebakKata.clear()
                        listTebakKata = DB.getInstance(applicationContext).tebakKata().getAllTbk()
                        tebakKataAdapter.setListItem(listTebakKata)
                        tebakKataAdapter.notifyDataSetChanged()
                        binding.etSearch.hint = tebakKataAdapter.itemCount.toString()
                    }
                    Snackbar.make(binding.questionLayout, "Tbk Deleted", Snackbar.LENGTH_SHORT)
                        .setAction("Undo", View.OnClickListener {
                            Toast.makeText(
                                this@QuestionActivity,
                                "KASIH KODE DISINI",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                        .show()
                }
            }

        }
    }


    @RequiresApi(Build.VERSION_CODES.R)
    private fun shareQRDialog(context: Context, content: String) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = DialogShareQrcodeBinding.inflate(inflater)
        val builder = AlertDialog.Builder(context).setView(binding.root)
        val dialog = builder.create()

        extracted(dialog)

        dialog.window!!.attributes.windowAnimations = R.style.DialogTopAnim
        dialog.window!!.attributes.gravity = Gravity.TOP
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        val barcodeEncoder = BarcodeEncoder()
        val bitmap: Bitmap =
            barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 1000, 1000)
        binding.imgQR.setImageBitmap(bitmap)

        binding.btnShareQr.setOnClickListener() {
            saveAndShareQRTBK(content)
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun saveAndShareQRTBK(content: String) {
        val barcodeEncoder = BarcodeEncoder()
        val bitmap: Bitmap =
            barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 1000, 1000)

        val filename = "${System.currentTimeMillis()}.png"
        var outputStream: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                outputStream = imageUri?.let { resolver.openOutputStream(it) }

                val shareIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    type = "image/png"
                }
                startActivity(Intent.createChooser(shareIntent, null))
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            outputStream = FileOutputStream(image)
        }

        outputStream?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            Helper().alertDialog(this, "Captured View and saved to Gallery")
        }

    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun createSoalDialog(context: Context) {
        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val bind = DialogSelectInputBinding.inflate(inflater)
        val builder = AlertDialog.Builder(context).setView(bind.root)
        val dialog = builder.create()

        extracted(dialog)

        dialog.window!!.attributes.windowAnimations = R.style.DialogTopAnim
        dialog.window!!.attributes.gravity = Gravity.TOP
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        bind.btnCreateTBK.setOnClickListener() {
            dialogInputTbk(this)
            dialog.dismiss()
        }

        bind.btnCreateTTS.setOnClickListener() {
            boardSet = BoardSet.EDITOR_NEW
            val i = Intent(this, BoardActivity::class.java)
            startActivity(i)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        bind.btnCreateWiw.setOnClickListener() {
            Toast.makeText(this, "Bikin Soal WIW", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun dialogInputTbk(context: Context) {
        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val bind = DialogInputTbkBinding.inflate(inflater)
        val builder = AlertDialog.Builder(context).setView(bind.root)
        val dialog = builder.create()

        extracted(dialog)

        dialog.window!!.attributes.windowAnimations = R.style.DialogTopAnim
        dialog.window!!.attributes.gravity = Gravity.TOP
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        bind.etId.isEnabled = false
        bind.etId.setText(UUID.randomUUID().toString())
        bind.etImgUrl.isEnabled = false
        bind.tvPreview.text = ""
        bind.etAnswer.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(45))

        @SuppressLint("SetTextI18n")
        fun previewAnswer(string: String) {
            val xLen = 15
            val yLen = 3
            val size = 45
            val rightMargin = arrayListOf<Int>()
            for (i in 0 until yLen) {
                rightMargin.add((i * xLen) - 1)
            }
            var pre = ""
            for (i in 0 until size) {
                if (i in rightMargin) {
                    if (i >= string.length) {
                        pre += "_"
                        continue
                    } else if (string[i] == ' ') pre += "_" + "\n"
                    else pre += string[i] + "\n"
                } else {
                    if (i >= string.length) {
                        pre += "_"
                        continue
                    } else if (string[i] == ' ') pre += "_"
                    else pre += string[i]
                }
            }
            bind.tvPreview.text = pre
        }

        bind.etAnswer.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                previewAnswer(s.toString())
            }

        })

        bind.btnCancelAddTbk.setOnClickListener() {
            dialog.dismiss()
        }

        bind.btnSaveTbk.setOnClickListener() {
            lifecycleScope.launch {
                DB.getInstance(applicationContext).tebakKata().insertTbk(
                    Data.TebakKata(
                        id = bind.etId.text.toString(),
                        imageUrl = bind.etImgUrl.text.toString(),
                        answer = bind.etAnswer.text.toString(),
                        hint1 = bind.etHint1.text.toString(),
                        hint2 = bind.etHint2.text.toString(),
                        hint3 = bind.etHint3.text.toString(),
                        hint4 = bind.etHint4.text.toString(),
                        hint5 = bind.etHint5.text.toString(),
                    )
                )
                dialog.dismiss()
            }
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

    /* searching filter category */
    private fun filter(str: String) {
        if (listLevel.isEmpty()) return
        val listLevelFilter = listLevel
        val result = listLevelFilter.filter { it.category.contains(str) }.toMutableList()

        if (result.isEmpty()) {
            questionAdapter.setListItem(listLevel)
        } else {
            questionAdapter.setListItem(result)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun questionAdapterActions() {
        binding.apply {
            listLevel.clear()
            lifecycleScope.launch {
                try {
                    listLevel = DB.getInstance(applicationContext).level().getAllLevel()
                        .ifEmpty { return@launch }
                } finally {
                    binding.etSearch.hint = "Data Kosong"
                }
                questionAdapter.setListItem(listLevel)
                binding.etSearch.hint = questionAdapter.itemCount.toString()
            }

            questionAdapter.setOnClickView { it ->
                lifecycleScope.launch {
                    boardSet = BoardSet.PLAY
                    currentLevel = it.id

                    listLevel =
                        DB.getInstance(applicationContext).level().getLevel(currentLevel)
                    Data.listQuestion =
                        DB.getInstance(applicationContext).question().getQuestion(currentLevel)
                    Data.listPartial = DB.getInstance(applicationContext).partial().getPartial(
                        currentLevel
                    )

                    val i = Intent(this@QuestionActivity, BoardActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }

            questionAdapter.setOnClickDelete {
                lifecycleScope.launch {

                    val levelId = it.id
                    lifecycleScope.launch {
                        DB.getInstance(applicationContext).level().deleteLevelById(levelId)
                    }
                    lifecycleScope.launch {
                        DB.getInstance(applicationContext).question()
                            .deleteQuestionByLevelId(levelId)
                    }
                    lifecycleScope.launch {
                        DB.getInstance(applicationContext).partial().deletePartialByLevelId(levelId)
                    }
                    lifecycleScope.launch {
                        listLevel.clear()
                        listLevel = DB.getInstance(applicationContext).level().getAllLevel()
                        questionAdapter.setListItem(listLevel)
                        questionAdapter.notifyDataSetChanged()
                        binding.etSearch.hint = questionAdapter.itemCount.toString()
                    }
                    Snackbar.make(binding.questionLayout, "Deleted", Snackbar.LENGTH_SHORT)
                        .setAction("Undo", View.OnClickListener {
                            Toast.makeText(
                                this@QuestionActivity,
                                "KASIH KODE DISINI",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                        .show()
                }
            }

            questionAdapter.setOnClickEdit { it ->
                lifecycleScope.launch {
                    boardSet = BoardSet.EDITOR_EDIT
                    currentLevel = it.id

                    listLevel =
                        DB.getInstance(applicationContext).level().getLevel(currentLevel)
                    Data.listQuestion =
                        DB.getInstance(applicationContext).question().getQuestion(currentLevel)
                    Data.listPartial = DB.getInstance(applicationContext).partial().getPartial(
                        currentLevel
                    )

                    val i = Intent(this@QuestionActivity, BoardActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }

        }
    }
}

