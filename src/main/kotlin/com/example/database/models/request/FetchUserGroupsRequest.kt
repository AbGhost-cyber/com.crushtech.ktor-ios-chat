package com.example.database.models.request

import kotlinx.serialization.Serializable

@Serializable
data class FetchUserGroupsRequest(val name: String, val password: String)