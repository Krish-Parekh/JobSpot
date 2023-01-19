package com.krish.jobspot.home.fragments.userFragment

import android.content.ContentResolver.MimeTypeInfo
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.MimeTypeFilter
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.krish.jobspot.R
import com.krish.jobspot.databinding.FragmentUserResumeEditBinding
import com.krish.jobspot.home.viewmodel.UserEditViewModel
import com.krish.jobspot.util.LoadingDialog
import com.krish.jobspot.util.UiState
import com.krish.jobspot.util.UiState.*

class UserResumeEditFragment : Fragment() {
    private var _binding : FragmentUserResumeEditBinding? = null
    private val binding get() = _binding!!
    private val userEditViewModel : UserEditViewModel by viewModels()
    private val loadingDialog : LoadingDialog by lazy { LoadingDialog(requireContext()) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserResumeEditBinding.inflate(inflater, container, false)

        setupViews()

        return binding.root
    }

    private fun setupViews() {

        binding.ivPopOut.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.layoutUploadedPdf.llFileRemoveContainer.visibility = View.GONE

        userEditViewModel.fetchStudentResume()
        setupObserver()
        userEditViewModel.fileData.observe(viewLifecycleOwner, Observer { pdfMetaData ->
            if (pdfMetaData != null){
                binding.layoutUploadedPdf.tvFileName.text = pdfMetaData.first
                binding.layoutUploadedPdf.tvFileMetaData.text = pdfMetaData.second
                binding.layoutUploadedPdf.root.setOnClickListener {
                    setPdfIntent(pdfMetaData.third)
                }
            }
        })
    }

    private fun setupObserver() {
        userEditViewModel.resumeStatus.observe(viewLifecycleOwner){ uiState ->
            when(uiState){
                LOADING -> {
                    loadingDialog.show()
                }
                SUCCESS -> {
                    loadingDialog.dismiss()
                }
                FAILURE -> {
                    loadingDialog.dismiss()
                }
                else -> Unit
            }

        }
    }

    private fun setPdfIntent(pdfUri: Uri) {
        val pdfIntent = Intent(Intent.ACTION_VIEW)
        pdfIntent.setDataAndType(pdfUri, "application/pdf")
        startActivity(pdfIntent)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}