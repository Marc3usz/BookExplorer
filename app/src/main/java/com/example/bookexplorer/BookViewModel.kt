package com.example.bookexplorer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BookViewModel : ViewModel() {
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
            try {
                val response = Api.service.getFictionBooks(limit = 20)
                _books.value = response.books.map { book -> 
                    val coverId = book.coverId
                    Book(
                        title = book.title,
                        authors = book.authors,
                        coverId = coverId,
                        coverUrl = "https://covers.openlibrary.org/b/id/$coverId-M.jpg",
                        firstPublishYear = book.firstPublishYear
                    )
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Wystąpił błąd"
                _isLoading.value = false
            }
        }
    }
}