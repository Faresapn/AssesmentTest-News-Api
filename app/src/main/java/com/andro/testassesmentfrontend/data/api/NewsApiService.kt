package com.andro.testassesmentfrontend.data.api

import com.andro.testassesmentfrontend.data.model.ArticlesResponse
import com.andro.testassesmentfrontend.data.model.SourcesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("v2/top-headlines/sources")
    suspend fun getNewsSources(
        @Query("category") category: String?,
        @Query("apiKey") apiKey: String
    ): Response<SourcesResponse>

    @GET("v2/everything")
    suspend fun getArticlesBySource(
        @Query("sources") sources: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int = 20,
        @Query("apiKey") apiKey: String
    ): Response<ArticlesResponse>

    @GET("v2/everything")
    suspend fun searchArticles(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int = 20,
        @Query("apiKey") apiKey: String
    ): Response<ArticlesResponse>
}