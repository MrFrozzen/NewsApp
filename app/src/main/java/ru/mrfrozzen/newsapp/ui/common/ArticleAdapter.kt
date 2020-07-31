package ru.mrfrozzen.newsapp.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.mrfrozzen.newsapp.R
import ru.mrfrozzen.newsapp.databinding.ArticleItemBinding
import ru.mrfrozzen.newsapp.databinding.SmallArticleItemBinding
import ru.mrfrozzen.newsapp.domain.Article
import ru.mrfrozzen.newsapp.util.toPrettyTime

class ArticleAdapter(
    private val onArticleClickListener: OnArticleClickListener,
    private val layoutType: Int = NORMAL_LAYOUT
) :
    ListAdapter<Article, AbstractArticleViewHolder>(ArticleDiffCallBack()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractArticleViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (layoutType) {
            SMALL_LAYOUT -> {
                SmallArticleViewHolder(
                    SmallArticleItemBinding.inflate(layoutInflater, parent, false),
                    onArticleClickListener
                )
            }
            else -> {
                ArticleViewHolder(
                    ArticleItemBinding.inflate(layoutInflater, parent, false),
                    onArticleClickListener
                )
            }
        }
    }

    override fun onBindViewHolder(holder: AbstractArticleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        const val SMALL_LAYOUT = 1
        const val NORMAL_LAYOUT = 0
    }
}

sealed class AbstractArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(article: Article)
}

class ArticleViewHolder(
    private val binding: ArticleItemBinding,
    private val onArticleClickListener: OnArticleClickListener
) : AbstractArticleViewHolder(binding.root) {

    override fun bind(article: Article) {
        binding.apply {
            articleItemTitle.text = article.title
            articleItemDescription.text = article.description
            articleItemSource.text = article.source?.name
            articleItemPublishedAt.text = article.publishedAt?.toPrettyTime()
            Glide.with(root).load(article.urlToImage).placeholder(
                articleItemImage.context.resources.getDrawable(
                    R.drawable.image_placeholder
                )
            ).into(articleItemImage)
            articleItemMoreButton.setOnClickListener {
                onArticleClickListener.onPopupMenuClick(it, article)
            }
            root.setOnClickListener {
                onArticleClickListener.onArticleClick(article)
            }
        }
    }
}

class SmallArticleViewHolder(
    private val binding: SmallArticleItemBinding,
    private val onArticleClickListener: OnArticleClickListener
) : AbstractArticleViewHolder(binding.root) {

    override fun bind(article: Article) {
        binding.apply {
            smallArticleItemTitle.text = article.title
            smallArticleItemSource.text = article.source?.name
            smallArticleItemPublishedAt.text = article.publishedAt?.toPrettyTime()
            Glide.with(root).load(article.urlToImage).placeholder(
                smallArticleItemImage.context.resources.getDrawable(
                    R.drawable.image_placeholder
                )
            ).into(smallArticleItemImage)

            smallArticleItemMoreButton.setOnClickListener {
                onArticleClickListener.onPopupMenuClick(it, article)
            }

            root.setOnClickListener {
                onArticleClickListener.onArticleClick(article)
            }
        }
    }

}

class ArticleDiffCallBack : DiffUtil.ItemCallback<Article>() {
    override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem.url == newItem.url
    }

    override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem.favorite == newItem.favorite
    }
}

interface OnArticleClickListener {
    fun onArticleClick(article: Article)
    fun onPopupMenuClick(view: View, article: Article)
}