package com.helpmom.mother.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_articles")
data class SavedArticleEntity(
    @PrimaryKey val articleId: String,
    val title: String,
    val category: String,
    val weekStart: Int,
    val weekEnd: Int,
    val summary: String,
    val imageUrl: String,
    val body: String
)
