package com.helpmom.mother.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedArticleDao {
    @Query("SELECT * FROM saved_articles ORDER BY title")
    fun observeSavedArticles(): Flow<List<SavedArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(article: SavedArticleEntity)

    @Query("DELETE FROM saved_articles WHERE articleId = :articleId")
    suspend fun remove(articleId: String)

    @Query("DELETE FROM saved_articles")
    suspend fun clear()
}
