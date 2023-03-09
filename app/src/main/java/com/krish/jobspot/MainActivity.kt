package com.krish.jobspot

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.krish.jobspot.auth.AuthActivity
import com.krish.jobspot.databinding.ActivityMainBinding
import com.krish.jobspot.home.activity.HomeActivity


class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private val mAuth by lazy { FirebaseAuth.getInstance() }
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        val headingText = SpannableString(getString(R.string.on_boarding_heading))
        val color = getColor(R.color.on_boarding_span_text_color)
        val headingColor = ForegroundColorSpan(color)
        headingText.setSpan(UnderlineSpan(), 10, 20, 0)
        headingText.setSpan(headingColor, 10, 20, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        binding.onBoardingHeading.text = headingText

        binding.ivFab.setOnClickListener {
            if(mAuth.currentUser != null ){
                val homeActivity = Intent(this, HomeActivity::class.java)
                startActivity(homeActivity)
            } else {
                val authActivity = Intent(this, AuthActivity::class.java)
                startActivity(authActivity)
            }
            finish()
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}