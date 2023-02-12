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
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.krish.jobspot.R
import com.krish.jobspot.databinding.ActivityMockQuestionBinding
import com.krish.jobspot.databinding.MockTestRuleLayoutBinding
import com.krish.jobspot.home.adapter.MockQuestionPageAdapter
import com.krish.jobspot.home.viewmodel.QuestionPageViewModel
import com.krish.jobspot.model.Mock
import com.krish.jobspot.util.LoadingDialog
import com.krish.jobspot.util.Status.*
import java.util.concurrent.TimeUnit


private const val TAG = "MockQuestionActivityTAG"

class MockQuestionActivity : AppCompatActivity() {
    private var _binding: ActivityMockQuestionBinding? = null
    private val binding get() = _binding!!
    private val questionPageViewModel by viewModels<QuestionPageViewModel>()
    private var mock: Mock = Mock()
    private var mockId: String = ""
    private var timer: CountDownTimer? = null
    private var timeRemaining: Long = 0
    private var timerStarted: Boolean = false
    private var mediator: TabLayoutMediator? = null // done
    private val loadingDialog by lazy { LoadingDialog(this) }
    private var pageChangeListener: ViewPager2.OnPageChangeCallback? = null // done
    private var backPressCallBack: OnBackPressedCallback? = null // done
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMockQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState != null) {
            timeRemaining = savedInstanceState.getLong("TIME_REMAINING")
            timerStarted = savedInstanceState.getBoolean("TIMER_STARTED")
        }
        setupUI()
        setupObserver()
    }

    private fun setupUI() {
        val bundle = intent.extras!!
        mockId = bundle.getString("MOCK_ID")!!
        questionPageViewModel.fetchMockTest(mockId)
        backPressCallBack = this@MockQuestionActivity.onBackPressedDispatcher.addCallback {
            submitDialog("Quit Mock", "Are you sure you want to quit?")
        }
        if (timerStarted) {
            startTimer()
        }

        with(binding) {
            ivPopOut.setOnClickListener {
                submitDialog("Quit Mock", "Are you sure you want to quit?")
            }

            btnSubmitQuiz.setOnClickListener {
                submitDialog("Submit Mock", "Are you sure you want to submit this mock?")
            }
        }
    }

    private fun setupObserver() {
        questionPageViewModel.mock.observe(this) { resource ->
            when (resource.status) {
                LOADING -> {
                    loadingDialog.show()
                }
                SUCCESS -> {
                    loadingDialog.dismiss()
                    mock = resource.data!!
                    timeRemaining = TimeUnit.MINUTES.toMillis(mock.duration.toLong())
                    questionPageViewModel.ruleDisplayed.observe(this) { value ->
                        value?.let { ruleDisplayed ->
                            if (ruleDisplayed.not()) {
                                instructionDialog()
                                setupViewPager()
                            }
                        }
                    }
                }
                ERROR -> {
                    loadingDialog.dismiss()
                }
            }
        }

        questionPageViewModel.submitMockStatus.observe(this) { resource ->
            when (resource.status) {
                LOADING -> {
                    loadingDialog.show()
                }
                SUCCESS -> {
                    loadingDialog.dismiss()
                    navigateToResultActivity()
                }
                ERROR -> {
                    loadingDialog.dismiss()
                }
            }
        }
    }

    private fun navigateToResultActivity() {
        val mockResult = Intent(this, MockResultActivity::class.java)
        mockResult.putExtra("MOCK_ID", mockId)
        startActivity(mockResult)
        finish()
    }

    private fun setupViewPager() {
        val mockQuestion = mock.mockQuestion
        binding.questionPager.adapter =
            MockQuestionPageAdapter(supportFragmentManager, mockQuestion, lifecycle)
        binding.questionPager.offscreenPageLimit = mockQuestion.size
        mediator = TabLayoutMediator(
            binding.questionCountTabLayout,
            binding.questionPager
        ) { tab, position ->
            tab.text = getString(R.string.field_tab_text, position + 1)
        }
        mediator?.attach()
        mockQuestion.forEachIndexed { index, _ ->
            val inflater = LayoutInflater.from(this@MockQuestionActivity)
            val tabTitle = inflater.inflate(R.layout.tab_title, null, false) as TextView
            binding.questionCountTabLayout.getTabAt(index)?.customView = tabTitle
        }

        pageChangeListener = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == mockQuestion.lastIndex) {
                    binding.btnSubmitQuiz.visibility = View.VISIBLE
                } else {
                    binding.btnSubmitQuiz.visibility = View.GONE
                }
            }
        }

        binding.questionPager.registerOnPageChangeCallback(pageChangeListener!!)
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
                questionPageViewModel.ruleDisplayed.postValue(true)
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
        outState.putBoolean("TIMER_STARTED", timerStarted)
    }

    override fun onDestroy() {
        pageChangeListener?.let { listener ->
            binding.questionPager.unregisterOnPageChangeCallback(listener)
            pageChangeListener = null
        }
        timer?.cancel()
        backPressCallBack?.remove()
        mediator?.detach()
        timer = null
        mediator = null
        _binding = null
        super.onDestroy()
    }
}