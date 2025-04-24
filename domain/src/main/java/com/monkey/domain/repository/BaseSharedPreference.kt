package com.monkey.domain.repository

interface BaseSharedPreference {
    suspend fun save(key: String, value: Any)
}