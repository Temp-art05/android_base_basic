package com.base.app.domain.usecase.post

import com.base.app.domain.model.Post
import com.base.app.domain.repository.PostRepository

class GetPostUseCase(
    private val postRepository: PostRepository,
) {
    suspend operator fun invoke(id: Int): Result<Post> = postRepository.getPost(id)
}
