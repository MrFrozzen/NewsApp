package ru.mrfrozzen.newsapp.di

import android.content.Context
import ru.mrfrozzen.newsapp.db.AppDatabase
import ru.mrfrozzen.newsapp.domain.Article
import ru.mrfrozzen.newsapp.network.NewsApiService
import ru.mrfrozzen.newsapp.repository.AppRepository
import ru.mrfrozzen.newsapp.ui.common.ArticleDetailViewModelFactory
import ru.mrfrozzen.newsapp.ui.favorite.FavoriteViewModelFactory
import ru.mrfrozzen.newsapp.ui.home.ArticleViewModelFactory
import ru.mrfrozzen.newsapp.ui.search.SearchViewModelFactory

/**
 * Manual dependency injection.
 */
object Injection {

    private fun provideDatabase(context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    private fun provideNewsApiService(): NewsApiService {
        return NewsApiService.getService()
    }

    private fun provideRepository(context: Context): AppRepository {
        return AppRepository.getRepository(provideDatabase(context), provideNewsApiService())
    }

    fun provideArticleViewModelFactory(
        context: Context,
        category: String
    ): ArticleViewModelFactory {
        return ArticleViewModelFactory(provideRepository(context), category)
    }

    fun provideSearchViewModelFactory(
        context: Context
    ): SearchViewModelFactory {
        return SearchViewModelFactory(provideRepository(context))
    }

    fun provideFavoriteViewModelFactory(
        context: Context
    ): FavoriteViewModelFactory {
        return FavoriteViewModelFactory(provideRepository(context))
    }

    fun provideArticleDetailViewModelFactory(
        context: Context, article: Article
    ): ArticleDetailViewModelFactory {
        return ArticleDetailViewModelFactory(provideRepository(context), article)
    }
}