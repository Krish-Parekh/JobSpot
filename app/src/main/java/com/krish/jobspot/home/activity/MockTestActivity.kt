package com.krish.jobspot.home.activity

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.navArgs
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.krish.jobspot.R
import com.krish.jobspot.databinding.ActivityMockTestBinding
import com.krish.jobspot.home.viewmodel.MockTestViewModel
import com.krish.jobspot.model.MockTestState

class MockTestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMockTestBinding
    private val args by navArgs<MockTestActivityArgs>()
    private val mockTestViewModel: MockTestViewModel by viewModels()
    private val mockTestState: MockTestState by lazy { args.mockTestState }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMockTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mockTestViewModel.fetchMockTest(mockTestId = mockTestState.quizUid)
        showInstructionDialog()
    }

    private fun showInstructionDialog() {
        val dialog = BottomSheetDialog(this)
        val bottomSheet = layoutInflater.inflate(R.layout.mock_test_rule_layout, null)
        val testName: TextView = bottomSheet.findViewById(R.id.tvMockTestName)
        val questionCount: TextView = bottomSheet.findViewById(R.id.tvMockTestQuestionCount)
        val testDuration: TextView = bottomSheet.findViewById(R.id.tvMockTestDuration)
        val startTest: MaterialButton = bottomSheet.findViewById(R.id.btnStartTest)
        dialog.setCancelable(false)
        startTest.setOnClickListener {
            mockTestViewModel.updateStudentTestStatus(mockTestState.quizUid)
            dialog.dismiss()
        }
        dialog.setContentView(bottomSheet)
        dialog.show()
        mockTestViewModel.mock.observe(this, Observer { mock ->
            if (mock.uid.isNotEmpty()) {
                testName.text = mock.title
                questionCount.text = mock.mockQuestion.size.toString()
                testDuration.text = mock.duration + "m"
            }
        })


    }
}