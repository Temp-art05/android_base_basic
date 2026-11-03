package com.base.app.data.repository

import com.base.app.data.mapper.toDomain
import com.base.app.data.remote.datasource.PostRemoteDataSource
import com.base.app.data.remote.error.toDomainException
import com.base.app.data.util.mapError
import com.base.app.data.util.withCatching
import com.base.app.domain.model.Page
import com.base.app.domain.model.Post
import com.base.app.domain.repository.PostRepository

/**
 * SAMPLE — repository là chỗ DUY NHẤT đổi thread + bắt exception (`withCatching`) và dịch lỗi hạ tầng
 * sang `DomainException` (`toDomainException`).
 */
class PostRepositoryImpl(
    private val postRemoteDataSource: PostRemoteDataSource,
) : PostRepository {

    override suspend fun getPosts(skip: Int, limit: Int): Result<Page<Post>> = withCatching {
        postRemoteDataSource.getPosts(skip = skip, limit = limit).toDomain()
    }.mapError { it.toDomainException() }

    override suspend fun getPost(id: Int): Result<Post> = withCatching {
        postRemoteDataSource.getPost(id).toDomain()
    }.mapError { it.toDomainException() }
}
