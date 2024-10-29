package com.example.universalyoga

import java.sql.Timestamp

data class Class(
    val id: String,
    val courseId : String,
    val date: Timestamp,
    val teacher: String,
    val comment: String,
    val createdAt: Timestamp,
    val updatedAt: Timestamp,
    val synced : Int,
    val isDeleted : Int
)
