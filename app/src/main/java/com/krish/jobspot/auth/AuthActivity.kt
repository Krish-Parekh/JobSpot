package com.krish.jobspot.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.krish.jobspot.R
import com.krish.jobspot.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}