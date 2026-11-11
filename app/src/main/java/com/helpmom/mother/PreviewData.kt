package com.helpmom.mother

import com.helpmom.mother.core.database.MeasurementEntity
import com.helpmom.mother.feature.pregnancy.data.PregnancyDto
import com.helpmom.mother.feature.telemetry.data.TelemetryDto

object PreviewData {
    val pregnancy = PregnancyDto(
        id = "preview-pregnancy",
        motherUserId = "preview-mother",
        lastMenstrualPeriod = "2026-05-10",
        dueDate = "2027-02-14",
        gestationalWeeks = 25,
        gestationalDays = 1
    )
    val measurement = MeasurementEntity(
        id = "preview-measurement",
        fetalHeartRateBpm = 140,
        temperatureCelsius = 36.8,
        measuredAt = "2026-11-03T01:00:00+00:00",
        healthLight = 1,
        healthSummary = "Valores dentro del rango de referencia simulado."
    )
    val telemetry = TelemetryDto(
        id = measurement.id,
        motherUserId = "preview-mother",
        fetalHeartRateBpm = measurement.fetalHeartRateBpm,
        temperatureCelsius = measurement.temperatureCelsius,
        measuredAt = measurement.measuredAt,
        healthLight = measurement.healthLight,
        healthSummary = measurement.healthSummary
    )
}
