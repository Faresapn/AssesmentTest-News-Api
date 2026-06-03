package com.andro.testassesmentfrontend.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.andro.testassesmentfrontend.data.model.Article
import com.andro.testassesmentfrontend.data.repository.NewsRepository
import com.andro.testassesmentfrontend.data.repository.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ArticleViewModel(private val newsRepository: NewsRepository) : ViewModel() {

    private val _articlesState = MutableStateFlow<Resource<List<Article>>>(Resource.Loading())
    val articlesState: StateFlow<Resource<List<Article>>> = _articlesState.asStateFlow()

    private var currentPage = 1
    private var currentSourceId = ""
    private var currentQuery = ""
    private var isLastPage = false
    private val accumulatedArticles = mutableListOf<Article>()
    private var isSearchMode = false

    fun fetchArticlesBySource(sourceId: String, isRefresh: Boolean = false) {
        if (isRefresh) resetPagination()
        if (isLastPage) return

        isSearchMode = false
        currentSourceId = sourceId

        viewModelScope.launch {
            if (currentPage == 1) _articlesState.value = Resource.Loading()

            when (val result = newsRepository.getArticles(sourceId, currentPage)) {
                is Resource.Success -> {
                    val newArticles = result.data.articles
                    if (newArticles.isEmpty()) {
                        isLastPage = true
                    } else {
                        accumulatedArticles.addAll(newArticles)
                        _articlesState.value = Resource.Success(accumulatedArticles.toList())
                        currentPage++
                    }
                }
                is Resource.Error -> _articlesState.value = Resource.Error(result.message)
                is Resource.Loading -> Unit
            }
        }
    }

    fun searchArticles(query: String, isRefresh: Boolean = true) {
        if (query.isBlank()) return
        if (isRefresh) resetPagination()
        if (isLastPage) return

        isSearchMode = true
        currentQuery = query

        viewModelScope.launch {
            if (currentPage == 1) _articlesState.value = Resource.Loading()

            when (val result = newsRepository.searchArticles(query, currentPage)) {
                is Resource.Success -> {
                    val newArticles = result.data.articles
                    if (newArticles.isEmpty()) {
                        isLastPage = true
                    } else {
                        accumulatedArticles.addAll(newArticles)
                        _articlesState.value = Resource.Success(accumulatedArticles.toList())
                        currentPage++
                    }
                }
                is Resource.Error -> _articlesState.value = Resource.Error(result.message)
                is Resource.Loading -> Unit
            }
        }
    }

    fun loadMoreArticles() {
        if (_articlesState.value is Resource.Loading) return
        if (isSearchMode) {
            searchArticles(currentQuery, isRefresh = false)
        } else {
            fetchArticlesBySource(currentSourceId, isRefresh = false)
        }
    }

    private fun resetPagination() {
        currentPage = 1
        isLastPage = false
        accumulatedArticles.clear()
    }

    class Factory(private val repository: NewsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ArticleViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ArticleViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}