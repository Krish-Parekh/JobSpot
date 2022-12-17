package com.krish.jobspot.home.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.krish.jobspot.databinding.FragmentHomeBinding
import com.krish.jobspot.home.adapter.JobListAdapter
import com.krish.jobspot.home.viewmodel.HomeViewModel
import com.krish.jobspot.model.Job
import com.krish.jobspot.util.counterAnimation


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    private val jobListAdapter: JobListAdapter by lazy { JobListAdapter(::onItemClick, requireActivity()) }
    private val homeViewModel: HomeViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupView()

        return binding.root
    }

    private fun setupView() {
        homeViewModel.fetchJobs()
        binding.apply {

            counterAnimation(0,50, tvCompaniesCount)
            counterAnimation(0,50, tvJobAppliedCount)

            rvRecentJobs.adapter = jobListAdapter
            rvRecentJobs.layoutManager = LinearLayoutManager(requireContext())

            homeViewModel.jobs.observe(viewLifecycleOwner, Observer { jobs ->
                jobListAdapter.setJobListData(newJobs = jobs)
            })
        }
    }

    private fun onItemClick(job: Job) {
        val direction = HomeFragmentDirections.actionHomeFragmentToJobViewActivity(job = job)
        findNavController().navigate(direction)
    }
}