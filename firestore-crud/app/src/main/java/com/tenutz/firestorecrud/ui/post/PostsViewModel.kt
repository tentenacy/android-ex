package com.tenutz.firestorecrud.ui.post

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.Query
import com.tenutz.firestorecrud.data.Post
import com.tenutz.firestorecrud.ui.base.BaseViewModel
import com.tenutz.firestorecrud.util.TAG_POST
import com.tenutz.firestorecrud.util.db
import com.tenutz.firestorecrud.util.user
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class PostsViewModel @Inject constructor() : BaseViewModel() {

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    init {
        fetchPosts()
    }

    fun fetchPosts() = viewModelScope.launch(Dispatchers.IO) {
        _posts.postValue(
            db.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await().documents.mapNotNull { document ->
                    if(document.exists()) {

                        Log.d(TAG_POST, "DocumentSnapshot data: ${document.data}")

                        val docId = document.id
                        val authorUid = document.data?.getOrDefault("uid", "").toString()
                        val title = document.data?.getOrDefault("title", "").toString()
                        val content = document.data?.getOrDefault("content", "").toString()
                        val downloadUri = document.data?.getOrDefault("downloadUri", "").toString()
                        val createdAt = document.data?.getOrDefault("createdAt", "").toString()
                        val updatedAt = document.data?.getOrDefault("updatedAt", "").toString()

                        val authorRef = db.collection("users").document(authorUid)
                        val authorData = authorRef.get().await().data
                        val authorName = authorData?.getOrDefault("displayName", "").toString()

                        Post(docId, title, content, Uri.parse(downloadUri), authorUid, authorName, createdAt, updatedAt)
                    } else {
                        Log.d(TAG_POST, "No such document")
                        null
                    }
            }
        )
    }
}