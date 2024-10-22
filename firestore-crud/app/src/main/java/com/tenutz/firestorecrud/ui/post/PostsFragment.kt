package com.tenutz.firestorecrud.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.tenutz.firestorecrud.databinding.FragmentPostsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostsFragment: Fragment() {

    private var _binding: FragmentPostsBinding? = null
    val binding get() = _binding!!

    private val vm: PostsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentPostsBinding.inflate(inflater, container, false).apply {
            _binding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.posts.observe(viewLifecycleOwner, {
            binding.revPosts.adapter = PostsAdapter(it) { docId, authorUid ->
                findNavController().navigate(PostsFragmentDirections.actionPostsFragmentToPostFragment(docId, authorUid))
            }
        })

        findNavController().currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
            savedStateHandle.getLiveData<String>("post").observe(viewLifecycleOwner) { _ ->
                savedStateHandle.remove<String>("post")
                vm.fetchPosts()
            }
        }
    }

}