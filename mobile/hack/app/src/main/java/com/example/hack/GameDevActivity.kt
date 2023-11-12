package com.example.hack

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.hack.data.ExcelBase
import com.example.hack.data.ModelExcel
import com.example.hack.databinding.ActivityGameDevBinding
import com.example.hack.retrofit.RetroApi
import com.example.hack.ui.model.FromVosk
import com.example.hack.ui.model.ModelForResult
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.SpeechStreamService
import org.vosk.android.StorageService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import kotlin.random.Random

class GameDevActivity : AppCompatActivity(), RecognitionListener {

    companion object {
        private const val STATE_START = 0
        private const val STATE_READY = 1
        private const val STATE_DONE = 2
        private const val STATE_MIC = 3
        private const val PERMISSIONS_REQUEST_RECORD_AUDIO = 1
    }

    private lateinit var binding: ActivityGameDevBinding
    private var model: Model? = null
    private var speechService: SpeechService? = null
    private var speechStreamService: SpeechStreamService? = null
    private lateinit var retrofit: Retrofit
    private lateinit var api: RetroApi
    private val result = mutableListOf<ModelExcel>()

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        binding = ActivityGameDevBinding.inflate(layoutInflater)
        setContentView(binding.root)
        LibVosk.setLogLevel(LogLevel.INFO)
        setUiState(STATE_START)
        retrofit = Retrofit.Builder()
            .baseUrl("https://39e7-35-227-82-26.ngrok.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(RetroApi::class.java)
        binding.ivMicro.setOnClickListener {
            recognizeMicrophone()
        }
        checkPermissionAndInitModel()
        binding.ivExit.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initModel()
            } else {
                finish()
            }
        }
    }

    private fun checkPermissionAndInitModel() {
        val permissionCheck =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSIONS_REQUEST_RECORD_AUDIO
            )
        } else {
            initModel()
        }
    }

    private fun initModel() {
        StorageService.unpack(this, "model", "model",
            { model ->
                this.model = model
                setUiState(STATE_READY)
            }
        ) { exception -> setErrorState("Failed to unpack the model" + exception.message) }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechService?.let {
            it.stop()
            it.shutdown()
        }
        speechStreamService?.stop()
    }

    override fun onResult(hypothesis: String) {
        val userAnswer = Gson().fromJson(hypothesis, FromVosk::class.java)
        CoroutineScope(Dispatchers.IO + CoroutineExceptionHandler { _, error -> Log.e("", error.message.orEmpty())}).launch {
            delay(500)
            val damage = Random.nextInt(11)
            takeDamage(damage)
            delay(500)
            runOnUiThread {
                val question = ExcelBase.randomQuestion()
                binding.tvText.text = question.question
                result.add(question)
            }
        }
        recognizeMicrophone()
    }

    private fun takeDamage(damage: Int) {
        val hpHero = binding.pbHero.progress
        val hpEnemy = binding.pbEnemy.progress
        runOnUiThread {
            if (damage < 5) {
                binding.pbHero.progress = hpHero - 5 + damage
            } else {
                binding.pbEnemy.progress = hpEnemy - damage + 5
            }
        }
        if (binding.pbHero.progress < 1 || binding.pbEnemy.progress < 1) {
            startActivity(Intent().putExtra("result", ModelForResult(gameDev = result)))
            finish()
        }
    }

    override fun onFinalResult(hypothesis: String) {
        setUiState(STATE_DONE)
        speechStreamService?.stop()
    }

    override fun onPartialResult(hypothesis: String) {}

    override fun onError(e: Exception) {}

    override fun onTimeout() {}

    private fun setUiState(state: Int) {
        when (state) {
            STATE_START -> {
                binding.tvText.setText(R.string.preparing)
                binding.tvText.movementMethod = ScrollingMovementMethod()
                binding.ivMicro.isEnabled = false
                binding.ivMicro.setImageResource(R.drawable.mic_not_ready)
            }

            STATE_READY -> {
                binding.tvText.setText(R.string.ready)
                binding.ivMicro.setImageResource(R.drawable.mic_off)
                binding.ivMicro.isEnabled = true
                runBlocking {
                    delay(500)
                    val question = ExcelBase.randomQuestion()
                    binding.tvText.text = question.question
                    result.add(question)
                }
            }

            STATE_DONE -> {
                binding.ivMicro.setImageResource(R.drawable.mic_off)
                binding.ivMicro.isEnabled = true
            }

            STATE_MIC -> {
                binding.ivMicro.setImageResource(R.drawable.mic_on)
                binding.ivMicro.isEnabled = true
            }

            else -> throw IllegalStateException("Unexpected value: $state")
        }
    }

    private fun setErrorState(message: String?) {
        binding.tvText.text = message
        binding.ivMicro.setImageResource(R.drawable.mic_not_ready)
        binding.ivMicro.isEnabled = false
    }

    private fun recognizeMicrophone() {
        if (speechService != null) {
            setUiState(STATE_DONE)
            speechService!!.stop()
            speechService = null
        } else {
            setUiState(STATE_MIC)
            try {
                val rec = Recognizer(model, 16000.0f)
                speechService = SpeechService(rec, 16000.0f)
                speechService!!.startListening(this)
            } catch (e: IOException) {
                setErrorState(e.message)
            }
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
        super.onBackPressed()
    }
}