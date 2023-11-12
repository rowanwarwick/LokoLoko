package com.example.hack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat.startActivity
import com.example.hack.data.ExcelBase
import com.example.hack.databinding.ActivityTraditionTestBinding
import com.example.hack.ui.model.ModelForResult
import com.example.hack.ui.model.Question
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale

class TraditionTestActivity: AppCompatActivity() {

    private lateinit var binding: ActivityTraditionTestBinding
    private var timer: CountDownTimer? = null
    private val listQuestion = mutableListOf<Question>()
    private var currentNumberQuestion: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTraditionTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTimer()
        startSession()
        timer?.start()
        binding.ivExit.setOnClickListener {
            onBackPressed()
        }
        binding.bA.setOnClickListener {
            clickVariant(it as AppCompatButton, 1)
        }
        binding.bB.setOnClickListener {
            clickVariant(it as AppCompatButton, 2)
        }
        binding.bC.setOnClickListener {
            clickVariant(it as AppCompatButton, 3)
        }
        binding.bD.setOnClickListener {
            clickVariant(it as AppCompatButton, 4)
        }
        binding.ivNext.setOnClickListener {
            currentNumberQuestion = validNumberQuestion(++currentNumberQuestion)
            showStatusButton()
        }
        binding.ivPrev.setOnClickListener {
            currentNumberQuestion = validNumberQuestion(--currentNumberQuestion)
            showStatusButton()
        }
        binding.ivRestart.setOnClickListener {
            listQuestion.map {
                it.userVariant = null
                it.isCheck = null
            }
            currentNumberQuestion = 0
            listQuestion.clear()
            startSession()
        }
    }

    private fun setTimer() {
        timer = object : CountDownTimer(150000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hour = (millisUntilFinished / 1000) / 3600
                val minute = ((millisUntilFinished / 1000) % 3600) / 60
                val second = (millisUntilFinished / 1000) % 60
                val time = String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second)
                binding.tvTimer.text = time
            }

            override fun onFinish() {
                binding.tvTimer.text = getString(R.string.end_time)
            }
        }
    }

    private fun clickVariant(view: AppCompatButton, variant: Int) {
        if (listQuestion[currentNumberQuestion].isCheck == null) {
            if (setCheckVariant(variant)) {
                view.background = AppCompatResources.getDrawable(this, R.drawable.back_for_right_variant)
            } else {
                view.background = AppCompatResources.getDrawable(this, R.drawable.back_for_wrong_variant)
            }
        }
        CoroutineScope(Dispatchers.Default).launch {
            delay(500)
            view.background = AppCompatResources.getDrawable(this@TraditionTestActivity, R.drawable.back_for_button)
            runOnUiThread {
                if (listQuestion[currentNumberQuestion].isCheck == null) {
                    setQuestion()
                } else {
                    showStatusButton()
                }
            }
            if (listQuestion.all { it.isCheck != null }) {
                startActivity(Intent().putExtra("result", ModelForResult(test = listQuestion)))
                finish()
            }
        }
    }

    private fun setCheckVariant(variant: Int): Boolean {
        var result = false
        if (variant == listQuestion[currentNumberQuestion].rightVariant) result = true
        listQuestion[currentNumberQuestion].isCheck = result
        listQuestion[currentNumberQuestion].userVariant = variant
        currentNumberQuestion = validNumberQuestion(++currentNumberQuestion)
        return result
    }

    private fun validNumberQuestion(number: Int) = number.coerceIn(0, 14)

    private fun showStatusButton() {
        setQuestion()
        resetButton()
        if (listQuestion[currentNumberQuestion].isCheck != null) {
            setCheckButton(listQuestion[currentNumberQuestion])
            activateButtonVariants(false)
        } else {
            activateButtonVariants(true)
        }
    }

    private fun resetButton() {
        binding.bA.background = AppCompatResources.getDrawable(this, R.drawable.back_for_button)
        binding.bB.background = AppCompatResources.getDrawable(this, R.drawable.back_for_button)
        binding.bC.background = AppCompatResources.getDrawable(this, R.drawable.back_for_button)
        binding.bD.background = AppCompatResources.getDrawable(this, R.drawable.back_for_button)
    }

    private fun activateButtonVariants(condition: Boolean) {
        binding.bA.isEnabled = condition
        binding.bB.isEnabled = condition
        binding.bC.isEnabled = condition
        binding.bD.isEnabled = condition
    }

    private fun setCheckButton(question: Question) {
        val back = if (question.userVariant == question.rightVariant) {
            AppCompatResources.getDrawable(this, R.drawable.back_for_right_variant)
        } else {
            AppCompatResources.getDrawable(this, R.drawable.back_for_wrong_variant)
        }
        when (question.userVariant) {
            1 -> binding.bA.background = back
            2 -> binding.bB.background = back
            3 -> binding.bC.background = back
            else -> binding.bD.background = back
        }
    }

    private fun startSession() {
        listQuestion.addAll(ExcelBase.generate())
        setQuestion()
    }

    private fun setQuestion() {
        val question = listQuestion[currentNumberQuestion]
        binding.tvQuestion.text = "${currentNumberQuestion + 1}) " + question.text
        binding.bA.text = question.one
        binding.bB.text = question.two
        binding.bC.text = question.three
        binding.bD.text = question.four
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
        super.onBackPressed()
    }
}