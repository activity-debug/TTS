package com.rendrapcx.tts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

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

    fun Context.showSoftKeyboard(window: Window, view: View) {
        if (view.requestFocus()) {
            WindowCompat.getInsetsController(window, view).show(
                WindowInsetsCompat.Type.ime()
            )
        }
    }

    fun Context.alertDialog(context: Context, msg: String, title: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage(msg)
            .setTitle(title)

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    fun Context.exitDialog(){
        val builder = AlertDialog.Builder(this)
        val customView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_1, null)
        builder.setView(customView)
        val dialog = builder.create()
        dialog.setCancelable(false)
        //dialog.window?.setBackgroundDrawable(ColorDrawable(Color.DKGRAY))

        val btnOk = customView.findViewById<Button>(R.id.btnOK)
        val btnCancel = customView.findViewById<Button>(R.id.btnCancel)

        btnOk.setOnClickListener(){
            Toast.makeText(this,"OKEH BYE!!", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener(){
            Toast.makeText(this,"Nah gotu donk", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        dialog.show()
    }

}