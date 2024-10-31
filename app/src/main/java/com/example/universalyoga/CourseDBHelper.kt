package com.example.universalyoga

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.util.UUID

class CourseDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){
    companion object{
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Yoga.db"
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
        const val COLUMN_SYNCED = "synced"
        const val COLUMN_ISDELETED = "isDeleted"
//        Additional for Class Table
        const val CLASS_TABLE_NAME = "classes"
        const val COLUMN_COURSE_ID = "courseId"
        const val COLUMN_DATE = "date"
        const val COLUMN_TEACHER = "teacher"
        const val COLUMN_COMMENT = "comment"
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
                "$COLUMN_SYNCED INTEGER, " +
                "$COLUMN_ISDELETED INTEGER, " +
                "$COLUMN_CREATED_AT TEXT, " +                          // Creation timestamp as TEXT
                "$COLUMN_UPDATED_AT TEXT)" )                           // Update timestamp as TEXT

        val CREATE_CLASS_TABLE = """
        CREATE TABLE $CLASS_TABLE_NAME (
            $COLUMN_ID TEXT PRIMARY KEY,
            $COLUMN_COURSE_ID TEXT,
            $COLUMN_DATE TEXT,
            $COLUMN_TEACHER TEXT,
            $COLUMN_COMMENT TEXT,
            $COLUMN_CREATED_AT TEXT,
            $COLUMN_UPDATED_AT TEXT,
            $COLUMN_SYNCED INTEGER,
            $COLUMN_ISDELETED INTEGER,
            FOREIGN KEY($COLUMN_COURSE_ID) REFERENCES $TABLE_NAME($COLUMN_ID) ON DELETE RESTRICT
        )
    """
        db?.execSQL(CREATE_CLASS_TABLE)
        db?.execSQL(CREATE_COURSE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db?.execSQL("DROP TABLE IF EXISTS $CLASS_TABLE_NAME")
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
            put(COLUMN_SYNCED, 0)
            put(COLUMN_ISDELETED, 0)
            put(COLUMN_IS_ACTIVE, if (course.isActive) 1 else 0)
            put(COLUMN_CREATED_AT, course.createdAt.toString())
            put(COLUMN_UPDATED_AT, course.updatedAt.toString())
        }

        return db.insert(TABLE_NAME, null, values)
    }

    fun getAllCourses(): List<Course> {
        val courses = mutableListOf<Course>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ISDELETED = ?", arrayOf("0"))

        if (cursor.moveToFirst()) {
            do {
                val course = Course(
                    id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    day = DayOfWeek.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY))),
                    time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                    capacity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAPACITY)),
                    duration = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION)),
                    price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                    type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL)),
                    isActive = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ACTIVE)) == 1,
                    synced = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SYNCED)),
                    isDeleted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ISDELETED)),
                    createdAt = convertStringToTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT))) ,
                    updatedAt = convertStringToTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT)))
                )
                courses.add(course)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return courses
    }

    fun getCourseById(id: String): Course? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = ? AND $COLUMN_ISDELETED = ?", arrayOf(id,"0"))

        var course: Course? = null
        if (cursor.moveToFirst()) {
            course = Course(
                id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                day = DayOfWeek.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY))),
                time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                capacity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAPACITY)),
                duration = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION)),
                price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL)),
                isActive = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ACTIVE)) == 1,
                synced = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SYNCED)),
                isDeleted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ISDELETED)),
                createdAt = convertStringToTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT))),
                updatedAt = convertStringToTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT)))
            )
        }
        cursor.close()
        return course
    }

    fun getUnsyncedCourse(): List<Course> {
        val courses = mutableListOf<Course>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_SYNCED = ?",
            arrayOf("0")
        )

        var course: Course? = null
        if (cursor.moveToFirst()) {
            do {
                val course = Course(
                    id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    day = DayOfWeek.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY))),
                    time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                    capacity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAPACITY)),
                    duration = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION)),
                    price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                    type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL)),
                    isActive = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ACTIVE)) == 1,
                    synced = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SYNCED)),
                    isDeleted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ISDELETED)),
                    createdAt = convertStringToTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT))) ,
                    updatedAt = convertStringToTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT)))
                )
                courses.add(course)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return courses
    }

    fun softDelete(id:String, imageURL: String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_NAME,"$COLUMN_ID=?", arrayOf(id.toString()))
    }

    fun softDelete(course: Course): Boolean {
        val db = this.writableDatabase

        // Check if there are any dependent classes for this course
        val classCount = db.query(
            "Classes",
            arrayOf("id"),
            "courseId = ?",
            arrayOf(course.id),
            null,
            null,
            null
        ).count

        return if (classCount > 0) {
            // Dependent classes exist, restrict deletion
            false
        } else {
            // No dependencies, proceed with soft deletion
            val values = ContentValues().apply {
                put("day", course.day.name)
                put("time", course.time)
                put("capacity", course.capacity)
                put("duration", course.duration)
                put("price", course.price)
                put("type", course.type)
                put("description", course.description)
                put("imageUrl", course.imageUrl)
                put("synced", 0)
                put("isDeleted", 1)
                put("updatedAt", Timestamp(System.currentTimeMillis()).toString()) // Update the timestamp
            }
            db.update("Courses", values, "id = ?", arrayOf(course.id)) > 0
        }
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
            put("synced", 0)
            put("updatedAt", Timestamp(System.currentTimeMillis()).toString()) // Update the timestamp
        }
