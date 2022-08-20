package com.krish.jobspot

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.krish.jobspot.auth.AuthActivity
import com.krish.jobspot.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        val headingText = SpannableString(getString(R.string.on_boarding_heading))
        val color = getColor(R.color.on_boarding_span_text_color)
        val headingColor = ForegroundColorSpan(color)
        headingText.setSpan(UnderlineSpan(), 10, 20, 0)
        headingText.setSpan(headingColor, 10, 20, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        binding.onBoardingHeading.text = headingText

        binding.ivFab.setOnClickListener {
            val authActivity = Intent(this, AuthActivity::class.java)
            startActivity(authActivity)
            finish()
        }
    }
}