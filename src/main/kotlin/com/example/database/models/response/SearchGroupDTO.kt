package com.example.database.models.response

import kotlinx.serialization.Serializable

@Serializable
data class SearchGroupDTO(
    val groupId: String,
    val groupIcon: String,
    val groupName: String,
    val groupDesc: String,
    val groupUrl: String,
    val dateCreated: Long
)