package com.example.bookexplorer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface BookRepository {
    suspend fun getFictionBooks(limit: Int = 20): Result<List<Book>>
    suspend fun getFictionBookByKey(key: String): Result<BookDetailed>
    suspend fun getAuthorByKey(key: String): Result<AuthorDetailedResolvedAuthor>
}

class BookRepositoryImpl(
    private val apiService: OpenLibraryApi
) : BookRepository {

    override suspend fun getFictionBooks(limit: Int): Result<List<Book>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getFictionBooks(limit)
                Result.success(response.books)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getFictionBookByKey(key: String): Result<BookDetailed> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getFictionBookByKey(key)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getAuthorByKey(key: String): Result<AuthorDetailedResolvedAuthor> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAuthorByKey(key)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}