package ru.mrfrozzen.newsapp.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.mrfrozzen.newsapp.databinding.SearchFragmentBinding
import ru.mrfrozzen.newsapp.di.Injection
import ru.mrfrozzen.newsapp.domain.Article
import ru.mrfrozzen.newsapp.ui.common.ArticleAdapter
import ru.mrfrozzen.newsapp.ui.common.OnArticleClickListener
import ru.mrfrozzen.newsapp.util.showPopupMenu
import timber.log.Timber

class SearchFragment : Fragment(), OnArticleClickListener {

    private lateinit var binding: SearchFragmentBinding

    private lateinit var adapter: ArticleAdapter

    private val searchViewModel: SearchViewModel by viewModels {
        Injection.provideSearchViewModelFactory(requireContext())
    }

    private var lastQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = SearchFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    private var currentPopupView: View? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ArticleAdapter(this, ArticleAdapter.SMALL_LAYOUT)

        binding.apply {
            viewModel = searchViewModel
            lifecycleOwner = viewLifecycleOwner

            searchArticleList.adapter = adapter

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        searchViewModel.searchArticles(query)
                        lastQuery = query
                    }
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }
            })

            searchArticleList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastPosition = layoutManager.findLastVisibleItemPosition()
                    if (lastPosition == adapter.itemCount - 1 && adapter.itemCount > 0) {
                        searchViewModel.onReachEnd()
                    }
                }
            })

            searchRetryButton.setOnClickListener {
                searchViewModel.searchArticles(lastQuery)
            }
        }

        subscribeUi()
    }

    private fun subscribeUi() {
        searchViewModel.apply {
            articles.observe(viewLifecycleOwner, Observer {
                it?.let {
                    Timber.d("onViewCreated: ${it.size}")
                    adapter.submitList(it)
                }
            })

            eventShowPopupMenu.observe(viewLifecycleOwner, Observer { article ->
                if (article != null) {
                    currentPopupView?.let {
                        article.showPopupMenu(it, {
                            searchViewModel.addFavoriteArticle(article)
                        }, {
                            searchViewModel.removeFavoriteArticle(article)
                        })
                    }
                    currentPopupView = null
                    searchViewModel.setSelectedArticle(null)
                }
            })

            status.observe(viewLifecycleOwner, Observer {
                it?.let {

                }
            })
        }
    }

    override fun onArticleClick(article: Article) {
        findNavController().navigate(
            SearchFragmentDirections.actionSearchDestToArticleDetailDest(
                article
            )
        )
    }

    override fun onPopupMenuClick(view: View, article: Article) {
        currentPopupView = view
        searchViewModel.setSelectedArticle(article)
    }

}
