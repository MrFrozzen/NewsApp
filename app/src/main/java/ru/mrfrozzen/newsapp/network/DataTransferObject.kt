package ru.mrfrozzen.newsapp.network

import ru.mrfrozzen.newsapp.domain.Article

/**
 * Simple object to hold News API responses.
 */
data class ArticlesNetworkContainer(
    val totalResults: Int,
    val articles: List<Article>
)
