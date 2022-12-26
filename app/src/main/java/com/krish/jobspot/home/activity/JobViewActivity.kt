package com.krish.jobspot.home.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.navArgs
import coil.load
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.krish.jobspot.R
import com.krish.jobspot.databinding.ActivityJobViewBinding
import com.krish.jobspot.home.viewmodel.StudentJobViewModel
import com.krish.jobspot.model.JobApplication
import com.krish.jobspot.util.LoadingDialog
import com.krish.jobspot.util.UiState.*
import com.krish.jobspot.util.createSalaryText
import com.krish.jobspot.util.showToast

private const val TAG = "JobViewActivity"
class JobViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJobViewBinding
    private val args by navArgs<JobViewActivityArgs>()
    private val job by lazy { args.job }
    private val mFirebaseAuth : FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val studentId : String by lazy { mFirebaseAuth.currentUser?.uid.toString() }
    private val studentJobViewModel : StudentJobViewModel by viewModels()
    private val loadingDialog : LoadingDialog by lazy { LoadingDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViews()
    }

    private fun setupViews() {
        studentJobViewModel.checkJobStatus(jobId = job.uid, studentId = studentId)
        handJobStatusResponse()
        binding.apply {
            ivPopOut.setOnClickListener {
                finish()
            }
            ivCompanyLogo.load(job.imageUrl)

            tvRole.text = job.role
            tvCompanyLocation.text = getString(R.string.field_company_and_location, job.name, job.city)
            tvJobDescription.text = job.description
            tvResponsibility.text = job.responsibility
            tvSalary.text = createSalaryText(job.salary, this@JobViewActivity)

            job.skillSet.forEach { job ->
                createSkillSetChip(job)
            }
        }
    }

    private fun createSkillSetChip(job: String) {
        val chip = Chip(this@JobViewActivity)
        chip.text = job
        chip.chipBackgroundColor = ContextCompat.getColorStateList(this@JobViewActivity, R.color.chip_background_color)
        chip.setTextColor(this@JobViewActivity.getColor(R.color.chip_text_color))
        chip.chipCornerRadius = 8f
        binding.requiredSkillSetChipGroup.addView(chip)
    }

    private fun handJobStatusResponse() {
        studentJobViewModel.checkJobStatus.observe(this@JobViewActivity, Observer { uiState ->
            when(uiState){
                LOADING -> {
                    loadingDialog.show()
                }
                SUCCESS -> {
                    if (studentJobViewModel.isJobApplicationSubmitted){
                        binding.btnApply.text = getString(R.string.field_job_applied)
                        binding.btnApply.isEnabled = false
                    } else {
                        binding.btnApply.isEnabled = true
                        binding.btnApply.setOnClickListener {
                            val jobApplication = JobApplication(jobId = job.uid, studentId = studentId)
                            studentJobViewModel.applyJob(jobApplication)
                            handleApplicationResponse()
                        }
                    }
                    loadingDialog.dismiss()
                }
                FAILURE -> {
                    loadingDialog.dismiss()
                }
                else -> Unit
            }
        })
    }

    private fun handleApplicationResponse() {
        studentJobViewModel.applicationStatus.observe(this, Observer { uiState ->
            when(uiState){
                LOADING -> {
                    loadingDialog.show()
                }
                SUCCESS -> {
                    showToast(this, getString(R.string.field_job_applied))
                    loadingDialog.dismiss()
                    finish()
                }
                FAILURE -> {
                    showToast(this, getString(R.string.field_error_job_applied))
                    loadingDialog.dismiss()
                }
                else -> Unit
            }
        })
    }
}