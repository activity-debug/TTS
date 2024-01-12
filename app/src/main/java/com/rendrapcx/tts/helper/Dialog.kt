package com.rendrapcx.tts.helper

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.rendrapcx.tts.databinding.ActivityBoardBinding
import com.rendrapcx.tts.databinding.CustomDialog1Binding
import com.rendrapcx.tts.databinding.DialogSettingBinding
import com.rendrapcx.tts.databinding.DialogWinBinding
import com.rendrapcx.tts.ui.QuestionActivity

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

    fun Context.winDialog(
        context: Context,
        boardBinding: ActivityBoardBinding
    ) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = DialogWinBinding.inflate(inflater)
        val builder = AlertDialog.Builder(context).setView(binding.root)
        val dialog = builder.create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        binding.btnNext.setOnClickListener(){
            Toast.makeText(context.applicationContext, "NEXT", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        binding.btnBack.setOnClickListener(){
            val i = Intent(context.applicationContext, QuestionActivity::class.java)
            startActivity(i)
            dialog.dismiss()
        }

        dialog.show()
    }

    fun Context.settingDialog(
        context: Context,
    ) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = DialogSettingBinding.inflate(inflater)
        val builder = AlertDialog.Builder(context).setView(binding.root)
        val dialog = builder.create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        binding.btnSettingClose.setOnClickListener(){
            dialog.dismiss()
        }

        dialog.show()
    }
}