package com.helpmom.mother.feature.pregnancy.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class HealthLight(val code: Int) {
    Green(1), Yellow(2), Red(3);
    companion object { fun fromCode(code: Int) = entries.firstOrNull { it.code == code } ?: Green }
}

enum class SymptomSeverity(val code: Int) {
    Mild(1), Moderate(2), Severe(3);
    companion object { fun fromCode(code: Int) = entries.firstOrNull { it.code == code } ?: Mild }
}

enum class TriageUrgency(val code: Int) {
    SelfCare(1), ContactCareTeam(2), Emergency(3);
    companion object { fun fromCode(code: Int?) = entries.firstOrNull { it.code == code } }
}

enum class AlertType(val code: Int) {
    Craving(1), TiredOrPain(2), ComeHere(3), Sos(4);
    companion object { fun fromCode(code: Int) = entries.firstOrNull { it.code == code } ?: Sos }
}

enum class AlertStatus(val code: Int) {
    Pending(1), Responded(2);
    companion object { fun fromCode(code: Int) = entries.firstOrNull { it.code == code } ?: Pending }
}

enum class AlertResponse(val code: Int) {
    OnMyWay(1), CallingNow(2), Done(3);
    companion object { fun fromCode(code: Int?) = entries.firstOrNull { it.code == code } }
}

enum class MessageAuthor(val code: Int) {
    Mother(1), Assistant(2);
    companion object { fun fromCode(code: Int) = entries.firstOrNull { it.code == code } ?: Mother }
}

fun responseLabel(response: Int?): String = when (response) {
    1 -> "Voy en camino 🚗"
    2 -> "Te llamo ahora 📞"
    3 -> "Listo ✓"
    else -> ""
}

fun urgencyLabel(urgency: Int?): String = when (urgency) {
    1 -> "Autocuidado"
    2 -> "Consulta a tu equipo"
    3 -> "Emergencia"
    else -> ""
}

data class BabyFruitInfo(
    val fruitName: String,
    val weightText: String,
    val lengthText: String,
    val emoji: String
)

fun babyFruitInfo(weeks: Int): BabyFruitInfo = when {
    weeks <= 4 -> BabyFruitInfo("semilla de amapola", "1 g", "0.2 cm", "🫐")
    weeks <= 8 -> BabyFruitInfo("frambuesa", "1 g", "1.6 cm", "🍓")
    weeks <= 12 -> BabyFruitInfo("ciruela", "14 g", "5.4 cm", "🫐")
    weeks <= 16 -> BabyFruitInfo("manzana", "100 g", "11.6 cm", "🍎")
    weeks <= 20 -> BabyFruitInfo("mango", "300 g", "25 cm", "🥭")
    weeks <= 24 -> BabyFruitInfo("papaya", "600 g", "30 cm", "🍈")
    weeks <= 28 -> BabyFruitInfo("berenjena", "1000 g", "37 cm", "🍆")
    weeks <= 32 -> BabyFruitInfo("calabaza", "1700 g", "42 cm", "🎃")
    weeks <= 36 -> BabyFruitInfo("melón", "2400 g", "47 cm", "🍈")
    else -> BabyFruitInfo("sandía", "3300 g", "51 cm", "🍉")
}

fun calculateDueDate(lastMenstrualPeriod: LocalDate): LocalDate = lastMenstrualPeriod.plusDays(280)

fun trimester(gestationalWeeks: Int): Int = when (gestationalWeeks) {
    in 1..13 -> 1
    in 14..27 -> 2
    else -> 3
}

fun daysRemaining(dueDate: LocalDate, today: LocalDate = LocalDate.now()): Long =
    ChronoUnit.DAYS.between(today, dueDate).coerceAtLeast(0)

/** "Hace un momento", "Hace N min", "Hace N h" o "Hace N días" a partir de una fecha ISO del backend. */
fun timeAgoLabel(isoDateTime: String?, now: java.time.Instant = java.time.Instant.now()): String {
    val instant = isoDateTime?.let { runCatching { java.time.OffsetDateTime.parse(it).toInstant() }.getOrNull() }
        ?: return ""
    val minutes = java.time.Duration.between(instant, now).toMinutes().coerceAtLeast(0)
    return when {
        minutes < 1 -> "Hace un momento"
        minutes < 60 -> "Hace $minutes min"
        minutes < 60 * 24 -> "Hace ${minutes / 60} h"
        else -> "Hace ${minutes / (60 * 24)} días"
    }
}

/** Texto del badge del semáforo: 1 Estable, 2 Atención, 3 Urgente. */
fun healthBadgeLabel(healthLight: Int?): String = when (healthLight) {
    3 -> "Urgente"
    2 -> "Atención"
    1 -> "Estable"
    else -> "Sin datos"
}

/** Texto del estado de salud: 1 Normal, 2 Atención, 3 Urgente. */
fun healthStatusLabel(healthLight: Int?): String = when (healthLight) {
    3 -> "Urgente"
    2 -> "Atención"
    1 -> "Normal"
    else -> "Sin mediciones"
}

/** La FPP debe ser posterior a la FUM y como máximo FUM + 308 días (44 semanas). */
fun isValidDueDate(lastMenstrualPeriod: LocalDate, dueDate: LocalDate): Boolean =
    dueDate.isAfter(lastMenstrualPeriod) && !dueDate.isAfter(lastMenstrualPeriod.plusDays(308))
