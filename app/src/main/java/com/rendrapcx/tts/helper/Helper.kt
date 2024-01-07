package com.rendrapcx.tts.helper

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.rendrapcx.tts.databinding.CustomDialog1Binding

class Helper {
    fun Context.showToast(message: String? = null, short: Boolean = true) {
        Toast.makeText(
            this,
            message,
            when (short) {
                true -> Toast.LENGTH_SHORT
                else -> Toast.LENGTH_SHORT
            }
        ).show()
    }

    fun alertDialog(context: Context, msg: String, title: String = "Information") {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage(msg)
            .setTitle(title)
            .setNegativeButton("Negative") { dialog, which ->
//                showToast("you select negatif")
            }
            .setItems(arrayOf("Item One", "Item Two", "Item Three")) { _, _ ->
//                showToast("You select on items")
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    fun inputLevel(context: Context, title: String = "Title", msg: String = "Message") {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        val input = EditText(context)
        input.requestFocus()
        builder
            .setCancelable(false)
            .setTitle(title)
            .setMessage(msg)
            .setView(input)
            .setPositiveButton("OK",
                DialogInterface.OnClickListener { dialog, whichButton ->
//                    val value: Editable = input.text
//                    binding.container2.inc tvLevelId1.text = value
                }).setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, whichButton ->
                    dialog.dismiss()
                }).show()
    }

}