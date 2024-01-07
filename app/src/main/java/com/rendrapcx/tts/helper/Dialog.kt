package com.rendrapcx.tts.helper

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.rendrapcx.tts.databinding.CustomDialog1Binding

open class Dialog {
    fun Context.exitDialog(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = CustomDialog1Binding.inflate(inflater)
        val builder = AlertDialog.Builder(context).setView(binding.root)
        val dialog = builder.create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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