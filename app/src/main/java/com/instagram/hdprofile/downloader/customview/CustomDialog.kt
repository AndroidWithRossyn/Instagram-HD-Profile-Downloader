package com.instagram.hdprofile.downloader.customview

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import com.instagram.hdprofile.downloader.R
import com.instagram.hdprofile.downloader.databinding.DialogLoadingBinding

/** Provides utility functions for displaying custom dialogs throughout the application. */
object CustomDialog {

    private var dialogProgress: Dialog? = null

    /** Displays a full-screen progress dialog with custom animations and a dismiss option. */
    fun showProgressDialog(context: Context) {
        if (dialogProgress != null && dialogProgress?.isShowing == true) {
            return
        }
        dialogProgress = Dialog(context)
        val binding = DialogLoadingBinding.inflate(dialogProgress!!.layoutInflater)
        dialogProgress!!.setContentView(
            binding.root
        )
        dialogProgress?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialogProgress?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogProgress?.window?.attributes?.windowAnimations = R.style.DialogAnimationPopUp
        dialogProgress?.window?.setGravity(Gravity.CENTER)
        dialogProgress?.setCanceledOnTouchOutside(true)
        dialogProgress?.setCancelable(true)
        binding.dialogProgressLiner.setOnClickListener {
            dialogProgress?.dismiss()
        }
        dialogProgress?.show()
    }

    /** Hides the progress dialog if it is currently being displayed. */
    fun hideProgressDialog() {
        dialogProgress?.takeIf { it.isShowing }?.dismiss()
    }
}