package com.example.bookexplorer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun BookDetailScreen(
    bookKey: String,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: BookDetailViewModel = viewModel()
) {
    val book by viewModel.book.collectAsState()
    val authors by viewModel.authors.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(bookKey) {
        val fullKey = "/works/$bookKey"
        viewModel.loadBookDetailed(fullKey)
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
                title = book?.title ?: "Szczegóły książki",
                showBackButton = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Błąd: $errorMessage",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = {
                            val fullKey = if (bookKey.startsWith("/works/")) bookKey else "/works/$bookKey"
                            viewModel.loadBookDetailed(fullKey)
                        }) {
                            Text("Spróbuj ponownie")
                        }
                    }
                }
            }

            book != null -> {
                BookDetailContent(
                    book = book,
                    authors = authors,
                    padding = padding
                )
            }
        }
    }
}

@Composable
private fun BookDetailContent(
    book: BookDetailed?,
    authors: List<AuthorDetailedResolvedAuthor>,
    padding: PaddingValues
) {
    book?.let { bookData ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                BookHeaderSection(book = bookData)
            }

            if (authors.isNotEmpty()) {
                item {
                    AuthorsSection(authors = authors)
                }
            }

            bookData.description?.let { description ->
                item {
                    DescriptionSection(description = description)
                }
            }
        }
    }
}

@Composable
private fun BookHeaderSection(book: BookDetailed) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val coverUrl = book.getLargeCover()
        if (coverUrl != null) {
            BookCover(
                url = coverUrl,
                modifier = Modifier
                    .width(120.dp)
                    .height(180.dp)
            )
        } else {
            Surface(
                modifier = Modifier
                    .width(120.dp)
                    .height(180.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Brak okładki",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            book.publishDate?.let { publishDate ->
                Text(
                    text = "Pierwsza publikacja: $publishDate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AuthorsSection(authors: List<AuthorDetailedResolvedAuthor>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (authors.size == 1) "Autor" else "Autorzy",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            authors.forEach { author ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = author.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun DescriptionSection(description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Opis",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}
