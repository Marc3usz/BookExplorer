package com.example.bookexplorer

import android.graphics.Bitmap
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.json.JSONObject
import java.net.URL

data class Book(
    val title: String,
    val author: String,
    val coverId: Int?,
    val coverUrl: String? = coverId?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" }
)

class HomeScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Replace this with your actual theme name if different
            MaterialTheme {
                HomeScreen(onBookClick = { book ->
                    // Handle navigation or clicks here
                })
            }
        }
    }
}

@Composable
fun HomeScreen(onBookClick: (Book) -> Unit) {
    var books by remember { mutableStateOf<List<Book>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun loadBooks() {
        isLoading = true
        errorMessage = null
        Thread {
            try {
                val json = URL("https://openlibrary.org/subjects/fiction.json?limit=20").readText()
                val jsonObject = JSONObject(json)
                val worksArray = jsonObject.getJSONArray("works")

                books = (0 until worksArray.length()).map { i ->
                    val work = worksArray.getJSONObject(i)
                    val title = work.getString("title")
                    val authorsArray = work.getJSONArray("authors")
                    val author = if (authorsArray.length() > 0) {
                        authorsArray.getJSONObject(0).getString("name")
                    } else {
                        "Nieznany autor"
                    }
                    val coverId = work.optInt("cover_id").takeIf { it != 0 }

                    Book(title, author, coverId)
                }
                isLoading = false
            } catch (e: Exception) {
                errorMessage = e.message ?: "Wystąpił błąd"
                isLoading = false
            }
        }.start()
    }

    LaunchedEffect(Unit) {
        loadBooks()
    }

    Scaffold(
        topBar = {
            Text("Biblioteka książek")
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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Błąd: $errorMessage",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { loadBooks() }) {
                            Text("Spróbuj ponownie")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(books) { book ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onBookClick(book) },
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (book.coverUrl != null) {
                                    BookCover(url = book.coverUrl)
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(120.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Brak okładki")
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = book.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = book.author,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookCover(url: String) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(url) {
        Thread {
            try {
                val connection = URL(url).openConnection()
                connection.connect()
                val inputStream = connection.getInputStream()
                bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .width(80.dp)
                .height(120.dp),
            contentScale = ContentScale.Crop
        )
    } ?: Box(
        modifier = Modifier
            .width(80.dp)
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp))
    }
}