package com.example.bookexplorer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface BookRepository {
    suspend fun getFictionBooks(limit: Int = 20, page: Int = 0): Result<List<Book>>
    suspend fun getFictionBookByKey(key: String): Result<BookDetailed>
    suspend fun getAuthorByKey(key: String): Result<AuthorDetailedResolvedAuthor>
    suspend fun searchBooks(query: String, limit: Int = 20, page: Int = 0): Result<List<Book>>
}

class BookRepositoryImpl(
    private val apiService: OpenLibraryApi
) : BookRepository {

    override suspend fun getFictionBooks(limit: Int, page: Int): Result<List<Book>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getFictionBooks(limit, page * limit)
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

    override suspend fun searchBooks(query: String, limit: Int, page: Int): Result<List<Book>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.searchBooks(query, limit, page * limit)
                Result.success(response.docs.map { it.toBook() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}