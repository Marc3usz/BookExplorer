package com.example.bookexplorer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookViewModel(
    private val repository: BookRepository = BookRepositoryImpl(Api.service)
) : ViewModel() {

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.getFictionBooks(limit = 20)
                .onSuccess { bookList ->
                    _books.value = bookList
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Wystąpił błąd"
                    _isLoading.value = false
                }
        }
    }
}

class BookDetailViewModel(
    private val repository: BookRepository = BookRepositoryImpl(Api.service)
) : ViewModel() {

    private val _book = MutableStateFlow<BookDetailed?>(null)
    val book: StateFlow<BookDetailed?> = _book.asStateFlow()

    private val _authors = MutableStateFlow<List<AuthorDetailedResolvedAuthor>>(emptyList())
    val authors: StateFlow<List<AuthorDetailedResolvedAuthor>> = _authors.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadBookDetailed(bookKey: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _authors.value = emptyList()
            var authorKeys: List<AuthorDetailed> = emptyList()

            repository.getFictionBookByKey(bookKey)
                .onSuccess { book ->
                    _book.value = book
                    authorKeys = book._authors
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Wystąpił błąd"
                    _isLoading.value = false
                }

            val authorsList = mutableListOf<AuthorDetailedResolvedAuthor>()

            for (author in authorKeys) {
                if (!_isLoading.value) break


                if (author.type.key == "/type/author_role") {
                    repository.getAuthorByKey(author.author.key)
                        .onSuccess { authorDetailedResolvedAuthor ->
                            authorsList.add(authorDetailedResolvedAuthor)
                            _authors.value = authorsList.toList()
                        }
                        .onFailure { exception ->
                            _errorMessage.value = exception.message ?: "Wystąpił błąd podczas pobierania autora"
                            _isLoading.value = false
                        }
                }
            }

            _isLoading.value = false
        }
    }
}