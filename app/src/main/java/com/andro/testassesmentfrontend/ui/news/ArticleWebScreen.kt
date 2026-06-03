package com.andro.testassesmentfrontend.ui.news

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun ArticleWebScreen(url: String) {
    // AndroidView acts as the interop bridge for traditional XML views
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                // Ensure links open inside our application instead of launching Chrome
                webViewClient = WebViewClient()

                // Security tip: Only enable JavaScript if strictly necessary for the site layout
                settings.javaScriptEnabled = true
            }
        },
        update = { webView ->
            webView.loadUrl(url)
        }
    )
}