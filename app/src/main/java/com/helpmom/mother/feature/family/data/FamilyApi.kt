package com.helpmom.mother.feature.family.data

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

data class FamilyMemberDto(val userId: String, val fullName: String, val role: Int, val joinedAt: String)
data class FamilyGroupDto(val id: String, val name: String, val members: List<FamilyMemberDto>)
data class InvitationDto(val code: String, val expiresAt: String)
interface FamilyApi {
    @POST("api/family/invitations") suspend fun createInvitation(): Response<InvitationDto>
    @GET("api/family") suspend fun getFamily(): Response<FamilyGroupDto>
    @DELETE("api/family/partner") suspend fun unlinkPartner(): Response<Unit>
}
