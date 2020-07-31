package ru.mrfrozzen.newsapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.mrfrozzen.newsapp.domain.Article
import ru.mrfrozzen.newsapp.domain.ArticleFetchResult

/**
 * Room database for this app.
 */
@Database(entities = [Article::class, ArticleFetchResult::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun articleDao(): ArticleDao

    companion object {

        private const val DATABASE_NAME = "app_database"

        // For singleton instantiation
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            synchronized(this) {
                var localInstance = INSTANCE
                if (localInstance == null) {
                    localInstance = Room.databaseBuilder(
                        context,
                        AppDatabase::class.java,
                        DATABASE_NAME
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = localInstance
                }
                return localInstance
            }
        }

    }

}