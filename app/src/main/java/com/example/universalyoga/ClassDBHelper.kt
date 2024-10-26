package com.example.universalyoga

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.util.UUID

class ClassDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){
    companion object{
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Courses.db"
        const val TABLE_NAME = "courses"
        const val COLUMN_ID = "id"
        const val COLUMN_DAY = "day"
        const val COLUMN_TIME = "time"
        const val COLUMN_CAPACITY  = "capacity"
        const val COLUMN_DURATION = "duration"
        const val COLUMN_PRICE = "price"
        const val COLUMN_TYPE = "type"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_IMAGE_URL = "imageUrl"
        const val COLUMN_IS_ACTIVE = "isActive"
        const val COLUMN_CREATED_AT = "createdAt"
        const val COLUMN_UPDATED_AT = "updatedAt"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_COURSE_TABLE = ("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID String PRIMARY KEY, " +     // Unique ID for the course
                "$COLUMN_DAY TEXT, " +                                 // Storing day as TEXT, could also use INTEGER for ENUM values
                "$COLUMN_TIME TEXT, " +                                // Time as TEXT
                "$COLUMN_CAPACITY INTEGER, " +                         // Capacity as INTEGER
                "$COLUMN_DURATION INTEGER, " +                         // Duration in minutes, INTEGER
                "$COLUMN_PRICE REAL, " +                               // Price as REAL for floating-point values
                "$COLUMN_TYPE TEXT, " +                                // Type as TEXT
                "$COLUMN_DESCRIPTION TEXT, " +                         // Description as TEXT
                "$COLUMN_IMAGE_URL TEXT, " +                           // Image URL as TEXT
                "$COLUMN_IS_ACTIVE INTEGER, " +                        // Active status as INTEGER (0 = false, 1 = true)
                "$COLUMN_CREATED_AT TEXT, " +                          // Creation timestamp as TEXT
                "$COLUMN_UPDATED_AT TEXT)" )                           // Update timestamp as TEXT
        db?.execSQL(CREATE_COURSE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME");
        onCreate(db)
    }

    fun createCourse(course: Course) : Long {
        val courseId = course.id.ifEmpty { UUID .randomUUID().toString() }
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, courseId)
            put(COLUMN_DAY, course.day.name)
            put(COLUMN_TIME, course.time)
            put(COLUMN_CAPACITY, course.capacity.toInt())
            put(COLUMN_DURATION, course.duration.toInt())
            put(COLUMN_PRICE, course.price)
            put(COLUMN_TYPE, course.type)
            put(COLUMN_DESCRIPTION, course.description )
            put(COLUMN_IMAGE_URL, course.imageUrl)  // Storing image URL as TEXT
            put(COLUMN_IS_ACTIVE, if (course.isActive) 1 else 0)
            put(COLUMN_CREATED_AT, course.createdAt.toString())
            put(COLUMN_UPDATED_AT, course.updatedAt.toString())
        }

        return db.insert(TABLE_NAME, null, values)
    }

    fun getAllCourses(): List<Course> {
        val courses = mutableListOf<Course>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        if (cursor.moveToFirst()) {
            do {
                val course = Course(

                    createdAt = convertStringToTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT))) ,
                    updatedAt = convertStringToTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT)))
                )
                courses.add(course)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return courses
    }

    fun deleteCourse(id:String,imageURL: String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_NAME,"$COLUMN_ID=?", arrayOf(id.toString()))
    }

    fun updateCourse(course: Course): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("day", course.day.name)
            put("time", course.time)
            put("capacity", course.capacity)
            put("duration", course.duration)
            put("price", course.price)
            put("type", course.type)
            put("description", course.description)
            put("imageUrl", course.imageUrl)
            put("updatedAt", Timestamp(System.currentTimeMillis()).toString()) // Update the timestamp
        }
//        2024-10-20 23:39:08.605

        // Define the WHERE clause and arguments
        val selection = "id = ?"
        val selectionArgs = arrayOf(course.id)

        // Perform the update and return the number of rows affected
        return db.update("courses", values, selection, selectionArgs)
    }


    fun convertStringToTimestamp(createdAtString: String): Timestamp {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = dateFormat.parse(createdAtString)
        return Timestamp(date.time)
    }

}
