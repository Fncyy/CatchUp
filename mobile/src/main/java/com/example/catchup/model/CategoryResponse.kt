package com.example.catchup.model

data class CategoryResponse(
    val categories: List<String>,
    val description: String,
    val status: String
)