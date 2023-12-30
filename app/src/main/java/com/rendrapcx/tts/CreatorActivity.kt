package com.rendrapcx.tts

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rendrapcx.tts.databinding.ActivityCreatorBinding
import com.rendrapcx.tts.databinding.DialogInputSoalBinding


class CreatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatorBinding
    private var vm = MainViewModel()


    val size = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vm = ViewModelProvider(this)[MainViewModel::class.java]

        initBoxArray()
        vm.clearBoxViewValue()
        vm.selectedValue.observe(this, Observer {
            vm.boxView[vm.position].text = it
        })

        setColor(0)

        binding.container1.board10X10.apply {
            for (i in 0 until size) {
                vm.boxView[i].setOnClickListener() {
                    setColor(3)
                    vm.position = i
                    if (binding.container2.switch1.isChecked) Helper().apply {showSoftKeyboard(window, vm.boxView[i]) }
                    setColor(1)
                }
            }
        }

        binding.container2.apply {
            tvLevelId1.setOnClickListener(){
                inputLevel(this@CreatorActivity, "....")
            }
            tvSoalId1.setOnClickListener() {
                inputSoal(this@CreatorActivity)
            }

            tvDirection1.setOnClickListener(){
                dialogSelectDirection(this@CreatorActivity)
            }
        }


    } //create


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode in 29..54) {
            val s = event?.displayLabel
            vm.currentValue(s.toString().uppercase())
//            if (vm.moveTo == "RIGHT") selectBlok(vm.next())
//            if (vm.moveTo == "DOWN") selectBlok(vm.down())
        } else {
            return false
        }

        return super.onKeyDown(keyCode, event)
    }

    private fun initBoxArray() {
        for (i in 0 until size) {
            val child = binding.container1.board10X10.getChildAt(i)
            if (child is TextView) vm.boxView.add(child)
        }
    }

    private fun inputLevel(context: Context, msg: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        val input = EditText(this)
        input.requestFocus()
        builder
            .setCancelable(false)
            .setTitle("Update ID Level")
            .setMessage(msg)
            .setView(input)
            .setPositiveButton("Ok",
                DialogInterface.OnClickListener { dialog, whichButton ->
                    val value: Editable = input.text
                    binding.container2.tvLevelId1.text = value
                }).setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, whichButton ->
                    dialog.dismiss()
                }).show()
    }
    private fun inputSoal(context: Context){
//        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val bind = DialogInputSoalBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(context).setView(bind.root)
        val dialog = builder.create()

        dialog.setCancelable(false)

        bind.etIDSoal11.requestFocus()

        bind.btnOK11.setOnClickListener() {
            binding.container2.tvSoalId1.text = bind.etIDSoal11.text
            binding.container2.tvTanya1.text = bind.etTanya11.text
            binding.container2.tvJawab1.text = bind.etJawab11.text
            dialog.dismiss()
        }

        bind.btnCancel11.setOnClickListener() {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setColor(set: Int){
        val disable = ContextCompat.getColor(this, R.color.disable)
        val active = ContextCompat.getColor(this, R.color.active)
        val selected = ContextCompat.getColor(this, R.color.selected)
        if (set == 0) {
            for (i in 0 until vm.boxView.size) vm.boxView[i].setBackgroundColor(disable)
        } else if (set == 1) {
            vm.boxView[vm.position].setBackgroundColor(selected)
        } else if (set == 3){
            for (i in 0 until vm.boxView.size) {
                if (vm.boxView[i].text.isNotEmpty()) vm.boxView[i].setBackgroundColor(active)
                else vm.boxView[i].setBackgroundColor(disable)
            }
        }
    }


    private fun dialogSelectDirection(context: Context) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setTitle("Select Direction")
            .setPositiveButton("OK") { dialog, which ->
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .setSingleChoiceItems(
                arrayOf("Mendatar", "Menurun", "Keduanya"), 0
            ) { dialog, which ->
                binding.container2.tvDirection1.text = which.toString()
            }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

} //end