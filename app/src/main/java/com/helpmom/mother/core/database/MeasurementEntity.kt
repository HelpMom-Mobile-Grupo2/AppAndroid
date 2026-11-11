package com.helpmom.mother.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey val id: String,
    val fetalHeartRateBpm: Int,
    val temperatureCelsius: Double,
    val measuredAt: String,
    val healthLight: Int,
    val healthSummary: String
)
