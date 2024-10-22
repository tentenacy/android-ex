package com.tenutz.firestorecrud.ui.post

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.esafirm.imagepicker.model.Image
import com.tenutz.firestorecrud.data.Post
import com.tenutz.firestorecrud.ui.base.BaseViewModel
import com.tenutz.firestorecrud.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


@HiltViewModel
class PostViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : BaseViewModel() {

    private val _post = MutableLiveData<Post>()
    val post: LiveData<Post> = _post

    val title = MutableLiveData<String>("")
    val content = MutableLiveData<String>("")
    private val _imageUri = MutableLiveData<Uri>(null)
    val imageUri: LiveData<Uri> = _imageUri

    val editable = MutableLiveData<Boolean>(false)
    val me = MutableLiveData<Boolean>(false)

    init {
        val docId = PostFragmentArgs.fromSavedStateHandle(savedStateHandle).docId
        val authorUid = PostFragmentArgs.fromSavedStateHandle(savedStateHandle).authorUid
        me.value = authorUid == auth.uid
        fetchPost(docId)
    }

    fun setImageUri(image: Image) {
        _imageUri.value = image.uri
    }

    fun enterEditModeOrUpdate() {
        if(editable.value == true) {
            updatePost()
        } else {
            enterEditMode()
        }
    }

    private fun enterEditMode() {
        editable.value = true
    }

    fun leaveEditMode() {
        editable.value = false
        _post.value = post.value
        title.value = post.value?.title
        content.value = post.value?.content
        _imageUri.value = post.value?.downloadUri
    }

    private fun updatePost() = viewModelScope.launch(Dispatchers.IO) {

        val downloadUri = if(imageUri.value != post.value?.downloadUri) uploadImageIfExists(imageUri.value) else null

        val postToUpdate = mutableMapOf<String, Any>(
            "title" to title.value!!,
            "content" to content.value!!,
            "imageName" to (imageUri.value?.lastPathSegment ?: ""),
            "updatedAt" to now,
        )
        
        //이미지 변경사항이 있을 때만 업로드하여 게시글에 반영
        downloadUri?.let {
            postToUpdate.put("downloadUri", it.await().toString())
        }

        db.collection("posts")
            .document(post.value!!.docId)
            .update(postToUpdate)
            .addOnSuccessListener {
                Log.d(TAG_POST, "게시글 업데이트 성공")
                viewEvent(EVENT_NAVIGATE to Unit)
            }
            .addOnFailureListener { Log.e(TAG_POST, "게시글 업데이트 실패", it) }
    }

    fun deletePost() {
        db.collection("posts")
            .document(post.value!!.docId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG_POST, "게시글 삭제 성공")
                viewEvent(EVENT_NAVIGATE to Unit)
            }
            .addOnFailureListener { Log.e(TAG_POST, "게시글 삭제 실패", it) }
    }

    private fun fetchPost(docId: String) = viewModelScope.launch(Dispatchers.IO) {
        db.collection("posts")
            .document(docId)
            .get()
            .await()
            .data?.run {

                val authorUid = getOrDefault("uid", "").toString()
                val title = getOrDefault("title", "").toString()
                val content = getOrDefault("content", "").toString()
                val downloadUri = Uri.parse(getOrDefault("downloadUri", "")?.takeIf { it != null }.toString())
                val createdAt = getOrDefault("createdAt", "").toString()
                val updatedAt = getOrDefault("updatedAt", "").toString()

                val authorRef = db.collection("users").document(authorUid)
                val authorData = authorRef.get().await().data
                val authorName = authorData?.getOrDefault("displayName", "").toString()

                _post.postValue(
                    Post(
                        docId,
                        title,
                        content,
                        downloadUri,
                        authorUid,
                        authorName,
                        createdAt,
                        updatedAt,
                    )
                )
                this@PostViewModel.title.postValue(title)
                this@PostViewModel.content.postValue(content)
                this@PostViewModel._imageUri.postValue(downloadUri)
            }
    }
}