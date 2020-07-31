package ru.mrfrozzen.newsapp.ui.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import ru.mrfrozzen.newsapp.R
import ru.mrfrozzen.newsapp.databinding.FavoriteFragmentBinding
import ru.mrfrozzen.newsapp.di.Injection
import ru.mrfrozzen.newsapp.domain.Article
import ru.mrfrozzen.newsapp.ui.common.ArticleAdapter
import ru.mrfrozzen.newsapp.ui.common.OnArticleClickListener
import ru.mrfrozzen.newsapp.util.showPopupMenu
import timber.log.Timber

class FavoriteFragment : Fragment(), OnArticleClickListener {

    private val favoriteViewModel: FavoriteViewModel by viewModels {
        Injection.provideFavoriteViewModelFactory(requireContext())
    }

    private lateinit var binding: FavoriteFragmentBinding

    private lateinit var adapter: ArticleAdapter

    private var itemInsertedOnce = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FavoriteFragmentBinding.inflate(layoutInflater, container, false)
        Timber.d("onCreateView: favorite")
        itemInsertedOnce = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.d("onViewCreated: favorite")

        setupToolbar()

        setupAdapter()

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = favoriteViewModel

            favoriteArticleList.adapter = adapter
        }

        subscribeUi()
    }

    private fun setupToolbar() {
        binding.favoriteToolbar.apply {
            (activity as AppCompatActivity).setSupportActionBar(this)
            setupWithNavController(findNavController())
            title = context.getString(R.string.favorite)
        }
    }

    private fun setupAdapter() {
        adapter = ArticleAdapter(this, ArticleAdapter.SMALL_LAYOUT)

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                Timber.d("favorite onItemRangeInserted: $positionStart $itemCount")
                if (itemInsertedOnce) {
                    // Scroll to top if there is a new article added to favorite
                    binding.favoriteArticleList.scrollToPosition(positionStart)
                } else {
                    itemInsertedOnce = true
                }
            }
        })
    }

    private fun subscribeUi() {
        favoriteViewModel.favoriteArticles.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                adapter.submitList(it)
            }
        })
    }

    override fun onArticleClick(article: Article) {
        findNavController().navigate(
            FavoriteFragmentDirections.actionFavoriteDestToArticleDetailFragment(article)
        )
    }

    override fun onPopupMenuClick(view: View, article: Article) {
        article.showPopupMenu(view, {
            favoriteViewModel.addFavoriteArticle(it)
        }, {
            favoriteViewModel.removeFavoriteArticle(it)
        })
    }

}
