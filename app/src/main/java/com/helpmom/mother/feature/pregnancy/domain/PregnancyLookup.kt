package com.helpmom.mother.feature.pregnancy.domain

/**
 * Devuelve el id del embarazo de la mamá.
 * Primero usa el guardado en DataStore; si no hay, lo pide al backend (GET /api/pregnancy).
 * Devuelve null si la mamá todavía no registró un embarazo o si no se pudo consultar.
 */
suspend fun findPregnancyId(savedId: String?, fetchFirstId: suspend () -> String?): String? =
    savedId?.takeIf { it.isNotBlank() } ?: runCatching { fetchFirstId() }.getOrNull()
