package com.rendrapcx.tts.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.rendrapcx.tts.databinding.ActivityMainBinding
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.viewmodel.VMDB
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var vmdb: VMDB
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val dao = DB.getInstance(application).level()
//        val factory = VMDBFactory(dao)
//        vmdb = ViewModelProvider(this, factory).get(VMDB::class.java)

        binding.apply {
            button2.setOnClickListener() {
//                vmdb.insertLevel(
//                    Data.Level("satu", "Testing", "10x10")
//                )
                lifecycleScope.launch {
                    DB.getInstance(applicationContext).level().insertLevel(
                       Data.Level("dua", "Testing", "10x10")
                    )
                }
            }
            button3.setOnClickListener() {
//                vmdb.level.observe(this@MainActivity, Observer {
//                    textView3.text = it.toString()
//                })
                DB.getInstance(applicationContext).level().getAllLevel().observe(this@MainActivity, Observer {
                    textView3.text = it.toString()
                })
            }
        }




        binding.apply {
            button.setOnClickListener() {
                val i = Intent(this@MainActivity, CreatorActivity::class.java)
                startActivity(i)
            }
        }
    }

}