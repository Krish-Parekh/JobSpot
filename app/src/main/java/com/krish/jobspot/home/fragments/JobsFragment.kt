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
import com.krish.jobspot.util.Status
import com.krish.jobspot.util.showToast


class JobsFragment : Fragment() {
    private var _binding : FragmentJobsBinding? = null
    private val binding get() = _binding!!
    private var _jobListAdapter: JobListAdapter? = null
    private val jobListAdapter get() = _jobListAdapter!!

    private val homeViewModel by viewModels<HomeViewModel>()
    private val jobs: MutableList<Job> by lazy { mutableListOf() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentJobsBinding.inflate(inflater, container, false)
        _jobListAdapter = JobListAdapter(::onItemClick, requireActivity())

        setupUI()
        setupObserver()

        return binding.root
    }

    private fun setupUI() {
        homeViewModel.fetchJobs()
        binding.apply {

            etSearch.addTextChangedListener { text: Editable? ->
                filterJobs(text)
            }
            rvJobs.adapter = jobListAdapter
            rvJobs.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObserver() {
        homeViewModel.jobs.observe(viewLifecycleOwner){ jobStatus ->
            when (jobStatus.status) {
                Status.LOADING -> Unit
                Status.SUCCESS -> {
                    val jobList = jobStatus.data!!
                    jobs.clear()
                    jobs.addAll(jobList)
                    jobListAdapter.setJobListData(jobList)
                }
                Status.ERROR -> {
                    val errorMessage = jobStatus.message!!
                    showToast(requireContext(), errorMessage)
                }
            }
        }
    }

    private fun filterJobs(text: Editable?) {
        if (!text.isNullOrEmpty()) {
            val filteredJobList = jobs.filter { job ->
                val title = job.role.lowercase()
                val inputText = text.toString().lowercase()
                title.contains(inputText)
            }
            jobListAdapter.setJobListData(filteredJobList)
        } else {
            jobListAdapter.setJobListData(jobs)
        }
    }

    private fun onItemClick(job: Job) {
        val direction = JobsFragmentDirections.actionJobsFragmentToJobViewActivity(job = job)
        findNavController().navigate(direction)
    }

    override fun onDestroyView() {
        jobs.clear()
        _jobListAdapter = null
        _binding = null
        super.onDestroyView()
    }
}