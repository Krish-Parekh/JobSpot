package com.krish.jobspot.home.fragments.userFragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.krish.jobspot.databinding.FragmentUserResumeEditBinding
import com.krish.jobspot.home.viewmodel.UserEditViewModel
import com.krish.jobspot.util.LoadingDialog
import com.krish.jobspot.util.Status.*
import com.krish.jobspot.util.showToast

class UserResumeEditFragment : Fragment() {
    private var _binding: FragmentUserResumeEditBinding? = null
    private val binding get() = _binding!!
    private val userEditViewModel by viewModels<UserEditViewModel>()
    private val loadingDialog: LoadingDialog by lazy { LoadingDialog(requireContext()) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserResumeEditBinding.inflate(inflater, container, false)

        setupUI()
        setupObserver()

        return binding.root
    }

    private fun setupUI() {
        userEditViewModel.fetchResume()
        binding.apply {
            ivPopOut.setOnClickListener {
                findNavController().popBackStack()
            }
            layoutUploadedPdf.llFileRemoveContainer.visibility = View.GONE
        }
    }

    private fun setupObserver() {
        userEditViewModel.resumeState.observe(viewLifecycleOwner) { resumeState ->
            when (resumeState.status) {
                LOADING -> {
                    loadingDialog.show()
                }
                SUCCESS -> {
                    val (fileName, fileMetaData, resumeUri) = resumeState.data!!
                    binding.layoutUploadedPdf.tvFileName.text = fileName
                    binding.layoutUploadedPdf.tvFileMetaData.text = fileMetaData
                    binding.layoutUploadedPdf.root.setOnClickListener {
                        setPdfIntent(resumeUri)
                    }
                    loadingDialog.dismiss()
                }
                ERROR -> {
                    val errorMessage = resumeState.message!!
                    showToast(requireContext(), errorMessage)
                    loadingDialog.dismiss()
                }
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