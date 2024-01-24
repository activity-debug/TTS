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
import com.rendrapcx.tts.constant.Const.BoardSet
import com.rendrapcx.tts.constant.Const.Companion.boardSet
import com.rendrapcx.tts.constant.Const.Companion.currentLevel
import com.rendrapcx.tts.constant.Const.FilterStatus
import com.rendrapcx.tts.databinding.ActivityQuestionBinding
import com.rendrapcx.tts.databinding.DialogShareQrcodeBinding
import com.rendrapcx.tts.helper.Dialog
import com.rendrapcx.tts.helper.Helper
import com.rendrapcx.tts.helper.UserRef
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.model.Data.Companion.listLevel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class QuestionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionBinding
    private var questionAdapter = QuestionAdapter()

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Helper().apply { hideSystemUI() }

        binding.headerPanel.tvLabelTop.text = "Editor"

        lifecycleScope.launch {
            val job1 = async {
                UserRef().checkUserPref(this@QuestionActivity, lifecycle)
                Data.userPreferences =
                    DB.getInstance(applicationContext).userPreferences().getAllUserPreferences()
            }
            job1.await()
            activeFilterTab(UserRef().getActiveTabFilter())
            questionAdapterActions()
        }


        binding.apply {
            rcViewQuestioner.layoutManager = LinearLayoutManager(this@QuestionActivity)
            rcViewQuestioner.adapter = questionAdapter
        }

        binding.btnNewLevel.setOnClickListener() {
            boardSet = BoardSet.EDITOR_NEW
            val i = Intent(this, BoardActivity::class.java)
            startActivity(i)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filter(s.toString())
            }
        })


        binding.headerPanel.apply {
            btnBack.setOnClickListener() {
                val i = Intent(this@QuestionActivity, MainActivity::class.java)
                startActivity(i)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }

        binding.swAll.setOnClickListener() {
            activeFilterTab(FilterStatus.ALL)
            UserRef().setActiveTabFilter("0", FilterStatus.ALL, this, lifecycle)
        }
        binding.swDraft.setOnClickListener() {
            activeFilterTab(FilterStatus.DRAFT)
            UserRef().setActiveTabFilter("0", FilterStatus.DRAFT, this, lifecycle)
        }
        binding.swPosted.setOnClickListener() {
            activeFilterTab(FilterStatus.POST)
            UserRef().setActiveTabFilter("0", FilterStatus.POST, this, lifecycle)
        }
    }

    private fun activeFilterTab(filterStatus: FilterStatus) {
        when (filterStatus) {
            FilterStatus.ALL -> {
                binding.swAll.setBackgroundResource(R.drawable.tabs_active_shape)
                binding.swDraft.setBackgroundResource(R.drawable.tabs_disable_shape)
                binding.swPosted.setBackgroundResource(R.drawable.tabs_disable_shape)

                questionAdapter.setListItem(listLevel)
            }

            FilterStatus.DRAFT -> {
                binding.swAll.setBackgroundResource(R.drawable.tabs_disable_shape)
                binding.swDraft.setBackgroundResource(R.drawable.tabs_active_shape)
                binding.swPosted.setBackgroundResource(R.drawable.tabs_disable_shape)

                val fil = listLevel.filter { it.status == FilterStatus.DRAFT }.toMutableList()
                questionAdapter.setListItem(fil)
            }

            FilterStatus.POST -> {
                binding.swAll.setBackgroundResource(R.drawable.tabs_disable_shape)
                binding.swDraft.setBackgroundResource(R.drawable.tabs_disable_shape)
                binding.swPosted.setBackgroundResource(R.drawable.tabs_active_shape)

                val fil = listLevel.filter { it.status == FilterStatus.POST }.toMutableList()
                questionAdapter.setListItem(fil)
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


    @RequiresApi(Build.VERSION_CODES.R)
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
            Dialog().showDialog(this, "Captured View and saved to Gallery")
        }

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
        val result = listLevelFilter.filter { it.category.lowercase().contains(str.lowercase()) }
            .toMutableList()

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
                    //Data.listPartial = DB.getInstance(applicationContext).partial().getPartial(
                        //currentLevel
                    //)

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

            questionAdapter.setOnClickStatus {
                if (it.status == FilterStatus.POST) it.status = FilterStatus.DRAFT
                else it.status = FilterStatus.POST
                lifecycleScope.launch {
                    DB.getInstance(applicationContext).level().updateStatus(
                        id = it.id,
                        status = it.status.name
                    )
                    questionAdapter.notifyDataSetChanged()
                }
            }

        }
    }
}

