package com.krish.jobspot.home.activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayoutMediator
import com.krish.jobspot.R
import com.krish.jobspot.databinding.ActivityMockQuestionBinding
import com.krish.jobspot.home.adapter.MockQuestionPageAdapter
import com.krish.jobspot.home.viewmodel.MockTestViewModel
import com.krish.jobspot.home.viewmodel.QuestionPageViewModel
import com.krish.jobspot.util.UiState
import com.krish.jobspot.util.UiState.*
import kotlinx.coroutines.flow.collectLatest

import java.util.concurrent.TimeUnit


private const val TAG = "MockQuestionActivityTAG"

class MockQuestionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMockQuestionBinding
    private val mockTestViewModel: MockTestViewModel by viewModels()
    private val questionPageViewModel: QuestionPageViewModel by viewModels()
    private val args by navArgs<MockQuestionActivityArgs>()
    private val mock by lazy { args.mock }
    private var timer: CountDownTimer? = null
    private var timeRemaining: Long = 0
    private var ruleDisplayed: Boolean = false
    private var timerStarted: Boolean = false
    private var tabLayoutMediator: TabLayoutMediator? = null
    private var mockQuestionAdapter: MockQuestionPageAdapter? = null
    private var listener: ViewPager2.OnPageChangeCallback? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMockQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        timeRemaining = TimeUnit.MINUTES.toMillis(mock.duration.toLong())

        if (savedInstanceState != null) {
            timeRemaining = savedInstanceState.getLong("TIME_REMAINING")
            ruleDisplayed = savedInstanceState.getBoolean("RULE_DISPLAYED")
            timerStarted = savedInstanceState.getBoolean("TIMER_STARTED")
        }
        setupViews()
    }

    private fun setupViews() {
        binding.apply {
            ivPopOut.setOnClickListener {
                finish()
            }
            if (!ruleDisplayed) {
                showInstructionDialog()
                ruleDisplayed = true
            }
            if (timerStarted) {
                startTimer()
            }
            mockQuestionAdapter = MockQuestionPageAdapter(this@MockQuestionActivity, mock.mockQuestion)

            questionPager.adapter = mockQuestionAdapter
            questionPager.offscreenPageLimit = mock.mockQuestion.size
            tabLayoutMediator =
                TabLayoutMediator(questionCountTabLayout, questionPager) { tab, position ->
                    tab.text = getString(R.string.field_tab_text, position + 1)
                }
            tabLayoutMediator?.attach()

            for (i in 0..mock.mockQuestion.size) {
                val tabTitle = LayoutInflater.from(this@MockQuestionActivity).inflate(R.layout.tab_title, null, false) as TextView
                questionCountTabLayout.getTabAt(i)?.customView = tabTitle
            }

            listener = object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position == mock.mockQuestion.size - 1) {
                        binding.btnSubmitQuiz.visibility = View.VISIBLE
                    } else {
                        binding.btnSubmitQuiz.visibility = View.GONE
                    }
                }
            }

            binding.questionPager.registerOnPageChangeCallback(listener!!)

            btnSubmitQuiz.setOnClickListener {
                questionPageViewModel.submitQuiz(mock = mock, timeRemaining)
            }

            lifecycleScope.launchWhenCreated {
                questionPageViewModel.uiEventFlow.collectLatest { value: UiState ->
                    when (value) {
                        LOADING -> {
                            Log.d(TAG, "Loading...")
                        }
                        SUCCESS -> {
                            navigateToResultActivity()
                        }
                        FAILURE -> {
                            Log.d(TAG, "Failure...")
                        }
                    }

                }
            }
        }
    }

    private fun navigateToResultActivity() {
        val intent = Intent(this, MockResultActivity::class.java)
        intent.putExtra("MOCK_ID", mock.uid)
        startActivity(intent)
        finish()
    }

    private fun showInstructionDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheet = layoutInflater.inflate(R.layout.mock_test_rule_layout, null)
        bottomSheet.apply {
            val testName: TextView = findViewById(R.id.tvMockTestName)
            val questionCount: TextView = findViewById(R.id.tvMockTestQuestionCount)
            val testDuration: TextView = findViewById(R.id.tvMockTestDuration)
            val startTest: MaterialButton = findViewById(R.id.btnStartTest)
            testName.text = mock.title
            questionCount.text = mock.mockQuestion.size.toString()
            testDuration.text = getString(R.string.field_mock_duration, mock.duration)
            bottomSheetDialog.setCancelable(false)
            startTest.setOnClickListener {
                mockTestViewModel.updateStudentTestStatus(mock.uid)
                bottomSheetDialog.dismiss()
                startTimer()
                timerStarted = true
            }
        }
        bottomSheetDialog.setContentView(bottomSheet)
        bottomSheetDialog.show()
    }

    private fun startTimer() {
        timer = object : CountDownTimer(timeRemaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                val secondsUntilFinished = millisUntilFinished / 1000
                val minutes = secondsUntilFinished / 60
                val seconds = secondsUntilFinished % 60
                val formattedCounter = "${minutes}:${seconds.toString().padStart(2, '0')}"
                binding.tvTimer.text = formattedCounter
            }

            override fun onFinish() {
                // Write Code to submit the test
                // Remember to cancel the time on destroy
            }
        }
        timer?.start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("TIME_REMAINING", timeRemaining)
        outState.putBoolean("RULE_DISPLAYED", ruleDisplayed)
        outState.putBoolean("TIMER_STARTED", timerStarted)
    }

    override fun onDestroy() {
        if (listener != null) {
            binding.questionPager.unregisterOnPageChangeCallback(listener!!)
            listener = null
        }
        timer?.cancel()
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
        mockQuestionAdapter = null
        binding.questionPager.adapter = null
        super.onDestroy()
    }
}