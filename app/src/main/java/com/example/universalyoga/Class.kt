package com.example.universalyoga

import java.sql.Timestamp
import java.time.DayOfWeek

data class Class(
    val id: String,
    val courseId : String,
    val date: Timestamp,
    val teacher: String,
    val comment: String
)
