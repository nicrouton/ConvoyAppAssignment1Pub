package edu.temple.convoy

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class CustomDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage("Test")
            .setPositiveButton("OK") { _,_ -> }
            .create()

    companion object {
        const val TAG = "JoinConvoy"
    }
}