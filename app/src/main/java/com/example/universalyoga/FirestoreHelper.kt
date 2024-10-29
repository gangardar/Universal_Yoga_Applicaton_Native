package com.example.universalyoga

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

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



}