package com.example.catchup.shared.model

data class CategoryResponse(
    val categories: List<String>,
    val description: String,
    val status: String
)