package ru.mrfrozzen.newsapp.ui.search

import androidx.lifecycle.*
import ru.mrfrozzen.newsapp.domain.Article
import ru.mrfrozzen.newsapp.domain.ArticleSearchResult
import ru.mrfrozzen.newsapp.network.NewsApiService
import ru.mrfrozzen.newsapp.network.Status
import ru.mrfrozzen.newsapp.network.isNotLoading
import ru.mrfrozzen.newsapp.repository.AppRepository
import ru.mrfrozzen.newsapp.util.AbsentLiveData
import kotlinx.coroutines.launch
import timber.log.Timber

class SearchViewModel(private val appRepository: AppRepository) : ViewModel() {

    private val _articles = MutableLiveData<ArrayList<Article>>()
    val articles: LiveData<ArrayList<Article>>
        get() = _articles

    private val _status = MutableLiveData<Int>(Status.IDLE)
    val status: LiveData<Int>
        get() = _status

    private lateinit var query: String

    private var currentSearchResult: ArticleSearchResult? = null

    private val selectedArticle = MutableLiveData<Article>(null)

    val eventShowPopupMenu = Transformations.switchMap(selectedArticle) {
        if (it != null) {
            liveData {
                if (appRepository.getArticle(it.url) == null) {
                    appRepository.insertArticle(it)
                }
                emit(appRepository.getArticle(it.url))
            }
        } else {
            AbsentLiveData.create()
        }
    }

    fun setSelectedArticle(article: Article?) {
        selectedArticle.value = article!!
    }

    fun onReachEnd() {
        val nextPage = getNextPage()
        if (nextPage != -1) {
            loadNextPage(query, nextPage)
        }
    }

    private fun getNextPage(): Int {
        val currentPage = currentSearchResult?.page ?: return -1
        val totalResult = currentSearchResult?.totalResult ?: return -1
        return if (currentPage * NewsApiService.PAGE_SIZE_DEF_VALUE < totalResult) {
            currentPage + 1
        } else {
            -1
        }
    }

    private fun loadNextPage(query: String, page: Int) {
        viewModelScope.launch {
            if (_status.value!!.isNotLoading()) {
                _status.value = Status.LOADING_MORE
                try {
                    currentSearchResult = appRepository.searchArticles(query, page)
                    val articlesTemp = currentSearchResult!!.articles
                    if (!articlesTemp.isNullOrEmpty()) {
                        _articles.value = ArrayList(_articles.value!!)
                        _articles.value?.addAll(articlesTemp)
                    }
                    _status.value = Status.SUCCESS
                } catch (e: Exception) {
                    _status.value = Status.FAILURE
                    Timber.d("onReachEnd: $e")
                }
            }
        }
    }

    fun searchArticles(query: String) {
        this.query = query
        viewModelScope.launch {
            try {
                _status.value = Status.LOADING
                Timber.d("searchArticles: Searching query: $query")
                _articles.value = ArrayList()
                currentSearchResult = appRepository.searchArticles(query, 1)
                _articles.value = ArrayList(currentSearchResult!!.articles)
                Timber.d("searchArticles: Search finished: $query")
                if (_articles.value.isNullOrEmpty()) {
                    _status.value = Status.NO_RESULT
                } else {
                    _status.value = Status.SUCCESS
                }
            } catch (e: Exception) {
                _status.value = Status.FAILURE
                Timber.d("searchArticles: $e")
            }
        }
    }

    fun addFavoriteArticle(article: Article) {
        viewModelScope.launch {
            appRepository.addFavoriteArticle(article)
        }
    }

    fun removeFavoriteArticle(article: Article) {
        viewModelScope.launch {
            appRepository.removeFavoriteArticle(article)
        }
    }
}

class SearchViewModelFactory(private val appRepository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SearchViewModel(appRepository) as T
    }

}