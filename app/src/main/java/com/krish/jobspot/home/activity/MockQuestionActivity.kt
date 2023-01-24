package com.krish.jobspot.home.activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.navArgs
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.krish.jobspot.R
import com.krish.jobspot.databinding.ActivityMockQuestionBinding
import com.krish.jobspot.databinding.MockTestRuleLayoutBinding
import com.krish.jobspot.home.adapter.MockQuestionPageAdapter
import com.krish.jobspot.home.viewmodel.QuestionPageViewModel
import com.krish.jobspot.util.LoadingDialog
import com.krish.jobspot.util.Status.*
import com.krish.jobspot.util.showToast
import java.util.concurrent.TimeUnit


private const val TAG = "MockQuestionActivityTAG"

class MockQuestionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMockQuestionBinding
    private val questionPageViewModel by viewModels<QuestionPageViewModel>()
    private val args by navArgs<MockQuestionActivityArgs>()
    private val mock by lazy { args.mock }
    private var timer: CountDownTimer? = null
    private var timeRemaining: Long = 0
    private var ruleDisplayed: Boolean = false
    private var timerStarted: Boolean = false
    private var tabLayoutMediator: TabLayoutMediator? = null
    private var questionAdapter: MockQuestionPageAdapter? = null
    private var pageChangeListener: OnPageChangeCallback? = null
    private val loadingDialog: LoadingDialog by lazy { LoadingDialog(this) }
    private var backPressCallBack: OnBackPressedCallback? = null
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
        setupUI()
        setupObserver()

    }

    private fun setupUI() {
        backPressCallBack = this@MockQuestionActivity.onBackPressedDispatcher.addCallback {
            submitDialog("Quit Mock", "Are you sure you want to quit?")
        }
        if (ruleDisplayed.not()) {
            instructionDialog()
            ruleDisplayed = true
        }
        if (timerStarted) {
            startTimer()
        }
        binding.apply {
            ivPopOut.setOnClickListener {
                submitDialog("Quit Mock", "Are you sure you want to quit?")
            }
            val mockQuestion = mock.mockQuestion
            questionAdapter = MockQuestionPageAdapter(supportFragmentManager, mockQuestion, lifecycle)
            questionPager.adapter = questionAdapter
            questionPager.offscreenPageLimit = mockQuestion.size
            tabLayoutMediator = TabLayoutMediator(questionCountTabLayout, questionPager) { tab, position ->
                    tab.text = getString(R.string.field_tab_text, position + 1)
                }
            tabLayoutMediator?.attach()
            mockQuestion.forEachIndexed { index, _ ->
                val inflater = LayoutInflater.from(this@MockQuestionActivity)
                val tabTitle = inflater.inflate(R.layout.tab_title, null, false) as TextView
                questionCountTabLayout.getTabAt(index)?.customView = tabTitle
            }

            pageChangeListener = object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position == mockQuestion.lastIndex) {
                        btnSubmitQuiz.visibility = View.VISIBLE
                    } else {
                        btnSubmitQuiz.visibility = View.GONE
                    }
                }
            }
            questionPager.registerOnPageChangeCallback(pageChangeListener!!)

            btnSubmitQuiz.setOnClickListener {
                submitDialog("Submit Mock", "Are you sure you want to submit this mock?")
            }
        }
    }

    private fun setupObserver() {
        questionPageViewModel.submitMockStatus.observe(this){ submitState ->
            when(submitState.status){
                LOADING -> {
                    loadingDialog.show()
                }
                SUCCESS -> {
                    val message = submitState.data!!
                    showToast(this, message)
                    navigateToResultActivity()
                    loadingDialog.dismiss()
                }
                ERROR -> {
                    val errorMessage = submitState.message!!
                    showToast(this, errorMessage)
                    loadingDialog.dismiss()
                }
            }
        }
    }

    private fun navigateToResultActivity() {
        val mockResultActivity = Intent(this, MockResultActivity::class.java)
        mockResultActivity.putExtra("MOCK_ID", mock.uid)
        startActivity(mockResultActivity)
        finish()
    }

    private fun instructionDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val instructionSheetBinding = MockTestRuleLayoutBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(instructionSheetBinding.root)
        instructionSheetBinding.apply {
            tvMockTestName.text = mock.title
            tvMockTestQuestionCount.text = mock.mockQuestion.size.toString()
            tvMockTestDuration.text = getString(R.string.field_mock_duration, mock.duration)
            bottomSheetDialog.setCancelable(false)
            btnStartTest.setOnClickListener {
                questionPageViewModel.updateStudentTestStatus(mockId = mock.uid)
                bottomSheetDialog.dismiss()
                startTimer()
                timerStarted = true
            }
        }
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
                timeRemaining = 0
                showToast(this@MockQuestionActivity, "Time up.")
                questionPageViewModel.submitMock(mock, timeRemaining)
            }
        }
        timer?.start()
    }

    private fun submitDialog(
        title: String,
        message: String
    ) {
        MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ ->
                questionPageViewModel.submitMock(mock = mock, timeRemaining)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("TIME_REMAINING", timeRemaining)
        outState.putBoolean("RULE_DISPLAYED", ruleDisplayed)
        outState.putBoolean("TIMER_STARTED", timerStarted)
    }

    override fun onDestroy() {
        pageChangeListener?.let { listener ->
            binding.questionPager.unregisterOnPageChangeCallback(listener)
        }
        timer?.cancel()
        tabLayoutMediator?.detach()
        backPressCallBack?.remove()
        pageChangeListener = null
        tabLayoutMediator = null
        questionAdapter = null
        super.onDestroy()
    }
}