package com.example.database.models.response

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String
)