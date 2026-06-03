package com.andro.testassesmentfrontend.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.andro.testassesmentfrontend.R
import com.andro.testassesmentfrontend.data.api.NetworkModule
import com.andro.testassesmentfrontend.data.repository.NewsRepository
import com.andro.testassesmentfrontend.databinding.ActivityMainBinding
import com.andro.testassesmentfrontend.ui.news.ArticleViewModel
import com.andro.testassesmentfrontend.ui.news.ArticleWebScreen
import com.andro.testassesmentfrontend.ui.news.ArticlesScreen
import com.andro.testassesmentfrontend.ui.news.NewsScreen
import com.andro.testassesmentfrontend.ui.news.NewsViewModel
import com.andro.testassesmentfrontend.utils.NetworkMonitor
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: NewsViewModel
    private lateinit var articleViewModel: ArticleViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val apiService = NetworkModule.apiService
        val networkMonitor = NetworkMonitor(this)
        val repository = NewsRepository(apiService,networkMonitor)
        val factory = NewsViewModel.Factory(repository)
        viewModel = ViewModelProvider(this, factory)[NewsViewModel::class.java]
        articleViewModel = ViewModelProvider(this, ArticleViewModel.Factory(repository))[ArticleViewModel::class.java]
        binding.composeView.setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "sources_screen") {

                // Screen 1: News Categories & Sources
                composable("sources_screen") {
                    NewsScreen(
                        viewModel = viewModel,
                        onSourceClick = { sourceId ->
                            // Navigate to the articles screen, passing the selected source ID
                            navController.navigate("articles_screen/$sourceId")
                        }
                    )
                }

                // Screen 2: News Articles List with Search and Infinite Scrolling
                composable(
                    route = "articles_screen/{sourceId}",
                    arguments = listOf(navArgument("sourceId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val sourceId = backStackEntry.arguments?.getString("sourceId") ?: ""
                    ArticlesScreen(
                        viewModel = articleViewModel,
                        sourceId = sourceId,
                        onArticleClick = { articleUrl ->
                            // URLs contain specialized symbols, so encode it cleanly prior to navigating
                            val encodedUrl = URLEncoder.encode(articleUrl, StandardCharsets.UTF_8.toString())
                            navController.navigate("web_screen/$encodedUrl")
                        }
                    )
                }

                // Screen 3: WebView Details
                composable(
                    route = "web_screen/{url}",
                    arguments = listOf(navArgument("url") { type = NavType.StringType })
                ) { backStackEntry ->
                    val url = backStackEntry.arguments?.getString("url") ?: ""
                    ArticleWebScreen(url = url)
                }
            }
        }
    }
}