package com.helpmom.mother

import com.helpmom.mother.core.network.ApiError
import com.helpmom.mother.feature.learning.ui.Article
import com.helpmom.mother.feature.learning.ui.isForWeek
import com.helpmom.mother.feature.pregnancy.domain.healthBadgeLabel
import com.helpmom.mother.feature.pregnancy.domain.isValidDueDate
import com.helpmom.mother.feature.pregnancy.domain.timeAgoLabel
import com.helpmom.mother.feature.pregnancy.domain.AlertResponse
import com.helpmom.mother.feature.pregnancy.domain.HealthLight
import com.helpmom.mother.feature.pregnancy.domain.SymptomSeverity
import com.helpmom.mother.feature.pregnancy.domain.TriageUrgency
import com.helpmom.mother.feature.pregnancy.domain.babyFruitInfo
import com.helpmom.mother.feature.pregnancy.domain.calculateDueDate
import com.helpmom.mother.feature.pregnancy.domain.responseLabel
import com.helpmom.mother.feature.pregnancy.domain.trimester
import com.helpmom.mother.feature.pregnancy.domain.urgencyLabel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.time.LocalDate
import java.time.OffsetDateTime

class DomainRulesTest {
    @Test fun enumCodesMapToDomain() {
        assertEquals(HealthLight.Green, HealthLight.fromCode(1))
        assertEquals(HealthLight.Yellow, HealthLight.fromCode(2))
        assertEquals(HealthLight.Red, HealthLight.fromCode(3))
        assertEquals(SymptomSeverity.Severe, SymptomSeverity.fromCode(3))
        assertEquals(TriageUrgency.Emergency, TriageUrgency.fromCode(3))
        assertEquals(AlertResponse.OnMyWay, AlertResponse.fromCode(1))
    }

    @Test fun responseLabelReturnsCorrectTexts() {
        assertEquals("Voy en camino 🚗", responseLabel(1))
        assertEquals("Te llamo ahora 📞", responseLabel(2))
        assertEquals("Listo ✓", responseLabel(3))
        assertEquals("", responseLabel(null))
        assertEquals("", responseLabel(0))
    }

    @Test fun urgencyLabelReturnsCorrectTexts() {
        assertEquals("Autocuidado", urgencyLabel(1))
        assertEquals("Consulta a tu equipo", urgencyLabel(2))
        assertEquals("Emergencia", urgencyLabel(3))
        assertEquals("", urgencyLabel(null))
    }

    @Test fun babyFruitInfoReturnsCorrectData() {
        val info24 = babyFruitInfo(24)
        assertEquals("papaya", info24.fruitName)
        assertEquals("600 g", info24.weightText)
        assertEquals("30 cm", info24.lengthText)
    }

    @Test fun dueDateAddsFortyWeeks() {
        assertEquals(LocalDate.parse("2027-02-14"), calculateDueDate(LocalDate.parse("2026-05-10")))
    }

    @Test fun trimesterUsesGestationalWeekRanges() {
        assertEquals(1, trimester(13)); assertEquals(2, trimester(14)); assertEquals(3, trimester(28))
    }

    @Test fun apiErrorReadsProblemDetails() {
        val body = """{"status":400,"title":"Error","detail":"Correo inválido","errors":{"Email":["Inválido"]}}"""
            .toResponseBody("application/problem+json".toMediaType())
        val error = ApiError.from(Response.error<Any>(400, body))
        assertEquals("Correo inválido", error.detail)
        assertEquals("Inválido", error.errors["Email"]?.first())
    }

    @Test fun offsetDatesRepresentCorrectInstants() {
        val utc = OffsetDateTime.parse("2026-11-04T15:00:00+00:00")
        val lima = OffsetDateTime.parse("2026-11-04T10:00:00-05:00")
        assertEquals(utc.toInstant(), lima.toInstant())
        assertEquals(LocalDate.parse("2026-05-10"), LocalDate.parse("2026-05-10"))
    }

    @Test fun timeAgoLabelUsesMinutesHoursAndDays() {
        val now = java.time.Instant.parse("2026-11-03T12:00:00Z")
        assertEquals("Hace un momento", timeAgoLabel("2026-11-03T11:59:30+00:00", now))
        assertEquals("Hace 5 min", timeAgoLabel("2026-11-03T11:55:00+00:00", now))
        assertEquals("Hace 2 h", timeAgoLabel("2026-11-03T10:00:00+00:00", now))
        assertEquals("Hace 3 días", timeAgoLabel("2026-10-31T12:00:00+00:00", now))
        assertEquals("", timeAgoLabel(null, now))
    }

    @Test fun dueDateCanBeEditedUpToFumPlus308() {
        val fum = LocalDate.of(2026, 3, 1)
        assertTrue(isValidDueDate(fum, fum.plusDays(280)))
        assertTrue(isValidDueDate(fum, fum.plusDays(308)))
        assertFalse(isValidDueDate(fum, fum.plusDays(309)))
        assertFalse(isValidDueDate(fum, fum))
    }

    @Test fun healthLabelsFollowTheTrafficLight() {
        assertEquals("Estable", healthBadgeLabel(1))
        assertEquals("Atención", healthBadgeLabel(2))
        assertEquals("Urgente", healthBadgeLabel(3))
        assertEquals("Sin datos", healthBadgeLabel(null))
    }

    @Test fun articlesAreFilteredByCurrentWeek() {
        val article = Article("a", "t", "Parto", 28, 42, "s", "b", "i")
        assertTrue(article.isForWeek(30))
        assertFalse(article.isForWeek(12))
    }
}
