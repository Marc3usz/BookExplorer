package com.example.bookexplorer

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface OpenLibraryApi {
    @GET("subjects/fiction.json")
    suspend fun getFictionBooks(@Query("limit") limit: Int = 20): OpenLibraryResponse
    @GET("{key}.json")
    suspend fun getFictionBookByKey(@Path("key") key: String): BookDetailed
    @GET("{key}.json")
    suspend fun getAuthorByKey(@Path("key") key: String): AuthorDetailedResolvedAuthor
}

object Api {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://openlibrary.org/")
        .client(
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: OpenLibraryApi = retrofit.create(OpenLibraryApi::class.java)
}