//        2024-10-20 23:39:08.605

        // Define the WHERE clause and arguments
        val selection = "id = ?"
        val selectionArgs = arrayOf(course.id)

        // Perform the update and return the number of rows affected
        return db.update("courses", values, selection, selectionArgs)
    }

    fun insertOrUpdateCourse(course: Course) {
        val remoteCreateAt = course.createdAt.time
        val remoteUpdatedAtMillis = course.updatedAt.time
        val db = this.writableDatabase

        // Query to check if the course exists and its updatedAt timestamp
        val selection = "${COLUMN_ID} = ?"
        val selectionArgs = arrayOf(course.id)
        val cursor = db.query(TABLE_NAME, arrayOf(COLUMN_UPDATED_AT), selection, selectionArgs, null, null, null)

        val values = ContentValues().apply {
            put("id", course.id)
            put("day", course.day.name)
            put("time", course.time)
            put("capacity", course.capacity)
            put("duration", course.duration)
            put("price", course.price)
            put("type", course.type)
            put("description", course.description)
            put("imageUrl", course.imageUrl)
            put("synced", 1)
            put("isDeleted", course.isDeleted)
            put("createdAt",Timestamp(remoteUpdatedAtMillis).toString())
            put("updatedAt", Timestamp(remoteCreateAt).toString())
        }

        if (cursor.moveToFirst()) {
            val localUpdatedAtMillis = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT))

            // Update only if the remote updatedAt is later
            if (remoteUpdatedAtMillis > localUpdatedAtMillis) {

                db.update(TABLE_NAME, values, selection, selectionArgs)
            }
        } else {
            db.insert(TABLE_NAME, null, values)
        }

        cursor.close()
    }

    fun changeToSynced(courses: List<Course>) {
        val db = this.writableDatabase
        db.beginTransaction() // Start a transaction for efficiency
        try {
            val selection = "id = ?"
            for (course in courses) {
                val selectionArgs = arrayOf(course.id)
                val values = ContentValues().apply {
                    put("day", course.day.name)
                    put("time", course.time)
                    put("capacity", course.capacity)
                    put("duration", course.duration)
                    put("price", course.price)
                    put("type", course.type)
                    put("description", course.description)
                    put("imageUrl", course.imageUrl)
                    put("synced", 1)
                }
                db.update("classes", values, selection, selectionArgs)
            }
            db.setTransactionSuccessful() // Mark the transaction as successful
        } finally {
            db.endTransaction() // Always end the transaction, even if there's an exception
        }
    }

    fun changeToSyncedById(course: Course): Int {
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
            put("synced", 1)
        }

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
