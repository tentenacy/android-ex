package com.tenutz.firestorecrud.data

import android.net.Uri

data class Post(
    val docId: String,
    val title: String,
    val content: String,
    val downloadUri: Uri,
    val authorUid: String,
    val author: String,
    val createdAt: String,
    val updatedAt: String,
)
