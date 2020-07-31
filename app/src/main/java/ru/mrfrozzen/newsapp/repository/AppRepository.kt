package ru.mrfrozzen.newsapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import ru.mrfrozzen.newsapp.db.AppDatabase
import ru.mrfrozzen.newsapp.domain.Article
import ru.mrfrozzen.newsapp.domain.ArticleFetchResult
import ru.mrfrozzen.newsapp.domain.ArticleSearchResult
import ru.mrfrozzen.newsapp.network.NewsApiService
import ru.mrfrozzen.newsapp.util.AbsentLiveData
import ru.mrfrozzen.newsapp.util.clean
import ru.mrfrozzen.newsapp.util.getContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class AppRepository(
    appDatabase: AppDatabase,
    private val newsApiService: NewsApiService
) {

    private val articleDao = appDatabase.articleDao()

    fun getArticles(category: String): LiveData<List<Article>> {
        return Transformations.switchMap(articleDao.getArticles(category)) { articleFetchResult ->
            if (articleFetchResult != null) {
                articleDao.getArticlesByUrlsOrdered(articleFetchResult.articleUrls)
            } else {
                AbsentLiveData.create()
            }
        }
    }

    fun getFavoriteArticles(): LiveData<List<Article>> {
        return articleDao.getFavoriteArticles()
    }

    suspend fun getArticleFetchResult(category: String): ArticleFetchResult? =
        withContext(Dispatchers.IO) {
            return@withContext articleDao.getArticleFetchResult(category)
        }

    suspend fun refreshArticles(category: String) {
        withContext(Dispatchers.IO) {
            Timber.d("refreshArticles: getting articles $category from network...")
            val articlesContainer =
                newsApiService.getArticles(category = category, page = 1)
            val articles = articlesContainer.articles
            articles.clean()
            Timber.d(
                "refreshArticles: articles from network: \n\t${articles[0]}\n\t${articles[1]}"
            )
            Timber.d("refreshArticles: clearing database...")

            Timber.d("refreshArticles: inserting articles to database...")
            articleDao.insertArticles(articles)
            articleDao.insertArticleFetchResult(
                ArticleFetchResult(category, articles.map {
                    it.url
                }, articlesContainer.totalResults, 1)
            )
            Timber.d("refreshArticles: done")
        }
    }

    suspend fun loadNextPage(category: String, page: Int) {
        Timber.d("loadNextPage: $category $page")
        withContext(Dispatchers.IO) {
            val articlesContainer =
                newsApiService.getArticles(category = category, page = page)
            val articles = articlesContainer.articles
            articles.clean()
            articleDao.insertArticles(articles)

            // Merge result with previous result
            val previousResult = articleDao.getArticleFetchResult(category)
            if (previousResult != null) {
                val urls = ArrayList(previousResult.articleUrls)
                urls.addAll(articlesContainer.articles.map { article ->
                    article.url
                })
                articleDao.insertArticleFetchResult(
                    ArticleFetchResult(category, urls, articlesContainer.totalResults, page)
                )
            }
        }
    }

    suspend fun addFavoriteArticle(article: Article) {
        withContext(Dispatchers.IO) {
            article.favorite = System.currentTimeMillis()
            articleDao.insertArticle(article)
        }
    }

    suspend fun removeFavoriteArticle(article: Article) {
        withContext(Dispatchers.IO) {
            article.favorite = 0
            articleDao.insertArticle(article)
        }
    }

    suspend fun insertArticle(article: Article) {
        withContext(Dispatchers.IO) {
            articleDao.insertArticle(article)
        }
    }

    suspend fun searchArticles(query: String, page: Int) = withContext(Dispatchers.IO) {
        val articlesNetworkContainer =
            newsApiService.searchArticles(query = query, page = page)
        val articles = articlesNetworkContainer.articles
        articles.clean()
        return@withContext ArticleSearchResult(
            query,
            articles,
            articlesNetworkContainer.totalResults,
            page
        )
    }

    suspend fun getFullContent(article: Article) = withContext(Dispatchers.IO) {
        if (article.content != null) {
            val html = newsApiService.getHtml(article.url)
            article.fullContent = html.getContent(article.content!!)
            articleDao.insertArticle(article)
        }
    }

    fun getArticleLd(url: String): LiveData<Article> = articleDao.getArticleLd(url)

    suspend fun getArticle(url: String): Article? = withContext(Dispatchers.IO) {
        return@withContext articleDao.getArticle(url)
    }

    companion object {

        @Volatile
        private var INSTANCE: AppRepository? = null

        fun getRepository(
            appDatabase: AppDatabase,
            newsApiService: NewsApiService
        ): AppRepository {
            synchronized(this) {
                var localInstance = INSTANCE
                if (localInstance == null) {
                    localInstance = AppRepository(appDatabase, newsApiService)
                    INSTANCE = localInstance
                }
                return localInstance
            }
        }

    }

}