package com.rendrapcx.tts

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.rendrapcx.tts.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setAnu()
        binding.apply {
            button.setOnClickListener() {
                val i = Intent(this@MainActivity, CreatorActivity::class.java)
                startActivity(i)
            }

            button2.setOnClickListener() {
                button3.visibility = View.VISIBLE
                it.visibility = View.GONE
                setAnu()
            }
            button3.setOnClickListener() {
                button2.visibility = View.VISIBLE
                it.visibility = View.GONE
                setAnu()
            }
        }
    }

    private fun setAnu() {
        binding.apply {
            button.height = button.width
            button2.height = button2.width
            button3.height = button3.width
        }
    }
}