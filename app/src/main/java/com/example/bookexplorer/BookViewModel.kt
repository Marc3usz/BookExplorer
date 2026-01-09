package com.example.bookexplorer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class BookViewModel(
    private val repository: BookRepository = BookRepositoryImpl(Api.service)
) : ViewModel() {

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var currentPage = 1
    private var canLoadMore = true
    private var searchJob: Job? = null
    private var isSearchMode = false

    init {
        loadBooks()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()

        if (query.isBlank()) {
            isSearchMode = false
            loadBooks()
        } else {
            isSearchMode = true
            searchJob = viewModelScope.launch {
                delay(500)
                searchBooks(query)
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        isSearchMode = false
        loadBooks()
    }

    private fun searchBooks(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            _isLoading.value = true
            _errorMessage.value = null
            currentPage = 1
            canLoadMore = true

            repository.searchBooks(query, limit = 20, page = 1)
                .onSuccess { bookList ->
                    _books.value = bookList
                    _isLoading.value = false
                    _isSearching.value = false
                    canLoadMore = bookList.isNotEmpty()
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Wystąpił błąd podczas wyszukiwania"
                    _isLoading.value = false
                    _isSearching.value = false
                }
        }
    }

    fun loadBooks() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            currentPage = 1
            canLoadMore = true

            repository.getFictionBooks(limit = 20, page = 1)
                .onSuccess { bookList ->
                    _books.value = bookList
                    _isLoading.value = false
                    canLoadMore = bookList.isNotEmpty()
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Wystąpił błąd"
                    _isLoading.value = false
                }
        }
    }

    fun loadMoreBooks() {
        println("loadMoreBooks called - isLoadingMore: ${_isLoadingMore.value}, isLoading: ${_isLoading.value}, canLoadMore: $canLoadMore, currentPage: $currentPage, isSearchMode: $isSearchMode")

        if (_isLoadingMore.value || _isLoading.value || !canLoadMore) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            val nextPage = currentPage + 1
            println("Fetching page: $nextPage")

            val result = if (isSearchMode) {
                repository.searchBooks(_searchQuery.value, limit = 20, page = nextPage)
            } else {
                repository.getFictionBooks(limit = 20, page = nextPage)
            }

            result
                .onSuccess { bookList ->
                    println("Received ${bookList.size} books for page $nextPage")
                    if (bookList.isEmpty()) {
                        canLoadMore = false
                    } else {
                        currentPage = nextPage
                        _books.value = _books.value + bookList
                        println("Total books now: ${_books.value.size}")
                    }
                    _isLoadingMore.value = false
                }
                .onFailure { exception ->
                    println("Error loading more books: ${exception.message}")
                    _errorMessage.value = exception.message ?: "Wystąpił błąd"
                    _isLoadingMore.value = false
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