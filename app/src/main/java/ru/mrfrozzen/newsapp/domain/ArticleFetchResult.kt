package ru.mrfrozzen.newsapp.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ru.mrfrozzen.newsapp.db.TypeConverter

@Entity
@TypeConverters(TypeConverter::class)
data class ArticleFetchResult(
    @PrimaryKey
    val category: String,
    val articleUrls: List<String>,
    val totalResult: Int,
    val page: Int,
    val firstRetrieved: Long = System.currentTimeMillis(),
    val lastRetrieved: Long = System.currentTimeMillis()
)