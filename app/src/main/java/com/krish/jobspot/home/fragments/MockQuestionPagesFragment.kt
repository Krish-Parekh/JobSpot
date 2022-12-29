package com.krish.jobspot.home.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayoutMediator
import com.krish.jobspot.R
import com.krish.jobspot.databinding.FragmentMockQuestionPagesBinding
import com.krish.jobspot.home.adapter.MockQuestionPageAdapter
import com.krish.jobspot.home.viewmodel.MockPagesViewModel
import com.krish.jobspot.home.viewmodel.MockTestViewModel
import com.krish.jobspot.model.Mock
import java.util.concurrent.TimeUnit

private const val TAG = "MockQuestionPagesFragmentTAGS"

class MockQuestionPagesFragment : Fragment() {
    private var _binding: FragmentMockQuestionPagesBinding? = null
    private val binding get() = _binding!!
    private var mock: Mock? = null
    private var timer: CountDownTimer? = null
    private var timeRemaining: Long = 0
    private var ruleDisplayed: Boolean = false
    private var timerStarted: Boolean = false
    private var tabLayoutMediator: TabLayoutMediator? = null
    private var mockQuestionAdapter: MockQuestionPageAdapter? = null
    private val mockTestViewModel: MockTestViewModel by viewModels()
    private val mockPagesViewModel: MockPagesViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMockQuestionPagesBinding.inflate(inflater, container, false)
        mock = mockPagesViewModel.mock
        timeRemaining = TimeUnit.MINUTES.toMillis(mock!!.duration.toLong())
        if (savedInstanceState != null) {
            timeRemaining = savedInstanceState.getLong("TIME_REMAINING")
            ruleDisplayed = savedInstanceState.getBoolean("RULE_DISPLAYED")
            timerStarted = savedInstanceState.getBoolean("TIMER_STARTED")
        }

        setupViews()

        return binding.root
    }

    private fun setupViews() {
        binding.apply {

            ivPopOut.setOnClickListener {
                requireActivity().finish()
            }

            if (!ruleDisplayed) {
                showInstructionDialog(mock!!)
                ruleDisplayed = true
            }

            if (timerStarted) {
                startTimer()
            }

            mockQuestionAdapter = MockQuestionPageAdapter(
                parentFragmentManager,
                mock!!.mockQuestion,
                viewLifecycleOwner.lifecycle
            )
            questionPager.adapter = mockQuestionAdapter
            tabLayoutMediator =
                TabLayoutMediator(questionCountTabLayout, questionPager) { tab, position ->
                    tab.text = getString(R.string.field_tab_text, position + 1)
                }
            tabLayoutMediator?.attach()

            for (i in 0..mock!!.mockQuestion.size) {
                val tabTitle = LayoutInflater.from(requireContext())
                    .inflate(R.layout.tab_title, null, false) as TextView
                questionCountTabLayout.getTabAt(i)?.customView = tabTitle
            }

            binding.questionPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position == mock!!.mockQuestion.size - 1) {
                        binding.btnSubmitMock.visibility = View.VISIBLE
                    } else {
                        binding.btnSubmitMock.visibility = View.GONE
                    }
                }
            })

            binding.btnSubmitMock.setOnClickListener {
                mockPagesViewModel.submitQuiz(mock = mock!!)
            }
        }
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

    private fun showInstructionDialog(mock: Mock) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("TIME_REMAINING", timeRemaining)
        outState.putBoolean("RULE_DISPLAYED", ruleDisplayed)
        outState.putBoolean("TIMER_STARTED", timerStarted)
    }

    override fun onDestroyView() {
        binding.questionPager.adapter = null
        mock = null
        timer?.cancel()
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
        mockQuestionAdapter = null
        _binding = null
        super.onDestroyView()
    }

}