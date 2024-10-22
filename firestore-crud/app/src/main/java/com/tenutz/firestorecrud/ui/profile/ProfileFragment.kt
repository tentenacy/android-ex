package com.tenutz.firestorecrud.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tenutz.firestorecrud.data.User
import com.tenutz.firestorecrud.databinding.FragmentProfileBinding
import com.tenutz.firestorecrud.util.formattedDate
import com.tenutz.firestorecrud.util.mainActivity
import com.tenutz.firestorecrud.util.user
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment: Fragment() {

    private var _binding: FragmentProfileBinding? = null
    val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentProfileBinding.inflate(inflater, container, false).apply {
            _binding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        Firebase.auth.currentUser?.let { binding.user = it }
        binding.txtProfileInfo.text = """
                email: ${user!!.email}
                
                emailVerified: ${user!!.isEmailVerified}
                
                uid: ${user!!.uid}
                
                registeredAt: ${user!!.metadata?.creationTimestamp?.formattedDate}
                
                lastSignInTimestamp: ${user!!.metadata?.lastSignInTimestamp?.formattedDate}
                
                provider: ${User.provider}
            """.trimIndent()

        binding.btnProfile.setOnClickListener {
            mainActivity.signOut()
        }

        binding.btnProfilePosts.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToPostsFragment())
        }

        binding.btnProfileNewPost.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToNewPostFragment())
        }
    }
}