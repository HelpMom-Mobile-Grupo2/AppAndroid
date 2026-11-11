package com.helpmom.mother.feature.triage.data

import com.helpmom.mother.core.network.ApiError

class TriageRepository(private val api: TriageApi) {
    suspend fun send(id: String?, content: String): Result<TriageConversationDto> = runCatching {
        val response = if (id == null) api.start(TriageMessageRequest(content)) else api.send(id, TriageMessageRequest(content))
        if (response.isSuccessful) response.body()!! else throw ApiError.from(response)
    }
}
