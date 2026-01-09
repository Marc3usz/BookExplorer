package com.example.bookexplorer

import com.google.gson.annotations.SerializedName

data class OpenLibraryResponse(
    @SerializedName("works")
    val books: List<Book>
)

data class Book(
    @SerializedName("key")
    val key: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("authors")
    val authors: List<Author>,
    @SerializedName("cover_id")
    val coverId: Int?,
) {
    fun getMediumCover(): String? {
        return if (coverId != null) "https://covers.openlibrary.org/b/id/$coverId-M.jpg" else null
    }
}

data class AuthorDetailedElement (
    @SerializedName("key")
    val key: String,
)

data class AuthorDetailedResolvedAuthor (
    @SerializedName("name")
    val name: String
)

data class AuthorDetailed (
    @SerializedName("author")
    val author: AuthorDetailedElement,
    @SerializedName("type")
    val type: AuthorDetailedElement,
)

data class BookDetailed(
    @SerializedName("key")
    val key: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("covers")
    val covers: List<Int>,
    @SerializedName("first_publish_date")
    val publishDate: String?,
    @SerializedName("description")
    val description: String?, // nie ma info o ilosci stron
    @SerializedName("authors")
    val _authors: List<AuthorDetailed>,
) {
    fun getLargeCover(): String? {
        val coverId = covers.firstOrNull()
        return if (coverId != null) "https://covers.openlibrary.org/b/id/$coverId-L.jpg" else null
    }
    fun getMediumCover(): String? {
        val coverId = covers.firstOrNull()
        return if (coverId != null) "https://covers.openlibrary.org/b/id/$coverId-M.jpg" else null
    }
}

data class Author(
    @SerializedName("name")
    val name: String
)

data class SearchResponse(
    @SerializedName("numFound") val numFound: Int,
    @SerializedName("start") val start: Int,
    @SerializedName("numFoundExact") val numFoundExact: Boolean,
    @SerializedName("num_found") val numFoundAlt: Int,
    @SerializedName("docs") val docs: List<SearchDoc>
)

data class SearchDoc(
    @SerializedName("key") val key: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("author_key") val authorKey: List<String>?,
    @SerializedName("author_name") val authorName: List<String>?,
    @SerializedName("cover_i") val coverId: Int?,
    @SerializedName("cover_edition_key") val coverEditionKey: String?,
    @SerializedName("edition_count") val editionCount: Int?,
    @SerializedName("first_publish_year") val firstPublishYear: Int?,
    @SerializedName("has_fulltext") val hasFulltext: Boolean?,
    @SerializedName("language") val language: List<String>?,
    @SerializedName("ebook_access") val ebookAccess: String?,
    @SerializedName("public_scan_b") val publicScan: Boolean?
) {
    fun toBook(): Book {
        val workKey = key ?: ""
        return Book(
            key = workKey,
            title = title ?: "Bez tytułu",
            authors = authorName?.map { name -> Author(name = name) } ?: emptyList(),
            coverId = coverId,
        )
    }
}