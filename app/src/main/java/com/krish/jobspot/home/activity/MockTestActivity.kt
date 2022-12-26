package com.krish.jobspot.home.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.krish.jobspot.R
import com.krish.jobspot.databinding.ActivityMockTestBinding
import com.krish.jobspot.home.viewmodel.MockTestViewModel
import com.krish.jobspot.model.QuizState

class MockTestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMockTestBinding
    private val args by navArgs<MockTestActivityArgs>()
    private val mockTestViewModel: MockTestViewModel by viewModels()
    private val quizState: QuizState by lazy { args.quizState }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMockTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showDeleteDialog()
    }

    private fun showDeleteDialog() {
        val dialog = BottomSheetDialog(this)
        val bottomSheet = layoutInflater.inflate(R.layout.quiz_rule_layout, null)
        val startTest: MaterialButton = bottomSheet.findViewById(R.id.btnStartTest)
        dialog.setCancelable(false)
        startTest.setOnClickListener {
            mockTestViewModel.updateStudentTestStatus(quizState.quizUid)
            dialog.dismiss()
        }
        dialog.setContentView(bottomSheet)
        dialog.show()
    }
}