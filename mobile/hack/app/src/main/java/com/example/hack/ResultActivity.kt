package com.example.hack

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.hack.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        adaptorCar = ResultRecyclerAdaptor(false, this)
//        adaptorMan = ResultRecyclerAdaptor(true, this)
//        binding.recycler.adapter = adaptorMan
//        binding.recycler.adapter = adaptorCar
    }
}