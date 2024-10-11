package org.brightmindenrichment.street_care.ui.user

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.ChangePasswordBinding
import org.brightmindenrichment.street_care.util.Extensions

class ChangePasswordFragment : Fragment() {
    private var _binding: ChangePasswordBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private var oldpassword: String = ""
    private var newpassword: String = ""
    private var reenternewpassword: String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = ChangePasswordBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSaveChanges.setOnClickListener{
            onSaveChanges()
        }
        binding.btnCancel.setOnClickListener{
            onCancel()
        }


    }

    private fun onCancel() {
        findNavController().popBackStack()
    }

    private fun onSaveChanges() {
        oldpassword = binding.editTextOldPassword.text.toString()
        newpassword = binding.editTextNewPassword.text.toString()
        reenternewpassword = binding.editTextReenterNewPassword.text.toString()

        if (TextUtils.isEmpty(oldpassword)) {
            binding.editTextOldPassword.setError(getString(R.string.mandatory))
        } else if (TextUtils.isEmpty(newpassword)  ) {
            binding.editTextNewPassword.setError(getString(R.string.mandatory))
        } else if (TextUtils.isEmpty(reenternewpassword)  ) {
            binding.editTextReenterNewPassword.setError(getString(R.string.mandatory))
        } else if (!newpassword.contentEquals(reenternewpassword)  ) {
            binding.editTextNewPassword.setError(getString(R.string.error_occurred))
        } else if(newpassword.contentEquals(oldpassword)){
            binding.editTextNewPassword.setError(getString(R.string.same_password))
        } else{
            changePassword(oldpassword,newpassword)
        }
    }

    private fun changePassword(oldPassword: String, newPassword: String) {

        auth = Firebase.auth
        val user = auth.currentUser

            // Check if the user is logged in
            user?.let {
                // Get user's email
                val email = user.email

                // Re-authenticate the user with the old password
                val credential = EmailAuthProvider.getCredential(email!!, oldPassword)
                user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        // If re-authentication is successful, update the password
                        user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                // Password updated successfully
                                Toast.makeText(requireContext(), getString(R.string.password_updated), Toast.LENGTH_SHORT).show()
                                findNavController().popBackStack()
                            } else {
                                // Password update failed
                                Toast.makeText(requireContext(), getString(R.string.error_occurred), Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        // Re-authentication failed
                        Toast.makeText(requireContext(), getString(R.string.wrong_password), Toast.LENGTH_SHORT).show()
                    }
                }
            } ?: run {
                // User is not logged in
                Toast.makeText(requireContext(), getString(R.string.user_not_logged_in), Toast.LENGTH_SHORT).show()
            }

    }


}