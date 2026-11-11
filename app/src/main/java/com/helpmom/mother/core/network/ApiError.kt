package com.helpmom.mother.core.network

import com.google.gson.Gson
import retrofit2.Response

data class ProblemDetails(
    val status: Int? = null,
    val title: String? = null,
    val detail: String? = null,
    val instance: String? = null,
    val errors: Map<String, List<String>>? = null
)

data class ApiError(
    val status: Int,
    val title: String,
    val detail: String,
    val errors: Map<String, List<String>> = emptyMap()
) : Exception(detail) {
    companion object {
        private val gson = Gson()

        fun from(response: Response<*>): ApiError {
            val problem = runCatching {
                response.errorBody()?.string()?.let {
                    gson.fromJson(it, ProblemDetails::class.java)
                }
            }.getOrNull()

            return ApiError(
                status = problem?.status ?: response.code(),
                title = problem?.title.orEmpty(),
                detail = problem?.detail ?: "No pudimos completar la solicitud. Inténtalo de nuevo.",
                errors = problem?.errors.orEmpty()
            )
        }
    }
}
