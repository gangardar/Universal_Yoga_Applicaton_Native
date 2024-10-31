package com.example.universalyoga

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.sql.Timestamp
import java.time.DayOfWeek

class FirestoreHelper {

    fun syncCourseToFirebase(course: Course, courseDBHelper: CourseDBHelper) {
        val courseData = mapOf(
            "id" to course.id,
            "day" to course.day.toString(),
            "time" to course.time,
            "capacity" to course.capacity,
            "duration" to course.duration,
            "price" to course.price,
            "type" to course.type,
            "description" to course.description,
            "imageUrl" to course.imageUrl,
            "isActive" to course.isActive,
            "isDeleted" to course.isDeleted,
            "createdAt" to course.createdAt,
            "updatedAt" to course.updatedAt
        )
        FirebaseFirestore.getInstance().collection("courses")
            .document(course.id) // Use course ID as document ID
            .set(courseData)
            .addOnSuccessListener { courseDBHelper.changeToSyncedById(course) }
            .addOnFailureListener { exception ->
                // Handle sync errors
                Log.e("Firebase", "Error syncing course: ${exception.message}") }
    }

    fun syncClassToFirebase(clazz: Class, classDBHelper: ClassDBHelper) {
        val classData = mapOf(
            "id" to clazz.id,
            "courseId" to clazz.courseId,
            "date" to clazz.date,
            "teacher" to clazz.teacher,
            "comment" to clazz.comment,
            "isDeleted" to clazz.isDeleted,
            "createdAt" to clazz.createdAt,
            "updatedAt" to clazz.updatedAt
        )
        FirebaseFirestore.getInstance().collection("classes")
            .document(clazz.id) // Use course ID as document ID
            .set(classData)
            .addOnSuccessListener { classDBHelper.changeToSyncedById(clazz) }
            .addOnFailureListener { exception ->
                // Handle sync errors
                Log.e("Firebase", "Error syncing clazz: ${exception.message}") }
    }

    fun fetchCoursesFromFirebase(callback: (List<Course>) -> Unit) {
        FirebaseFirestore.getInstance().collection("courses")
            .whereEqualTo("isDeleted", 0) // Assuming `isDeleted` is an integer in Firestore
            .get()
            .addOnSuccessListener { snapshot ->
                val courses = mutableListOf<Course>()
                for (document in snapshot) {
                    val course = Course(
                        id = document.getString("id") ?: "",
                        day = DayOfWeek.valueOf((document.getString("day") ?: "").uppercase()),
                        time = document.getString("time") ?: "",
                        capacity = document.getLong("capacity")?.toInt() ?: 0,
                        duration = document.getLong("duration")?.toInt() ?: 0,
                        price = document.getDouble("price") ?: 0.0,
                        type = document.getString("type") ?: "",
                        description = document.getString("description") ?: "",
                        imageUrl = document.getString("imageUrl") ?: "",
                        isActive = document.getBoolean("isActive") ?: false,
                        createdAt = document.getTimestamp("createdAt")?.let { Timestamp(it.seconds * 1000 + it.nanoseconds / 1_000_000) } ?: Timestamp(0),
                        updatedAt = document.getTimestamp("updatedAt")?.let { Timestamp(it.seconds * 1000 + it.nanoseconds / 1_000_000) } ?: Timestamp(0),
                        synced = 1,
                        isDeleted = document.getLong("isDeleted")?.toInt() ?: 0
                    )
                    courses.add(course)
                }
                callback(courses)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseFetch", "Failed to fetch data", e)
                callback(emptyList()) // Return an empty list if there's an error
            }
    }


    fun fetchClassesFromFirebase(callback: (List<Class>) -> Unit) {
        FirebaseFirestore.getInstance().collection("classes")
            .whereEqualTo("isDeleted", 0) // Assuming `isDeleted` is an integer in Firestore
            .get()
            .addOnSuccessListener { snapshot ->
                val classes = mutableListOf<Class>()
                for (document in snapshot) {
                    val clazz = com.example.universalyoga.Class(
                        id = document.getString("id") ?: "",
                        date = document.getTimestamp("date")?.let { Timestamp(it.seconds * 1000 + it.nanoseconds / 1_000_000) } ?: Timestamp(0),
                        teacher = document.getString("teacher") ?: "",
                        comment = document.getString("comment") ?: "",
                        createdAt = document.getTimestamp("createdAt")?.let { Timestamp(it.seconds * 1000 + it.nanoseconds / 1_000_000) } ?: Timestamp(0),
                        updatedAt = document.getTimestamp("updatedAt")?.let { Timestamp(it.seconds * 1000 + it.nanoseconds / 1_000_000) } ?: Timestamp(0),
                        courseId = document.getString("courseId") ?: "",
                        synced = 1,
                        isDeleted = document.getLong("isDeleted")?.toInt() ?: 0
                    )
                    classes.add(clazz)
                }
                callback(classes)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseFetch", "Failed to fetch data", e)
                callback(emptyList()) // Return an empty list if there's an error
            }
    }




}