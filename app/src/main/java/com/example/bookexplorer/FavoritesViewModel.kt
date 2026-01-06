package com.example.bookexplorer

import android.app.Application
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {
    private val favoritesManager = FavoritesManager(application)
    private val bookRepository = BookRepositoryImpl(Api.service)

    private val _favoriteBooks = MutableStateFlow<List<Book>>(emptyList())
    val favoriteBooks: StateFlow<List<Book>> = _favoriteBooks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadFavoriteBooks()
    }

    fun loadFavoriteBooks() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                favoritesManager.favoritesFlow.collectLatest { favoriteKeys ->
                    if (favoriteKeys.isEmpty()) {
                        _favoriteBooks.value = emptyList()
                        _isLoading.value = false
                        return@collectLatest
                    }

                    val books = favoriteKeys.mapNotNull { key ->
                        try {
                            val result = bookRepository.getFictionBookByKey(key)
                            if (result.isFailure) {
                                _errorMessage.value = result.exceptionOrNull()?.message ?: "Nieznany błąd"
                                _isLoading.value = false
                                return@mapNotNull null
                            }

                            val book = result.getOrNull() ?: return@mapNotNull null

                            val fetchedAuthors = book._authors.mapNotNull { authorRole ->
                                if (authorRole.type.key == "/type/author_role") {
                                    bookRepository.getAuthorByKey(authorRole.author.key)
                                } else {
                                    _errorMessage.value = "Nieznany typ autora"
                                    _isLoading.value = false
                                    null
                                }
                            }
                            Book(
                                key = book.key,
                                title = book.title,
                                authors= fetchedAuthors.mapNotNull{ author ->
                                    if (author.isSuccess) {
                                        Author(name = author.getOrNull()!!.name)
                                    } else {
                                        null
                                    }
                               },
                                coverId= book.covers.firstOrNull() ?: 0
                            )
                        } catch (e: Exception) {
                            _errorMessage.value = e.message ?: "Nieznany błąd"
                            _isLoading.value = false
                            null
                        }


                    }

                    _favoriteBooks.value = books
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Nieznany błąd"
                _isLoading.value = false
            }
        }
    }

}