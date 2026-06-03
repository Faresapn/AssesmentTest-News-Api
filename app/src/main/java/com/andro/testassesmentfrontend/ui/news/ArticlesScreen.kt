package com.andro.testassesmentfrontend.ui.news

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andro.testassesmentfrontend.data.model.Article
import com.andro.testassesmentfrontend.data.model.SourceInfo
import com.andro.testassesmentfrontend.data.repository.Resource

@Composable
fun ArticlesScreen(
    viewModel: ArticleViewModel,
    sourceId: String,
    onArticleClick: (String) -> Unit
) {
    val articlesState by viewModel.articlesState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Fetch initial list when screen mounts
    LaunchedEffect(sourceId) {
        viewModel.fetchArticlesBySource(sourceId, isRefresh = true)
    }

    ArticlesContent(
        articlesState = articlesState,
        searchQuery = searchQuery,
        onSearchQueryChange = { query ->
            searchQuery = query
            if (query.isNotBlank()) {
                viewModel.searchArticles(query, isRefresh = true)
            }
        },
        onLoadMore = {
            viewModel.loadMoreArticles()
        },
        onArticleClick = onArticleClick
    )
}

// --- 2. STATELESS LAYER (UI Renderer & Preview Target) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesContent(
    articlesState: Resource<List<Article>>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onLoadMore: () -> Unit,
    onArticleClick: (String) -> Unit
) {
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar Header
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search articles...") },
            leadingIcon = { Text("🔍") },
            singleLine = true
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when (articlesState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Error -> {
                    ErrorState(
                        message = articlesState.message ?: "Network error occurred.",
                        onRetry = onLoadMore // Triggers a reload call
                    )
                }
                is Resource.Success -> {
                    val articles = articlesState.data ?: emptyList()
                    if (articles.isEmpty()) {
                        EmptyState()
                    } else {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(articles) { index, article ->
                                // Endless Scrolling trigger point
                                if (index >= articles.size - 3) {
                                    LaunchedEffect(articles.size) {
                                        onLoadMore()
                                    }
                                }
                                ArticleItem(article = article, onClick = { onArticleClick(article.url) })
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 3. REUSABLE LIST ITEM CARD ---
@Composable
fun ArticleItem(article: Article, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = article.description ?: "No description available.",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Published at: ${article.publishedAt.take(10)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

// --- 4. THE ARTICLES PREVIEW ENGINE ---
@Preview(showBackground = true, name = "Articles Screen Design")
@Composable
fun ArticlesScreenPreview() {
    // Generate isolated dummy articles list matching your backend data models
    val mockSource = SourceInfo("techcrunch", "TechCrunch")
    val dummyArticles = listOf(
        Article(
            source = mockSource,
            author = "Faresa",
            title = "Jetpack Compose Migrations Accelerated in Modern Projects",
            description = "Developers are finding massive productivity gains by dropping traditional XML view layouts for modular declarative code architectures.",
            url = "https://kotlinlang.org",
            urlToImage = null,
            publishedAt = "2026-06-03T12:00:00Z",
            content = null
        ),
        Article(
            source = mockSource,
            author = "Android Team",
            title = "Kotlin 2.0 Compiler Changes Everything for Mobile Frameworks",
            description = "The new K2 compilation framework speeds up system indexing pipelines and moves compose processing architectures into standalone Gradle plugins.",
            url = "https://developer.android.com",
            urlToImage = null,
            publishedAt = "2026-06-03T15:30:00Z",
            content = null
        )
    )

    MaterialTheme {
        // Call your stateless container passing the mock success results directly
        ArticlesContent(
            articlesState = Resource.Success(dummyArticles),
            searchQuery = "",
            onSearchQueryChange = {},
            onLoadMore = {},
            onArticleClick = {}
        )
    }
}