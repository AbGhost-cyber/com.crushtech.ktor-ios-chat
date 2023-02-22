package com.example.database.models.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateGroupRequest(val groupName: String, val groupDesc: String, val groupIcon: String)
