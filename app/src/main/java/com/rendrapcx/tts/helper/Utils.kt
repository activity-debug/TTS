package com.rendrapcx.tts.helper

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class Utils {

    fun Context.getBitmapEncoder(content: String): Bitmap {
        val barcodeEncoder = BarcodeEncoder()
        return barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 512, 512)
    }

    fun Context.saveBitmapEncoder(content: String) {
        saveAndShare(getBitmapEncoder(content))
    }

    private fun Context.saveAndShare(bitmap: Bitmap, fileNameStr : String = "") {
        val filename = if (fileNameStr.isEmpty()) "${System.currentTimeMillis()}.png" else "${fileNameStr}.png"

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
            Helper().alertDialog(this, "Captured View and saved to Gallery")
        }
    }
}