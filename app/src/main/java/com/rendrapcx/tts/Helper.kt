package com.rendrapcx.tts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
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

    fun showSoftKeyboard(window: Window, view: View) {
        WindowCompat.getInsetsController(window, view).show(
            WindowInsetsCompat.Type.ime()
        )
    }

    fun alertDialog(context: Context, msg: String, title: String = "Information") {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage(msg)
            .setTitle(title)

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    fun Context.exitDialog(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = CustomDialog1Binding.inflate(inflater)
        val builder = AlertDialog.Builder(context).setView(binding.root)
        val dialog = builder.create()

        dialog.setCancelable(false)

        binding.btnOK.setOnClickListener() {
            Toast.makeText(this, "OKEH BYE!!", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        binding.btnCancel.setOnClickListener() {
            Toast.makeText(this, "Nah gotu donk", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        dialog.show()
    }

}