package com.tenutz.firestorecrud.ui.post

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.esafirm.imagepicker.model.Image
import com.tenutz.firestorecrud.BuildConfig
import com.tenutz.firestorecrud.data.ChatCompletionRequest
import com.tenutz.firestorecrud.data.ChatCompletionResponse
import com.tenutz.firestorecrud.data.MessageRequest
import com.tenutz.firestorecrud.data.OpenAIApi
import com.tenutz.firestorecrud.ui.base.BaseViewModel
import com.tenutz.firestorecrud.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class NewPostViewModel @Inject constructor(
    private val openAIApi: OpenAIApi,
) : BaseViewModel() {

    private val _imageUri = MutableLiveData<Uri>()
    val imageUri: LiveData<Uri> = _imageUri

    val title = MutableLiveData<String>()
    val content = MutableLiveData<String>()

    fun setImageUri(image: Image) {
        _imageUri.value = image.uri
    }

    fun generateAIPost() {

        val request = ChatCompletionRequest(
            model = "gpt-4o",
            messages = listOf(
                MessageRequest(
                    role = "user",
                    content = """
                        자유 주제로 글을 다음의 형식에 맞춰 작성해줘(답변은 제목, 내용을 vertical bar로 구분하고 제목, 내용 라벨링은 제거)
                    """.trimIndent(),
                )
            ),
        )

        openAIApi.generateText("Bearer ${BuildConfig.OPEN_API_KEY}", request).enqueue(object : Callback<ChatCompletionResponse> {
            override fun onResponse(call: Call<ChatCompletionResponse>, response: Response<ChatCompletionResponse>) {

                if(response.isSuccessful) {

                    val postAI = response.body()
                    Log.d(TAG_POST, "success: $postAI")

                    postAI?.choices?.firstOrNull()?.let {

                        val title = it.message.content.substringBefore("|")
                        val content = it.message.content.substringAfter("|")

                        Log.d(TAG_POST, "title: $title")
                        Log.d(TAG_POST, "content: $content")

                        this@NewPostViewModel.title.value = title
                        this@NewPostViewModel.content.value = content
                    }
                } else {
                    Log.e(TAG_POST, "failure: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<ChatCompletionResponse>, t: Throwable) {
                Log.e(TAG_POST, "failure", t)
            }
        })
    }

    fun newPost() = viewModelScope.launch(Dispatchers.IO) {

        val downloadUri = uploadImageIfExists(imageUri.value)

        val newPost = hashMapOf(
            "uid" to user!!.uid,
            "title" to title.value,
            "content" to content.value,
            "imageName" to imageUri.value?.lastPathSegment,
            "downloadUri" to downloadUri?.await()?.toString(),
            "createdAt" to now,
            "updatedAt" to now,
        )

        db.collection("posts")
            .add(newPost)
            .addOnSuccessListener {
                Log.d(TAG_POST, "Successfully added new post")
                viewEvent(EVENT_NAVIGATE to Unit)
            }
            .addOnFailureListener {
                Log.e(TAG_POST, "Failed to add new post", it)
            }

    }

}