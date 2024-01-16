package com.rendrapcx.tts.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.zxing.BinaryBitmap
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Reader
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.integration.android.IntentIntegrator
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.databinding.ActivityMainBinding
import com.rendrapcx.tts.helper.Dialog
import com.rendrapcx.tts.helper.Helper
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.model.Data.Companion.listLevel
import com.rendrapcx.tts.ui.dlg.playMenu
import com.rendrapcx.tts.ui.trial.TestActivity
import kotlinx.coroutines.launch
import org.json.JSONException
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    private var resultQRDecoded = ""

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        installSplashScreen()

        Helper().apply { hideSystemUI() }

        getData()

        binding.apply {
            btnGoListQuestion.setOnClickListener() {
                val i = Intent(this@MainActivity, QuestionActivity::class.java)
                startActivity(i)
            }
            btnSettingMain.setOnClickListener() {
                Dialog().apply { settingDialog(this@MainActivity) }
            }
            btnUserSecret.setOnClickListener() {
                Dialog().apply { userProfile(this@MainActivity) }
            }
            btnLogin.setOnClickListener() {
                Dialog().apply { loginDialog(this@MainActivity) }
            }
            btnGoTTS.setOnClickListener() {
                playMenu(this@MainActivity, lifecycle)
            }

            btnGoWiw.setOnClickListener() {
               val intent = Intent(this@MainActivity, TestActivity::class.java)
                startActivity(intent)
            }

            btnScanQRCode.setOnClickListener(){ v ->
                openAlbums()
            }

            btnGoTBK.setOnClickListener(){
                val data = listLevel[0].toString()
                val ara = mutableListOf<Data.Level>()

                tvResult.text = data

            }
        }
    }

    fun getData() {
        lifecycleScope.launch {
            try {
                listLevel = DB.getInstance(applicationContext).level().getAllLevel()
                    .ifEmpty { return@launch }
            } finally {

            }
        }
    }

    private fun getQRContent(file: File) : String {
        val inputStream: InputStream = BufferedInputStream(FileInputStream(file))
        val bitmap = BitmapFactory.decodeStream(inputStream)
        return decodeQRImage(bitmap)!!
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
                "Error decoding QR Code, Mohon pilih gambar QR Code yang benar!",
                Toast.LENGTH_SHORT
            ).show()
        }
        return contents
    }

    private val resultLauncherGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val imageUri = data!!.data!!
                //binding.imgCoder.setImageURI(imageUri)

                val imagePath = convertMediaUriToPath(imageUri)
                val imgFile = File(imagePath)
                //binding.textResultContent.text = getQRContent(imgFile)
                resultQRDecoded = getQRContent(imgFile)
            } else {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show()
            }
        }
    private fun openAlbums() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        resultLauncherGallery.launch(galleryIntent)
    }

    private fun convertMediaUriToPath(uri: Uri): String {
        val proj = arrayOf<String>(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, proj, null, null, null)
        val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val path = cursor.getString(columnIndex)
        cursor.close()
        return path
    }

}