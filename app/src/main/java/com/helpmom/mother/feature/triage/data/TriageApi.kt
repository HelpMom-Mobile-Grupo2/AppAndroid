package com.helpmom.mother.feature.triage.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class TriageMessageDto(val id: String, val author: Int, val content: String, val sentAt: String)
data class TriageConversationDto(val id: String, val motherUserId: String, val createdAt: String,
                                 val messages: List<TriageMessageDto>, val urgency: Int?, val guidance: String?)
data class TriageSummaryDto(val id: String, val createdAt: String, val urgency: Int?, val lastMessage: String)
data class TriageMessageRequest(val content: String)

interface TriageApi {
    @POST("api/triage") suspend fun start(@Body request: TriageMessageRequest): Response<TriageConversationDto>
    @POST("api/triage/{conversationId}/messages")
    suspend fun send(@Path("conversationId") id: String, @Body request: TriageMessageRequest): Response<TriageConversationDto>
    @GET("api/triage") suspend fun conversations(): Response<List<TriageSummaryDto>>
    @GET("api/triage/{conversationId}") suspend fun conversation(@Path("conversationId") id: String): Response<TriageConversationDto>
}
