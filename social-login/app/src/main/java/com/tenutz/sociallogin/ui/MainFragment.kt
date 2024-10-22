package com.tenutz.sociallogin.ui

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.tenutz.sociallogin.R
import com.tenutz.sociallogin.data.ReAuthPref
import com.tenutz.sociallogin.databinding.FragmentMainBinding

class MainFragment: Fragment() {

    private val auth = Firebase.auth

    companion object {
        private val TAG = MainFragment::class.java.simpleName
    }

    private var _binding: FragmentMainBinding? = null
    val binding: FragmentMainBinding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentMainBinding.inflate(inflater, container, false).apply {
            _binding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.svMainContent.isVerticalScrollBarEnabled = false
        binding.svMain.isVerticalScrollBarEnabled = false

        binding.btnMainSignout.setOnClickListener {
            auth.signOut()
            resetAfterSignout()
            navigateToLoginFragment()
        }

        binding.btnMainProfile.setOnClickListener {
            binding.txtMainContent.text = """
                name: ${auth.currentUser?.displayName}
                email: ${auth.currentUser?.email}
                photoUrl: ${auth.currentUser?.photoUrl}
                emailVerified: ${auth.currentUser?.isEmailVerified}
                uid: ${auth.currentUser?.uid}
            """.trimIndent()
        }

        binding.btnMainSpecificProfile.setOnClickListener {
            auth.currentUser?.let {

                var text = ""

                for (profile in it.providerData)
                    text += """
                        providerId: ${profile.providerId}
                        uid: ${profile.uid}
                        name: ${profile.displayName}
                        email: ${profile.email}
                        photoUrl: ${profile.photoUrl}
                        
                    """.trimIndent()

                binding.txtMainContent.text = text
            }
        }

        binding.btnMainUpdateProfile.setOnClickListener {
            auth.currentUser?.run {
                val inputs = binding.etMainInput.text.toString().split(",")
                val profileUpdates = userProfileChangeRequest {
                    displayName = inputs[0]
                    inputs.getOrNull(1)?.let { photoUri = Uri.parse(inputs[1]) }
                }
                updateProfile(profileUpdates)
                    .addOnSuccessListener {
                        Log.i(TAG, "프로필 업데이트 성공")
                        toastSuccess()
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "프로필 업데이트 실패")
                        toastFailure()
                    }
            }
        }

