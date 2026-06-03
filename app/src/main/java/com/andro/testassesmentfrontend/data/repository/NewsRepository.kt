package com.andro.testassesmentfrontend.data.repository

import com.andro.testassesmentfrontend.BuildConfig
import com.andro.testassesmentfrontend.data.api.NewsApiService
import com.andro.testassesmentfrontend.data.model.ArticlesResponse
import com.andro.testassesmentfrontend.data.model.Source
import com.andro.testassesmentfrontend.utils.NetworkMonitor


sealed class Resource<T> {
    class Success<T>(val data: T) : Resource<T>()
    class Error<T>(val message: String, val data: T? = null) : Resource<T>()
    class Loading<T> : Resource<T>()
}

// --- Repository ---
class NewsRepository(private val apiService: NewsApiService, private val networkMonitor: NetworkMonitor) {

    // Note: In production, inject this via BuildConfig or secure storage
    private val apiKey = BuildConfig.NEWS_API_KEY

    suspend fun getSources(category: String): Resource<List<Source>> {
        return try {
            val response = apiService.getNewsSources(category, apiKey)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.sources)
            } else {
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

    suspend fun getArticles(sourceId: String, page: Int): Resource<ArticlesResponse> {
        // Proactive negative case verification
        if (!networkMonitor.isInternetAvailable()) {
            return Resource.Error("No internet connection. Check your network settings and try again.")
        }

        return try {
            val response = apiService.getArticlesBySource(sourceId, page, apiKey = apiKey)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Server returned error code: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.localizedMessage}")
        }
    }

    suspend fun searchArticles(query: String, page: Int): Resource<ArticlesResponse> {
        return try {
            val response = apiService.searchArticles(query, page, apiKey = apiKey)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }
}