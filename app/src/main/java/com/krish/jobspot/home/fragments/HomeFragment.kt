package com.krish.jobspot.home.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.krish.jobspot.R
import com.krish.jobspot.databinding.FragmentHomeBinding
import com.krish.jobspot.home.activity.UserActivity
import com.krish.jobspot.home.adapter.JobListAdapter
import com.krish.jobspot.home.viewmodel.HomeViewModel
import com.krish.jobspot.model.Job
import com.krish.jobspot.util.counterAnimation


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var _jobListAdapter: JobListAdapter? = null
    private val jobListAdapter get() = _jobListAdapter!!
    private val homeViewModel: HomeViewModel by viewModels()
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        _jobListAdapter = JobListAdapter(::onItemClick, requireActivity())
        setupView()

        return binding.root
    }

    private fun setupView() {
        homeViewModel.fetchJobs()
        homeViewModel.fetchCurrentUser()
        binding.apply {

            lifecycleScope.launchWhenCreated {
                homeViewModel.countUpdater.collect { counterValue ->
                    counterAnimation(0, counterValue.first, tvCompaniesCount)
                    counterAnimation(0, counterValue.second, tvJobAppliedCount)
                }
            }
            binding.tvWelcomeHeading.text = getString(R.string.field_welcome_text, mAuth.currentUser?.displayName)
            ivProfileImage.load(mAuth.currentUser?.photoUrl)

            ivProfileImage.setOnClickListener {
                navigateToUserActivity()
            }

            rvRecentJobs.adapter = jobListAdapter
            rvRecentJobs.layoutManager = LinearLayoutManager(requireContext())

            homeViewModel.jobs.observe(viewLifecycleOwner, Observer { jobs ->
                jobListAdapter.setJobListData(newJobs = jobs)
            })
        }
    }

    private fun navigateToUserActivity() {
        val intent = Intent(requireContext(), UserActivity::class.java)
        startActivity(intent)
    }

    private fun onItemClick(job: Job) {
        val direction = HomeFragmentDirections.actionHomeFragmentToJobViewActivity(job = job)
        findNavController().navigate(direction)
    }

    override fun onDestroyView() {
        _jobListAdapter = null
        _binding = null
        super.onDestroyView()
    }
}