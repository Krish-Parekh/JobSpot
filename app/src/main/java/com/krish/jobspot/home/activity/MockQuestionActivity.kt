package com.krish.jobspot.home.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayoutMediator
import com.krish.jobspot.R
import com.krish.jobspot.databinding.ActivityMockQuestionBinding
import com.krish.jobspot.home.adapter.MockQuestionPageAdapter
import com.krish.jobspot.home.viewmodel.MockTestViewModel

class MockQuestionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMockQuestionBinding
    private val mockTestViewModel: MockTestViewModel by viewModels()
    private val args by navArgs<MockQuestionActivityArgs>()
    private val mock by lazy { args.mock }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMockQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViews()
    }

    private fun setupViews() {
        showInstructionDialog()
        binding.questionPager.adapter = MockQuestionPageAdapter(this, mock.mockQuestion.size, mock.mockQuestion)

        TabLayoutMediator(binding.questionCountTabLayout, binding.questionPager) { tab, position ->
            tab.text = "${position + 1}"
        }.attach()

        for (i in 0..mock.mockQuestion.size) {
            val textView =
                LayoutInflater.from(this).inflate(R.layout.tab_title, null, false) as TextView
            binding.questionCountTabLayout.getTabAt(i)?.customView = textView
        }

    }

    private fun showInstructionDialog() {
        val dialog = BottomSheetDialog(this)
        val bottomSheet = layoutInflater.inflate(R.layout.mock_test_rule_layout, null)
        val testName: TextView = bottomSheet.findViewById(R.id.tvMockTestName)
        val questionCount: TextView = bottomSheet.findViewById(R.id.tvMockTestQuestionCount)
        val testDuration: TextView = bottomSheet.findViewById(R.id.tvMockTestDuration)
        val startTest: MaterialButton = bottomSheet.findViewById(R.id.btnStartTest)
        testName.text = mock.title
        questionCount.text = mock.mockQuestion.size.toString()
        testDuration.text = getString(R.string.field_mock_duration, mock.duration)
        dialog.setCancelable(false)
        startTest.setOnClickListener {
            mockTestViewModel.updateStudentTestStatus(mock.uid)
            dialog.dismiss()
        }
        dialog.setContentView(bottomSheet)
        dialog.show()
    }
}