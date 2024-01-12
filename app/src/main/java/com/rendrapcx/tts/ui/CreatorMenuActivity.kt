package com.rendrapcx.tts.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.rendrapcx.tts.R
import com.rendrapcx.tts.databinding.ActivityCreatorMenuBinding

class CreatorMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreatorMenuBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatorMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}