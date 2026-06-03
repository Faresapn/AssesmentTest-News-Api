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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andro.testassesmentfrontend.data.model.Source
import com.andro.testassesmentfrontend.data.repository.Resource


val newsCategories = listOf(
    "Business", "Entertainment", "General", "Health", "Science", "Sports", "Technology"
)

@Composable
fun NewsScreen (
    viewModel: NewsViewModel,
    onSourceClick: (String) -> Unit
){
    val sourcesState by viewModel.sourcesState.collectAsState()
    var selectedCategory by remember { mutableStateOf(newsCategories[0].lowercase()) }
    LaunchedEffect(selectedCategory) {
        viewModel.fetchSourcesByCategory(selectedCategory)
    }
    NewsContent(
        sourcesState = sourcesState,
        selectedCategory = selectedCategory,
        onCategorySelect = { category -> selectedCategory = category },
        onSourceClick = onSourceClick,
        onRetry = { viewModel.fetchSourcesByCategory(selectedCategory) }
    )
}

@Composable
fun NewsContent(
    sourcesState: Resource<List<Source>>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    onSourceClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(newsCategories) { category ->
                val isSelected = category.lowercase() == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelect(category.lowercase()) },
                    label = { Text(category) }
                )
            }
        }

        Divider()
        Box(modifier = Modifier.fillMaxSize()) {
            when (sourcesState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Error -> {
                    ErrorState(
                        message = sourcesState.message ?: "Network error occurred.",
                        onRetry = onRetry
                    )
                }
                is Resource.Success -> {
                    val sources = sourcesState.data ?: emptyList()
                    if (sources.isEmpty()) {
                        EmptyState()
                        } else {
                        LazyColumn(
                            contentPadding = PaddingValues(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(sources) { source ->
                                SourceItem(source = source, onClick = { onSourceClick(source.id) })
                            }
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun SourceItem(source: Source, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = source.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = source.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
        }
    }
}

@Preview(showBackground = true, name = "Full Screen Preview")
@Composable
fun NewsScreenFullPreview() {
    val dummySources = listOf(
        Source("1", "BBC News", "International news coverage.", "general"),
        Source("2", "TechCrunch", "Startup and technology insights.", "technology"),
        Source("3", "Bloomberg", "Business and financial market updates.", "business")
    )

    MaterialTheme {
        NewsContent(
            sourcesState = Resource.Success(dummySources),
            selectedCategory = "business",
            onCategorySelect = {},
            onSourceClick = {},
            onRetry = {}
        )
    }
}

