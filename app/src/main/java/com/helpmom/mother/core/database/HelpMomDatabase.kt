package com.helpmom.mother.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MeasurementEntity::class, SavedArticleEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HelpMomDatabase : RoomDatabase() {
    abstract fun measurementDao(): MeasurementDao
    abstract fun savedArticleDao(): SavedArticleDao
}
