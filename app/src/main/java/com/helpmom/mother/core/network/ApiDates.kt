package com.helpmom.mother.core.network

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun parseDateOnly(value: String): LocalDate = LocalDate.parse(value)
fun parseDateTimeOffset(value: String): OffsetDateTime = OffsetDateTime.parse(value)
fun formatForPhone(value: String, pattern: String = "d MMM, HH:mm"): String =
    parseDateTimeOffset(value).atZoneSameInstant(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern(pattern))
