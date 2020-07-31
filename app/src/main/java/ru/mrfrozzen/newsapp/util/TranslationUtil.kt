package ru.mrfrozzen.newsapp.util

import ru.mrfrozzen.newsapp.NewsApplication
import ru.mrfrozzen.newsapp.R
import ru.mrfrozzen.newsapp.network.NewsApiService

fun String.translate(): String {
    return when (this) {
        NewsApiService.CATEGORY_BUSINESS -> NewsApplication.getString(R.string.business)
        NewsApiService.CATEGORY_ENTERTAINMENT -> NewsApplication.getString(R.string.entertainment)
        NewsApiService.CATEGORY_SPORTS -> NewsApplication.getString(R.string.sports)
        NewsApiService.CATEGORY_TECHNOLOGY -> NewsApplication.getString(R.string.technology)
        NewsApiService.CATEGORY_SCIENCE -> NewsApplication.getString(R.string.science)
        NewsApiService.CATEGORY_HEALTH -> NewsApplication.getString(R.string.health)
        else -> NewsApplication.getString(R.string.general)
    }
}