package com.helpmom.mother.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements ORDER BY measuredAt DESC")
    fun observeMeasurements(): Flow<List<MeasurementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(measurements: List<MeasurementEntity>)

    @Query("DELETE FROM measurements")
    suspend fun clear()

    @Query("DELETE FROM measurements")
    suspend fun deleteAll()

    @androidx.room.Transaction
    suspend fun replaceAll(measurements: List<MeasurementEntity>) {
        clear()
        insertAll(measurements)
    }
}
