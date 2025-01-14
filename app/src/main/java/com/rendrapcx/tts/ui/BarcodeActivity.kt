package com.rendrapcx.tts.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore.Images.Media
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.zxing.BinaryBitmap
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Reader
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.integration.android.IntentIntegrator
import com.rendrapcx.tts.R
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.constant.Const.Companion.isEditor
import com.rendrapcx.tts.constant.Const.Companion.isEnableClick
import com.rendrapcx.tts.constant.Const.Companion.koinUser
import com.rendrapcx.tts.constant.RequestCode
import com.rendrapcx.tts.databinding.ActivityBarcodeBinding
import com.rendrapcx.tts.helper.GoogleMobileAdsConsentManager
import com.rendrapcx.tts.helper.Helper
import com.rendrapcx.tts.helper.MPlayer
import com.rendrapcx.tts.helper.Sora
import com.rendrapcx.tts.helper.UserRef
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.Base64
import java.util.concurrent.atomic.AtomicBoolean


class BarcodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBarcodeBinding
    private var myClipboard: ClipboardManager? = null
    //private var myClip: ClipData? = null

    private var counterClearInput = 0

    private var qrShare = mutableListOf<Data.QRShare>()
    private var qrListLevel = mutableListOf<Data.Level>()
    private var qrListQuestion = mutableListOf<Data.Question>()

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Helper().apply { hideSystemUI() }

        binding.loading.root.visibility = View.INVISIBLE

        myClipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?

        binding.includeHeader.include.componentKoin.visibility = View.GONE
        binding.textResultContent.text = ""
        binding.editPaste.setText("")
        binding.editPaste.visibility = View.INVISIBLE
        binding.resultPanel.visibility = View.INVISIBLE
        qrShare.clear()
        qrListLevel.clear()
        qrListQuestion.clear()

        binding.includeHeader.apply {
            tvLabelTop.text = "Scan Data Questioner"
            btnBack.setOnClickListener {
                if (isEnableClick) {
                    val i = Intent(this@BarcodeActivity, MainActivity::class.java)
                    startActivity(i)
                    finish()
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                }
            }

            btnSettingPlay.setOnClickListener {
                /*ENABLE EDITOR*/
                counterClearInput++
                if (counterClearInput > 9 && !isEditor) {
                    isEditor = true
                    UserRef().setIsEditor(isEditor, applicationContext, lifecycle)
                    YoYo.with(Techniques.RubberBand).playOn(it)
                    Toast.makeText(
                        this@BarcodeActivity,
                        "Editor Aktif",
                        Toast.LENGTH_SHORT
                    ).show()
                    MPlayer().sound(applicationContext, Sora.SUCCESS)
                }
            }

            btnSettingPlay.setOnLongClickListener {
                /*RESET USER ANSWER*/
                lifecycleScope.launch {
                    isEditor = false
                    UserRef().setIsEditor(isEditor, applicationContext, lifecycle)
                    MPlayer().sound(applicationContext, Sora.SUCCESS)
                }
                return@setOnLongClickListener true
            }
        }

        binding.apply {

            imgCoder.setOnClickListener() {
                if (isEditor) {
                    koinUser += 10000
                    UserRef().setKoin(koinUser, applicationContext, lifecycle)
                    MPlayer().sound(applicationContext, Sora.SUCCESS)
                }
            }

            btnDecodeGallery.setOnClickListener {
                openAlbums()
            }

            btnDecodeFromCamera.setOnClickListener {
                openCamera()
            }

            btnSaveSoal.setOnClickListener {
                if (isEnableClick){
                    isEnableClick = false
                    loading.tvLoadingInfo.text = "Saving"
                    loading.root.visibility = View.VISIBLE
                    lifecycleScope.launch {
                        val job = async {
                            saveQRToDB()
                        }
                        job.await()
                    }
                    YoYo.with(Techniques.Bounce).repeat(5).duration(1000).playOn(binding.resultPanel)
                    YoYo.with(Techniques.RubberBand).repeat(5).duration(1000).playOn(binding.resultPanel)
                    YoYo.with(Techniques.Swing).repeat(5).duration(1000).playOn(loading.tvLoadingInfo)
                    YoYo.with(Techniques.Wobble).repeat(5).duration(1000).playOn(loading.tvLoadingInfo)
                    YoYo.with(Techniques.RubberBand).repeat(5).duration(1000)
                        .onEnd {
                            MPlayer().sound(applicationContext, Sora.DING)
                            isEnableClick = true
                            loading.root.visibility = View.INVISIBLE
                            resultPanel.visibility = View.INVISIBLE
                        }
                        .playOn(loading.tvLoadingInfo)
                }

            }

            btnPasteSoal.setOnClickListener {
                var error = false
                try {
                    val abc = myClipboard?.primaryClip
                    val item = abc?.getItemAt(0)

                    binding.editPaste.setText("")
                    binding.editPaste.setText(item?.text.toString())

                    val decodeString =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            String(Base64.getDecoder().decode(binding.editPaste.text.toString()))
                        } else {
                            android.util.Base64.decode(binding.editPaste.text.toString(), android.util.Base64.DEFAULT).toString()
                        }

                    qrShare.clear()

                    qrShare = Json.decodeFromString<MutableList<Data.QRShare>>(decodeString)
                    qrListLevel = qrShare[0].level
                    qrListQuestion = qrShare[0].question

                } catch (e: Exception) {
                    error = true
                    Toast.makeText(this@BarcodeActivity, "${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    if (error) {
                        Toast.makeText(
                            applicationContext,
                            "Bukan data soal Terka TTS",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        binding.resultPanel.visibility = View.VISIBLE
                        binding.textResultContent.text =
                            "ID: ${qrListLevel[0].id} \n" +
                                    "Category: ${qrListLevel[0].category} \n" +
                                    "Title: ${qrListLevel[0].title} \n" +
                                    "Creator: ${qrListLevel[0].userId}"
                    }
                }
            }

        }

    }

    private fun saveQRToDB() {
        lifecycleScope.launch {
            val id = qrListLevel[0].id
            var levelId = ""
            val data = DB.getInstance(applicationContext).level().getAllLevel()
            val ids = data.map { it.id }
            val newId: Boolean

            if (id in ids) {
                newId = true
                levelId = Helper().generateLevelId(ids.size)
            } else {
                newId = false
                levelId = id
            }

            val category = if (binding.editInputContent.text.isNotEmpty()) {
                binding.editInputContent.text.toString().trimEnd().trimStart()
            } else {
                qrListLevel[0].category
            }

            DB.getInstance(applicationContext).level().insertLevel(
                Data.Level(
                    id = levelId,
                    category = category,
                    title = qrListLevel[0].title,
                    userId = qrListLevel[0].userId,
                    status = Const.FilterStatus.POST
                )
            )
            //Add Questioner
            qrListQuestion.filter { it.levelId == id }.map { it }.forEach {
                DB.getInstance(applicationContext).question().insertQuestion(
                    Data.Question(
                        levelId = levelId, //if (newId) levelId else it.id,
                        id = if (newId) "${levelId}-${it.direction}-${Helper().formatQuestionId(it.number + 1)}" else it.id,
                        number = it.number,
                        direction = it.direction,
                        asking = it.asking,
                        answer = it.answer,
                        slot = it.slot
                    )
                )
            }
        }
    }


    /* GET QR RESULT GALLERY */
    @SuppressLint("SetTextI18n")
    private val resultLauncherGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val imageUri = data!!.data!!
                binding.imgCoder.setImageURI(imageUri)

                val imagePath = convertMediaUriToPath(imageUri)
                val imgFile = File(imagePath)
                val dt = getQRContent(imgFile)
                val decodeString = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String(Base64.getDecoder().decode(dt))
                } else {
                    android.util.Base64.decode(dt, android.util.Base64.DEFAULT).toString()
                }

                qrShare.clear()
                qrShare = Json.decodeFromString<MutableList<Data.QRShare>>(decodeString)

                qrListLevel = qrShare[0].level
                qrListQuestion = qrShare[0].question

                binding.textResultContent.text = "file: ${imgFile} \n" +
                        "ID: ${qrListLevel[0].id} \n" +
                        "Category: ${qrListLevel[0].category} \n" +
                        "Title: ${qrListLevel[0].title} \n" +
                        "Creator: ${qrListLevel[0].userId}"

                binding.resultPanel.visibility = View.VISIBLE
            } else {
                Toast.makeText(this@BarcodeActivity, "Result Not Found", Toast.LENGTH_LONG).show()
            }
        }

    private fun openAlbums() {
        val galleryIntent = Intent(Intent.ACTION_PICK, Media.INTERNAL_CONTENT_URI)
        resultLauncherGallery.launch(galleryIntent)
    }

    private fun openCamera() {
        val qrScan = IntentIntegrator(this@BarcodeActivity)
        qrScan.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        qrScan.setPrompt("Scan a QR Code")
        qrScan.setOrientationLocked(false)
        qrScan.setBeepEnabled(true)
        qrScan.setBarcodeImageEnabled(true)
        qrScan.initiateScan()
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (intentResult != null) {
            if (intentResult.contents == null) {
                Toast.makeText(baseContext, "Cancelled", Toast.LENGTH_SHORT).show()
            } else {
                val imagePath = intentResult.barcodeImagePath
                binding.imgCoder.setImageURI(imagePath.toUri())

                val dt = intentResult.contents //getQRContent(imgFile)
                val decodeString = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String(Base64.getDecoder().decode(dt))
                } else {
                    android.util.Base64.decode(dt, android.util.Base64.DEFAULT).toString()
                }

                qrShare.clear()
                qrShare = Json.decodeFromString<MutableList<Data.QRShare>>(decodeString)

                qrListLevel = qrShare[0].level
                qrListQuestion = qrShare[0].question

                binding.textResultContent.text = "file: ${intentResult.barcodeImagePath} \n" +
                        "ID: ${qrListLevel[0].id} \n" +
                        "Category: ${qrListLevel[0].category} \n" +
                        "Title: ${qrListLevel[0].title} \n" +
                        "Creator: ${qrListLevel[0].userId}"

                binding.resultPanel.visibility = View.VISIBLE
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun getQRContent(file: File): String {
        val inputStream: InputStream = BufferedInputStream(FileInputStream(file))
        val bitmap = BitmapFactory.decodeStream(inputStream)
        return decodeQRImage(bitmap)!!
    }

    private fun convertMediaUriToPath(uri: Uri): String {
        val proj = arrayOf<String>(Media.DATA)
        val cursor = contentResolver.query(uri, proj, null, null, null)
        val columnIndex = cursor!!.getColumnIndexOrThrow(Media.DATA)
        cursor.moveToFirst()
        val path = cursor.getString(columnIndex)
        cursor.close()
        return path
    }

    private fun decodeQRImage(bMap: Bitmap): String? {
        var contents: String? = null
        val intArray = IntArray(bMap.width * bMap.height)
        bMap.getPixels(intArray, 0, bMap.width, 0, 0, bMap.width, bMap.height)
        val source: LuminanceSource = RGBLuminanceSource(bMap.width, bMap.height, intArray)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        val reader: Reader = MultiFormatReader()
        try {
            val result = reader.decode(bitmap)
            contents = result.text

        } catch (e: Exception) {
            Log.e("QrTest", "Error decoding qr code", e)
            Toast.makeText(
                this,
                "Error decoding QR Code, Silakan pilih gambar QR Code yang benar!",
                Toast.LENGTH_SHORT
            ).show()
        }
        return contents
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RequestCode.WRITE_EXTERNAL_STORAGE_PERMISSION_CODE.ordinal -> if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(
                        this,
                        "Anda perlu memberikan semua izin untuk menggunakan aplikasi ini.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

            RequestCode.READ_EXTERNAL_STORAGE_PERMISSION_CODE.ordinal -> if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(
                        this,
                        "Anda perlu memberikan semua izin untuk menggunakan aplikasi ini.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

            RequestCode.CAMERA_PERMISSION_CODE.ordinal -> if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(
                        this,
                        "Anda perlu memberikan semua izin untuk menggunakan aplikasi ini.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

}