package com.krish.jobspot.user_details.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.krish.jobspot.R
import com.krish.jobspot.databinding.FragmentStudentResumeBinding
import com.krish.jobspot.user_details.viewmodel.UserDetailViewModel
import com.krish.jobspot.util.showToast
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import com.krish.jobspot.databinding.BottomSheetDeleteFileBinding
import com.krish.jobspot.home.activity.HomeActivity
import com.krish.jobspot.util.LoadingDialog
import com.krish.jobspot.util.Status.*
import java.io.File
import java.util.*

private const val TAG = "StudentResumeFragment"

class StudentResumeFragment : Fragment() {

    private var _binding: FragmentStudentResumeBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<StudentResumeFragmentArgs>()
    private val userDetailViewModel by  viewModels<UserDetailViewModel>()
    private val pdfLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            handleCapturedPdf(result)
        }
    private val loadingDialog : LoadingDialog by lazy { LoadingDialog(requireContext()) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStudentResumeBinding.inflate(inflater, container, false)

        setupUI()
        setupObserver()

        return binding.root
    }

    private fun setupUI() {
        binding.apply {
            ivPopOut.setOnClickListener {
                findNavController().popBackStack()
            }

            layoutUploadPdf.root.setOnClickListener {
                startPdfIntent()
            }

            if(userDetailViewModel.getPdfUri() != null){
                hidePdfUploadView()
                getFileInfo(userDetailViewModel.getPdfUri()!!)
            }

            layoutUploadedPdf.llFileRemoveContainer.setOnClickListener {
                deleteResumeDialog()
            }

            btnSubmit.setOnClickListener {
                val imageUri = Uri.parse(args.student.details?.imageUrl)
                val pdfUri = userDetailViewModel.getPdfUri()
                if(pdfUri != null){
                    userDetailViewModel.uploadStudentData(pdfUri, imageUri, args.student)
                } else {
                    showToast(requireContext(), "Please attach your resume.")
                }
            }
        }
    }

    private fun setupObserver() {
        userDetailViewModel.uploadStudent.observe(viewLifecycleOwner) { uploadState ->
            when (uploadState.status) {
                LOADING -> {
                    loadingDialog.show()
                }
                SUCCESS -> {
                    hidePdfUploadedView()
                    userDetailViewModel.setPdfUri(null)
                    loadingDialog.dismiss()
                    navigateToHomeActivity()
                }
                ERROR -> {
                    showToast(requireContext(), "Error while uploading data, Retry!!")
                    loadingDialog.dismiss()
                }
            }
        }
    }

    private fun handleCapturedPdf(result: ActivityResult) {
        val resultCode = result.resultCode
        val data = result.data
        when (resultCode) {
            Activity.RESULT_OK -> {
                userDetailViewModel.setPdfUri(pdfUri = data?.data!!)
                getFileInfo(userDetailViewModel.getPdfUri()!!)
            }
            Activity.RESULT_CANCELED -> {
                Log.d(TAG, "TASK CANCELLED")
            }
        }
    }

    @SuppressLint("Range")
    private fun getFileInfo(pdfUri: Uri) {
        try {
            if (pdfUri.scheme.equals("content")) {
                val pdfCursor: Cursor? =
                    requireActivity().contentResolver.query(pdfUri, null, null, null, null)
                pdfCursor.use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        val fileName =
                            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                        val fileSize = cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE))
                        val fileDateTime: Long = try {
                            val tempColDate = cursor
                                .getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                            cursor.moveToFirst()
                            cursor.getLong(tempColDate)
                        } catch (e: Exception) {
                            Log.d(TAG, "Exception: ${e.message}")
                            0
                        }

                        val simpleFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = fileDateTime
                        val formattedDtm = simpleFormatter.format(calendar.time)

                        val fileSizeInMb = fileSize.toDouble() / (1024 * 1024)
                        val sizeInFormat = DecimalFormat("#.##").format(fileSizeInMb)
                        val maxSize = 5  // 5 MB in bytes
                        Log.d(TAG, "FileSize : $fileSizeInMb, MaxFileSize : $maxSize")
                        if(fileSizeInMb > maxSize){
                            showToast(requireContext(), "File size above 5MB")
                        }else{
                            setupFileView(fileName, sizeInFormat, formattedDtm)
                            hidePdfUploadView()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Exception: ${e.message}")
        }
    }

    private fun setupFileView(fileName: String?, sizeInFormat: String, formattedDtm: String) {
        userDetailViewModel.resumeFileName = fileName
        userDetailViewModel.fileMetaData = getString(R.string.resume_meta_data, sizeInFormat, formattedDtm)
        binding.layoutUploadedPdf.tvFileName.text = fileName
        binding.layoutUploadedPdf.tvFileMetaData.text =
            getString(R.string.resume_meta_data, sizeInFormat, formattedDtm)
    }

    private fun deleteResumeDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val studentDeleteSheetBinding = BottomSheetDeleteFileBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(studentDeleteSheetBinding.root)
        studentDeleteSheetBinding.apply {
            btnNo.setOnClickListener {
                bottomSheetDialog.dismiss()
            }
            btnRemoveFile.setOnClickListener {
                userDetailViewModel.setPdfUri(null)
                bottomSheetDialog.dismiss()
                hidePdfUploadedView()
            }
        }
        bottomSheetDialog.show()
    }

    private fun navigateToHomeActivity() {
        val homeActivity = Intent(requireContext(), HomeActivity::class.java)
        startActivity(homeActivity)
        requireActivity().finish()
    }

    private fun startPdfIntent() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        pdfLauncher.launch(intent)
    }

    private fun hidePdfUploadView() {
        binding.layoutUploadPdf.root.visibility = View.GONE
        binding.layoutUploadedPdf.root.visibility = View.VISIBLE
    }

    private fun hidePdfUploadedView() {
        binding.layoutUploadedPdf.root.visibility = View.GONE
        binding.layoutUploadPdf.root.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}