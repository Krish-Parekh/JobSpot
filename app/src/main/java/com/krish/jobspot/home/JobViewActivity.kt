package com.krish.jobspot.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.navArgs
import coil.load
import com.google.android.material.chip.Chip
import com.krish.jobspot.R
import com.krish.jobspot.databinding.ActivityJobViewBinding
import com.krish.jobspot.util.createSalaryText

class JobViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJobViewBinding
    private val args by navArgs<JobViewActivityArgs>()
    private val job by lazy { args.job }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobViewBinding.inflate(layoutInflater)

        setupViews()
        setContentView(binding.root)
    }

    private fun setupViews() {
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
                val chip = Chip(this@JobViewActivity)
                chip.text = job
                requiredSkillSetChipGroup.addView(chip)
            }
        }
    }
}