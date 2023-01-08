package com.krish.jobspot.home.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.krish.jobspot.R
import com.krish.jobspot.databinding.FragmentAlertBinding
import com.krish.jobspot.home.adapter.AlertAdapter
import com.krish.jobspot.home.viewmodel.AlertViewModel

private const val TAG = "ALERTFRAGMENT"
class AlertFragment : Fragment() {
    private var _binding : FragmentAlertBinding? = null
    private val binding get() = _binding!!

    private var _alertAdapter : AlertAdapter? = null
    private val alertAdapter get() = _alertAdapter!!

    private val alertViewModel by viewModels<AlertViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAlertBinding.inflate(inflater, container, false)
        _alertAdapter = AlertAdapter()

        setupUI()
        setupObserver()
        return binding.root
    }

    private fun setupUI() {
        binding.apply {
            alertViewModel.fetchNotifications()

            rvNotification.adapter = alertAdapter
            rvNotification.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObserver() {
        alertViewModel.notifications.observe(viewLifecycleOwner){ notifications ->
            Log.d(TAG, "Notifications: ${notifications}")
            alertAdapter.setData(notifications)
        }
    }
    override fun onDestroyView() {
        _binding = null
        _alertAdapter = null
        super.onDestroyView()
    }
}