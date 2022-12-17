package com.krish.jobspot.home.fragments

import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.krish.jobspot.databinding.FragmentJobsBinding
import com.krish.jobspot.home.adapter.JobListAdapter
import com.krish.jobspot.home.viewmodel.HomeViewModel
import com.krish.jobspot.model.Job


class JobsFragment : Fragment() {
    private lateinit var binding : FragmentJobsBinding
    private val homeViewModel : HomeViewModel by viewModels()
    private val jobs: MutableList<Job> by lazy { mutableListOf() }

    private val jobListAdapter: JobListAdapter by lazy { JobListAdapter(::onItemClick, requireActivity()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentJobsBinding.inflate(inflater, container, false)

        setupViews()

        return binding.root
    }

    private fun setupViews() {
        homeViewModel.fetchJobs()
        binding.apply {

            etSearch.addTextChangedListener { text: Editable? ->
                filterJobs(text)
            }
            rvJobs.adapter = jobListAdapter
            rvJobs.layoutManager = LinearLayoutManager(requireContext())

            homeViewModel.jobs.observe(viewLifecycleOwner, Observer { jobs ->
                jobListAdapter.setJobListData(newJobs = jobs)
                this@JobsFragment.jobs.clear()
                this@JobsFragment.jobs.addAll(jobs)
            })
        }
    }

    private fun filterJobs(text: Editable?) {
        if (!text.isNullOrEmpty()) {
            val filteredJobList = jobs.filter { job ->
                val title = job.role.lowercase()
                val inputText = text.toString().lowercase()
                title.contains(inputText)
            }
            jobListAdapter.setJobListData(newJobs = filteredJobList)
        } else {
            jobListAdapter.setJobListData(newJobs = jobs)
        }
    }

    private fun onItemClick(job: Job) {
        val direction = JobsFragmentDirections.actionJobsFragmentToJobViewActivity(job = job)
        findNavController().navigate(direction)
    }
}