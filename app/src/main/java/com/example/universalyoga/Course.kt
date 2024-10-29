package com.example.universalyoga

import java.sql.Timestamp
import java.time.DayOfWeek

data class Course(
    val id: String,
    val day: DayOfWeek,
    val time: String,
    val capacity: Int,
    val duration: Int,
    val price: Double,
    val type: String,
    val description: String,
    val imageUrl: String,      
    val isActive: Boolean,
    val createdAt: Timestamp,
    val updatedAt: Timestamp,
    val synced : Int
)
