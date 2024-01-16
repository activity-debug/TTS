package com.rendrapcx.tts.ui.trial

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Reader
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.camera.CameraConfigurationUtils
import com.rendrapcx.tts.databinding.ActivityTestBinding
import com.rendrapcx.tts.helper.Utils
import com.rendrapcx.tts.model.Data
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream


enum class RequestCode {
    WRITE_EXTERNAL_STORAGE_PERMISSION_CODE,
    READ_EXTERNAL_STORAGE_PERMISSION_CODE,
    CAMERA_PERMISSION_CODE,
}

open class TestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestBinding
    private lateinit var tvResult: TextView

    private var WRITE_EXTERNAL_STORAGE_PERMISSION_CODE: Int = 1
    private var READ_EXTERNAL_STORAGE_PERMISSION_CODE: Int = 2
    private var CAMERA_PERMISSION_CODE: Int = 3
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.apply {

            btnDecodeGallery.setOnClickListener() {
                openAlbums()
            }

            btnEncode.setOnClickListener() {
                val content =  editInputContent.text.toString()
                val barcodeEncoder = BarcodeEncoder()
                val bitmap: Bitmap =
                    barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 1000, 1000)
//                saveMediaToStorage(bitmap)
                Utils().apply { saveMediaToStorage(bitmap) }
                imgCoder.setImageBitmap(bitmap)
            }

            btnDecodeFromCamera.setOnClickListener() {
                openCamera()
            }
        }

    }

   open val resultLauncherGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val imageUri = data!!.data!!
                binding.imgCoder.setImageURI(imageUri)

                val imagePath = convertMediaUriToPath(imageUri)
                val imgFile = File(imagePath)
                binding.textResultContent.text = getQRContent(imgFile)
            } else {
                Toast.makeText(this@TestActivity, "Result Not Found", Toast.LENGTH_LONG).show()
            }
        }

    private fun openAlbums() {
        val galleryIntent = Intent(Intent.ACTION_PICK, Media.INTERNAL_CONTENT_URI)
        resultLauncherGallery.launch(galleryIntent)
    }

    private fun openCamera() {
        val qrScan = IntentIntegrator(this@TestActivity)
        qrScan.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        qrScan.setPrompt("Scan a QR Code")
        qrScan.setOrientationLocked(false)
        qrScan.setBeepEnabled(true)
        qrScan.setBarcodeImageEnabled(true)
        qrScan.initiateScan()
    }

    //    @Deprecated("Deprecated in Java")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
//        if (result != null) {
//            if (result.contents == null) {
//                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show()
//            } else {
//                try {
//                    val contents = result.contents
//                    binding.textResultContent.text = contents
//                } catch (e: JSONException) {
//                    e.printStackTrace()
//                    Toast.makeText(this, result.contents, Toast.LENGTH_LONG).show()
//                }
//            }
//        } else {
//            super.onActivityResult(requestCode, resultCode, data)
//        }
//    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (intentResult != null) {
            if (intentResult.contents == null) {
                Toast.makeText(baseContext, "Cancelled", Toast.LENGTH_SHORT).show()
            } else {
                binding.textResultContent.text = intentResult.contents
                binding.editInputContent.setText(intentResult.barcodeImagePath)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun saveMediaToStorage(bitmap: Bitmap) {
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
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            outputStream = FileOutputStream(image)
        }

        outputStream?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            Toast.makeText(this, "Captured View and saved to Gallery", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getQRContent(file: File): String {
        val inputStream: InputStream = BufferedInputStream(FileInputStream(file))
        val bitmap = BitmapFactory.decodeStream(inputStream)
        return decodeQRImage(bitmap)!!
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