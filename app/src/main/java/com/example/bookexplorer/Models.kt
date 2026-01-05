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
    @SerializedName("first_publish_year")
    val firstPublishYear: Int
) {
    fun getMediumCover(): String? {
        return if (coverId != null) "https://covers.openlibrary.org/b/id/$coverId-M.jpg" else null
    }
    fun getLargeCover(): String? {
        return if (coverId != null) "https://covers.openlibrary.org/b/id/$coverId-L.jpg" else null
    }
}

data class Author(
    @SerializedName("name")
    val name: String
)