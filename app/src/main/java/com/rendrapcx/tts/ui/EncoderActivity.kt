@file:Suppress("PLUGIN_IS_NOT_ENABLED")

package com.rendrapcx.tts.ui

import android.os.Bundle
import android.provider.ContactsContract.Data
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.rendrapcx.tts.databinding.ActivityEncoderBinding
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data.Companion.listLevel
import com.rendrapcx.tts.model.Data.Companion.listQuestion
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Base64


@Serializable
data class Soal(
  var id: String,
  var soal: String,
  var levelId: String,
  var slot: ArrayList<Int>,
)
class EncoderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEncoderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEncoderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            listLevel = DB.getInstance(applicationContext).level().getAllLevel()
            val soal = listLevel[0].id
            listQuestion = DB.getInstance(applicationContext).question().getQuestion(soal)
        }


        binding.btnEncode11.setOnClickListener() {
            val json = Json.encodeToString(listQuestion)
            val encodeString: String = Base64.getEncoder().encodeToString(json.toByteArray())
            binding.tvData11.text = encodeString
            binding.etData.setText(listQuestion[0].asking)
        }

        binding.btnDecode11.setOnClickListener() {
            val dt = binding.tvData11.text.toString()
            val decodeString = String(Base64.getDecoder().decode(dt))
            binding.textView17.text = decodeString
            val json = Json.decodeFromString<MutableList<com.rendrapcx.tts.model.Data.Question>>(decodeString)
            binding.etData.setText(json[0].slot.toString())
            Toast.makeText(this, "${json[1]}", Toast.LENGTH_SHORT).show()
        }

        binding.button3.setOnClickListener() {

        }

        binding.button4.setOnClickListener() {

        }
    }
}

