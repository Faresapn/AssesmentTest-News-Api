package com.andro.testassesmentfrontend.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.andro.testassesmentfrontend.data.model.Article
import com.andro.testassesmentfrontend.data.model.Source
import com.andro.testassesmentfrontend.data.repository.NewsRepository
import com.andro.testassesmentfrontend.data.repository.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewsViewModel(private val newsRepository: NewsRepository) : ViewModel() {
    private val _sourcesState = MutableStateFlow<Resource<List<Source>>>(Resource.Loading())
    val sourcesState: StateFlow<Resource<List<Source>>> = _sourcesState.asStateFlow()

    private val _articlesState = MutableStateFlow<Resource<List<Article>>>(Resource.Loading())
    val articlesState: StateFlow<Resource<List<Article>>> = _articlesState.asStateFlow()

    private var currentPage = 1
    private var currentSourceId = ""
    private var isLastPage = false
    private val accumulatedArticles = mutableListOf<Article>()

    fun fetchSourcesByCategory(category: String) {
        viewModelScope.launch {
            _sourcesState.value = Resource.Loading()
            val result = newsRepository.getSources(category)
            _sourcesState.value = result
        }
    }
    class Factory(private val repository: NewsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NewsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NewsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}