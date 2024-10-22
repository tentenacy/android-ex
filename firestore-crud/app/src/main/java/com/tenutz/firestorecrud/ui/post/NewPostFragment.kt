package com.tenutz.firestorecrud.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.esafirm.imagepicker.features.ImagePickerConfig
import com.esafirm.imagepicker.features.ImagePickerLauncher
import com.esafirm.imagepicker.features.ImagePickerMode
import com.esafirm.imagepicker.features.registerImagePicker
import com.esafirm.imagepicker.model.Image
import com.tenutz.firestorecrud.databinding.FragmentNewPostBinding
import com.tenutz.firestorecrud.ui.bdadapter.ImageBindingAdapter
import com.tenutz.firestorecrud.util.EVENT_NAVIGATE
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewPostFragment: Fragment() {

    private var _binding: FragmentNewPostBinding? = null
    val binding get() = _binding!!

    private lateinit var imagePickerLauncher: ImagePickerLauncher

    private val vm: NewPostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imagePickerLauncher = registerImagePicker { result: List<Image> ->
            result.getOrNull(0)?.let { vm.setImageUri(it) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val root = FragmentNewPostBinding.inflate(inflater, container, false).apply {
            _binding = this
        }.root

        binding.vm = vm
        binding.lifecycleOwner = this

        return  root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNewpostPost.setOnClickListener {
            vm.newPost()
        }

        binding.imgNewpostPlusImage.setOnClickListener {
            imagePickerLauncher.launch(ImagePickerConfig { mode = ImagePickerMode.SINGLE })
        }

        binding.btnNewpostAi.setOnClickListener {
            vm.generateAIPost()
        }

        vm.viewEvent.observe(viewLifecycleOwner, { event ->
            event.getContentIfNotHandled()?.let {
                when(it.first) {
                    EVENT_NAVIGATE -> findNavController().navigateUp()
                }
            }
        })
    }

}