        binding.btnMainSetEmail.setOnClickListener {
            auth.currentUser?.run {
                //Important: To set a user's email address, the user must have signed in recently. See Re-authenticate a user.
                val newEmailAddress = binding.etMainInput.text.toString()
                updateEmail(newEmailAddress)
                    .addOnSuccessListener {
                        Log.i(TAG, "이메일 설정 성공")
                        toastSuccess()
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "이메일 설정 실패")
                        toastFailure()
                    }
            }
        }

        binding.btnMainSendVerificationEmail.setOnClickListener {
            auth.currentUser?.run {

                //Additionally you can localize the verification email by updating the language code on the Auth instance before sending the email. For example:
//                auth.setLanguageCode("fr")

                sendEmailVerification()
                    .addOnSuccessListener {
                        Log.i(TAG, "이메일 전송 성공")
                        toastSuccess()
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "이메일 전송 실패")
                        toastFailure()
                    }
            }
        }

        binding.btnMainSetPwd.setOnClickListener {
            auth.currentUser?.run {
                //Important: To set a user's password, the user must have signed in recently. See Re-authenticate a user.
                val newPassword = binding.etMainInput.text.toString()
                updatePassword(newPassword)
                    .addOnSuccessListener {
                        Log.i(TAG, "비밀번호 설정 성공")
                        toastSuccess()
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "비밀번호 설정 실패")
                        toastFailure()
                    }
            }
        }

        binding.btnMainSendReset.setOnClickListener {
            //Additionally you can localize the verification email by updating the language code on the Auth instance before sending the email. For example:
//                auth.setLanguageCode("fr")

            val emailAddress = binding.etMainInput.text.toString()
            auth.sendPasswordResetEmail(emailAddress)
                .addOnSuccessListener {
                    Log.i(TAG, "이메일 전송 성공")
                    toastSuccess()
                }
                .addOnFailureListener {
                    Log.e(TAG, "이메일 전송 실패")
                    toastFailure()
                }
        }

        binding.btnMainDeleteUser.setOnClickListener {

            auth.currentUser?.run {
                delete()
                    .addOnSuccessListener {
                        Log.i(TAG, "계정 삭제 성공")
                        toastSuccess()
                        resetAfterSignout()
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "계정 삭제 실패")
                        toastFailure()
                    }
            }
        }

        binding.btnMainReauthenticate.setOnClickListener {

            auth.currentUser?.run {
                ReAuthPref.credential?.let {
                    reauthenticate(it)
                        .addOnSuccessListener {
                            Log.i(TAG, "재인증 성공")
                            toastSuccess()
                        }
                        .addOnFailureListener {
                            Log.e(TAG, "재인증 실패")
                            toastFailure()
                        }
                } ?: ReAuthPref.firebaseToken?.let {
                    auth.signInWithCustomToken(it)
                        .addOnSuccessListener { result ->
                            Log.i(TAG, "파이어베이스로 로그인 성공")
                            toastSuccess()
                        }
                        .addOnFailureListener {
                            Log.e(TAG, "파이어베이스로 로그인 실패")
                            toastFailure()
                        }
                } ?: run {
                    val inputs = binding.etMainInput.text.toString().split(",")
                    val email = inputs.getOrNull(0)
                    val password = inputs.getOrNull(1)
                    val credential = EmailAuthProvider.getCredential(email ?: "", password ?: "")
                    reauthenticate(credential)
                        .addOnSuccessListener {
                            Log.i(TAG, "재인증 성공")
                            toastSuccess()
                        }
                        .addOnFailureListener {
                            Log.e(TAG, "재인증 실패")
                            toastFailure()
                        }
                }
            }
        }

        binding.btnMainCreatePwdAccount.setOnClickListener {
            val inputs = binding.etMainInput.text.toString().split(",")
            auth.createUserWithEmailAndPassword(inputs[0], inputs[1])
                .addOnSuccessListener {
                    Log.i(TAG, "계정 생성 성공")
                    toastSuccess()

                    binding.txtMainContent.text = auth.currentUser?.run {
                        """
                            name: ${displayName}
                            email: ${email}
                            photoUrl: ${photoUrl}
                            emailVerified: ${isEmailVerified}
                            uid: ${uid}
                        """.trimIndent()
                    }
                }
                .addOnFailureListener {
                    Log.e(TAG, "계정 생성 실패")
                    toastFailure()
                }
        }

        binding.btnMainSigninPwdAccount.setOnClickListener {
            val inputs = binding.etMainInput.text.toString().split(",")
            auth.signInWithEmailAndPassword(inputs[0], inputs[1])
                .addOnSuccessListener {
                    Log.i(TAG, "계정 로그인 성공")
                    toastSuccess()

                    resetAfterSignout()

                    binding.txtMainContent.text = auth.currentUser?.run {
                        """
                                name: ${displayName}
                                email: ${email}
                                photoUrl: ${photoUrl}
                                emailVerified: ${isEmailVerified}
                                uid: ${uid}
                            """.trimIndent()
                    }
                }
                .addOnFailureListener {
                    Log.e(TAG, "계정 로그인 실패")
                    toastFailure()
                }
        }

        binding.btnMainSendAuthLink.setOnClickListener {

        }

    }

    private fun resetAfterSignout() {
        ReAuthPref.clear()
    }

    private fun navigateToLoginFragment() {
        findNavController().navigate(
            R.id.navigation_main,
            null,
            NavOptions.Builder().setPopUpTo(R.id.mainFragment, true).build()
        )
    }

    private fun toastSuccess() {
        Toast.makeText(requireContext(), "성공", Toast.LENGTH_SHORT).show()
    }

    private fun toastFailure() {
        Toast.makeText(requireContext(), "실패", Toast.LENGTH_SHORT).show()
    }

}