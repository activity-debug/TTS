package com.rendrapcx.tts.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
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
import com.rendrapcx.tts.model.Data.Companion.listQuestion
import com.rendrapcx.tts.model.Data.Companion.qrShare
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Base64


class QuestionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionBinding
    private var questionAdapter = QuestionAdapter()
    private var levelShareIndexId = -1
    private var myClipboard: ClipboardManager? = null
    //private var myClip: ClipData? = null

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Helper().apply { hideSystemUI() }

        myClipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?;

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
            finish()
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
                finish()
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


    @SuppressLint("SetTextI18n")
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

        binding.tvWarningInfo.visibility = View.INVISIBLE
        var error = false
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap =
                barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 5000, 5000)
            binding.imgQR.setImageBitmap(bitmap)
        } catch (e: Exception) {
            error = true
            binding.tvWarningInfo.text = "Text terlalu panjang (${content.count()}) " +
                    "QR Image tidak bisa di generate. \n" +
                    "kirim soal terinkripsi"
            Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
        }
        finally {
            if (error) {
                binding.btnShareQr.text = "Share As Text"
                binding.tvWarningInfo.visibility = View.VISIBLE
            } else {
                binding.btnShareQr.text = "Share"
                binding.tvWarningInfo.visibility = View.INVISIBLE
            }
        }


        binding.btnShareQr.setOnClickListener() {
            if (!error) {
                saveAndShareQRTBK(content)
            } else {
                val i = Intent()
                i.action = Intent.ACTION_SEND
                i.type = "text/plain"
                i.putExtra(Intent.EXTRA_TEXT, content)
                context.startActivity(Intent.createChooser(i, "Please select App"))
            }
            dialog.dismiss()
        }

        dialog.show()
    }


    @RequiresApi(Build.VERSION_CODES.R)
    private fun saveAndShareQRTBK(content: String) {
        val barcodeEncoder = BarcodeEncoder()
        val bitmap: Bitmap =
            barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 1000, 1000)
        val x = levelShareIndexId
        val lvl = listLevel[x].category.trim().uppercase()
        //val tit = listLevel[x].title.trim().uppercase()
        val index = Helper().format(x)
        val filename = "${lvl}-${index}-${System.currentTimeMillis()}.png"
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

            /*DISABLE DULU INI YAH BIAR GAK PLAY DARI EDITOR, KEBANYAKAN*/
//            questionAdapter.setOnClickView { it ->
//                lifecycleScope.launch {
//                    boardSet = BoardSet.PLAY
//                    currentLevel = it.id
//
//                    listLevel =
//                        DB.getInstance(applicationContext).level().getLevel(currentLevel)
//                    listQuestion =
//                        DB.getInstance(applicationContext).question().getQuestion(currentLevel)
//
//                    val i = Intent(this@QuestionActivity, BoardActivity::class.java)
//                    startActivity(i)
//                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
//                }
//            }

            questionAdapter.setOnClickDelete {
                lifecycleScope.launch {

                    val levelId = it.id
                    val job1 = async {
                        DB.getInstance(applicationContext).level().deleteLevelById(levelId)
                    }
                    job1.await()
                    val job2 = async {
                        DB.getInstance(applicationContext).question()
                            .deleteQuestionByLevelId(levelId)
                    }
                    job2.await()

                    val job3 = async {
                        listLevel.clear()
                        listLevel = DB.getInstance(applicationContext).level().getAllLevel()
                    }
                    job3.await()

                    questionAdapter.setListItem(listLevel)
                    questionAdapter.notifyDataSetChanged()
                    binding.etSearch.hint = questionAdapter.itemCount.toString()

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
                    listQuestion =
                        DB.getInstance(applicationContext).question().getQuestion(currentLevel)

                    val i = Intent(this@QuestionActivity, BoardActivity::class.java)
                    startActivity(i)
                    finish()
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

            questionAdapter.setOnClickShare { lvl ->
                lifecycleScope.launch {
                    val job = async {
                        qrShare.clear()
                        val level = listLevel.filter { it.id == lvl.id }.toMutableList()
                        val question =
                            DB.getInstance(applicationContext).question().getQuestion(lvl.id)
                        qrShare.add(
                            Data.QRShare(level, question)
                        )
                        levelShareIndexId = listLevel.indexOfFirst { it.id == lvl.id }
                    }
                    job.await()
                    //Dialog().showDialog(this@QuestionActivity, "${qrShare}")
                    var encodeString = ""
                    val job2 = async {
                        val json = Json.encodeToString(qrShare)

                        encodeString = Base64.getEncoder().encodeToString(json.toByteArray())
                    }
                    job2.await()
//                    Toast.makeText(this@QuestionActivity, "${encodeString.count()}", Toast.LENGTH_SHORT).show()
//                    val myClip = ClipData.newPlainText("encodeString", encodeString);
                    //myClipboard?.setPrimaryClip(myClip);
                    shareQRDialog(this@QuestionActivity, encodeString)
                }

            }

        }
    }
}

