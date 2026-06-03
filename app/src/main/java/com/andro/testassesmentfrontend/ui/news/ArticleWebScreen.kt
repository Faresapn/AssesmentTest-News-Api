package com.andro.testassesmentfrontend.ui.news

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun ArticleWebScreen(url: String) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()

                settings.javaScriptEnabled = true
            }
        },
        update = { webView ->
            webView.loadUrl(url)
        }
    )
}