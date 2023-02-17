package com.example.database.models.response

import kotlinx.serialization.Serializable

@Serializable
data class MessageDTO(
    val name: String,
    val message: String,
    val id: String
)
