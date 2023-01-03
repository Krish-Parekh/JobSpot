package com.krish.jobspot.home.activity

import android.os.Bundle

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer

import androidx.recyclerview.widget.LinearLayoutManager

import com.krish.jobspot.R
import com.krish.jobspot.databinding.ActivityMockResultBinding
import com.krish.jobspot.home.adapter.MockSolutionAdapter
import com.krish.jobspot.home.viewmodel.MockSolutionViewModel

import com.krish.jobspot.util.checkTimeUnit


private const val TAG = "MockResultActivity"

class MockResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMockResultBinding
    private var _mockSolutionAdapter: MockSolutionAdapter? = null
    private val mockSolutionAdapter get() = _mockSolutionAdapter!!
    private val mockSolutionViewModel: MockSolutionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMockResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mockId = intent.extras?.getString("MOCK_ID")!!
        mockSolutionViewModel.fetchMockResult(mockId)
        _mockSolutionAdapter = MockSolutionAdapter()
        setupView()
    }

    private fun setupView() {
        binding.apply {

            rvSolution.adapter = mockSolutionAdapter
            rvSolution.layoutManager = LinearLayoutManager(this@MockResultActivity)

            mockSolutionViewModel.mockScore.observe(
                this@MockResultActivity,
                Observer { mockResult ->
                    if (mockResult != null) {
                        val totalQuestion = mockResult.totalQuestion.toFloat()
                        val correctAns = mockResult.correctAns.toInt()
                        val progress = (correctAns / totalQuestion) * 100
                        scoreProgressBar.progress = progress.toInt()
                        tvScore.text = getString(
                            R.string.field_score,
                            mockResult.correctAns,
                            mockResult.totalQuestion
                        )
                        tvIncorrectScore.text = getString(
                            R.string.field_score,
                            mockResult.incorrectAns,
                            mockResult.totalQuestion
                        )
                        tvUnAttemptedScore.text = getString(
                            R.string.field_score,
                            mockResult.unAttempted,
                            mockResult.totalQuestion
                        )
                        tvTimeTakenScore.text = checkTimeUnit(mockResult.timeTaken)
                    }
                })

            mockSolutionViewModel.mockSolution.observe(
                this@MockResultActivity,
                Observer { mockQuestions ->
                    if (mockQuestions.isNotEmpty()) {
                        mockSolutionAdapter.setMockQuestions(newMockQuestion = mockQuestions)
                    }
                })

        }
    }

    override fun onDestroy() {
        _mockSolutionAdapter = null
        binding.rvSolution.adapter = null
        super.onDestroy()
    }
}