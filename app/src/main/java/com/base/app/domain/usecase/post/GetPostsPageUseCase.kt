package com.base.app.domain.usecase.post

import com.base.app.domain.model.Page
import com.base.app.domain.model.Post
import com.base.app.domain.repository.PostRepository

/**
 * SAMPLE — lấy trang kế tiếp của feed. Rule nằm ở đây, không ở ViewModel: kích thước trang, offset của
 * trang kế, khử trùng lặp (BE chèn bài mới có thể làm lệch offset).
 */
class GetPostsPageUseCase(
    private val postRepository: PostRepository,
) {
    /** [loaded] rỗng = trang đầu. Trả về chỉ những post chưa có trong [loaded]. */
    suspend operator fun invoke(loaded: List<Post>): Result<Page<Post>> {
        val loadedIds = loaded.mapTo(HashSet()) { it.id }
        return postRepository.getPosts(skip = loaded.size, limit = PAGE_SIZE)
            .map { page -> page.copy(items = page.items.filterNot { it.id in loadedIds }) }
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}
