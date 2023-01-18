package com.krish.jobspot.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.krish.jobspot.R
import com.krish.jobspot.model.Job
import com.krish.jobspot.util.createSalaryText

class JobListAdapter(
    private val onItemClick: (job: Job) -> Unit,
    private val activity: FragmentActivity
) : RecyclerView.Adapter<JobListAdapter.JobListAdapterViewHolder>() {

    private var jobs: MutableList<Job> = mutableListOf()

    inner class JobListAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val jobCard: MaterialCardView = itemView.findViewById(R.id.cvJob)
        private val companyLogo: ImageView = itemView.findViewById(R.id.ivCompanyLogo)
        private val jobRole: TextView = itemView.findViewById(R.id.tvJobRole)
        private val companyNameLocation: TextView =
            itemView.findViewById(R.id.tvCompanyNameLocation)
        private val salary: TextView = itemView.findViewById(R.id.tvSalary)
        private val designation: TextView = itemView.findViewById(R.id.chipDesignation)
        private val workType: Chip = itemView.findViewById(R.id.chipWorkType)
        private val btnApply: Chip = itemView.findViewById(R.id.btnApply)

        fun bind(job: Job) {
            companyLogo.load(job.imageUrl) {
                error(R.drawable.ic_apple_logo)
                placeholder(R.drawable.ic_jobs)
                build()
            }
            jobRole.text = job.role
            designation.text = job.designation
            companyNameLocation.text =
                itemView.context.getString(R.string.field_company_and_location, job.name, job.city)
            salary.text = createSalaryText(job.salary, requireActivity = activity)
            btnApply.setOnClickListener {
                onItemClick(job)
            }
            jobCard.setOnClickListener {
                onItemClick(job)
            }
            workType.text = job.workType
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobListAdapterViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.job_detail_card, parent, false)
        return JobListAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobListAdapterViewHolder, position: Int) {
        holder.bind(jobs[position])
    }

    override fun getItemCount(): Int = jobs.size


    fun setJobListData(newJobs: List<Job>) {
        jobs.clear()
        jobs.addAll(newJobs)
        notifyDataSetChanged()
    }
}