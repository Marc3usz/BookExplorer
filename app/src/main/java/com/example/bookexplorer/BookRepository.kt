package com.example.bookexplorer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface BookRepository {
    suspend fun getFictionBooks(limit: Int = 20): Result<List<Book>>
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
}