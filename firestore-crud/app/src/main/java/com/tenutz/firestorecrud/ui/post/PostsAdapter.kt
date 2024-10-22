package com.tenutz.firestorecrud.ui.post

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tenutz.firestorecrud.data.Post
import com.tenutz.firestorecrud.databinding.ItemPostsBinding

class PostsViewHolder(
    private val binding: ItemPostsBinding,
    private val listener: (String, String) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.post = post
        binding.root.setOnClickListener { listener(post.docId, post.authorUid) }
    }
}

class PostsAdapter(
    private val posts: List<Post>,
    private val listener: (String, String) -> Unit,
): RecyclerView.Adapter<PostsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): PostsViewHolder {
        return PostsViewHolder(ItemPostsBinding.inflate(LayoutInflater.from(parent.context), parent, false), listener)
    }

    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int {
        return posts.size
    }
}