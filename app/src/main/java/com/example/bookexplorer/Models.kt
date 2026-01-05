package com.example.bookexplorer

import com.google.gson.annotations.SerializedName

data class OpenLibraryResponse(
    @SerializedName("works")
    val books: List<Book>
)

data class Book(
    @SerializedName("title")
    val title: String,
    @SerializedName("authors")
    val authors: List<Author>,
    @SerializedName("cover_id")
    val coverId: Int?,
    var coverUrl: String?,
    @SerializedName("first_publish_year")
    val firstPublishYear: Int
)

data class Author(
    @SerializedName("name")
    val name: String
)