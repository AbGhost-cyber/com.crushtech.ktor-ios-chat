package com.example.security.hashing

interface HashingService {
    fun generateSaltedHash(value: String, saltLength: Int = 32): SaltedHash
    fun verify(value: String, saltedHash: SaltedHash): Boolean
}

data class SaltedHash(
    val hash: String,
    val salt: String
)