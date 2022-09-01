package com.krish.jobspot.user_details

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.krish.jobspot.R
import com.krish.jobspot.databinding.ActivityUserDetailBinding

class UserDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}