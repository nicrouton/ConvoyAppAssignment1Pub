package edu.temple.convoy

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class PermissionAlertDialogFragment(
    val dialogText: String,
    val positiveButtonText: String,
    val negativeButtonText: String,
    val posAction: ((DialogInterface, Int) -> Unit),
    val negAction: ((DialogInterface, Int) -> Unit)
): DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext()).apply {
            setMessage(dialogText)
            setPositiveButton(positiveButtonText, posAction)
            setNegativeButton(negativeButtonText, negAction)
        }.create()


    companion object {
        const val TAG = "PermsRequest"
    }
}