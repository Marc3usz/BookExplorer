package com.example.bookexplorer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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