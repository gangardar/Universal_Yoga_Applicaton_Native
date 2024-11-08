package com.example.universalyoga

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.UUID

class ClassDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){
    companion object{
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Yoga.db"
        const val TABLE_NAME = "classes"
        const val COURSE_TABLE_NAME = "courses"
        const val COLUMN_ID = "id"
        const val COLUMN_COURSE_ID = "courseId"
        const val COLUMN_DATE = "date"
        const val COLUMN_TEACHER = "teacher"
        const val COLUMN_COMMENT = "comment"
        const val COLUMN_CREATED_AT = "createdAt"
        const val COLUMN_UPDATED_AT = "updatedAt"
        const val COLUMN_SYNCED = "synced"
        const val COLUMN_ISDELETED = "isDeleted"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_CLASS_TABLE = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID TEXT PRIMARY KEY,
            $COLUMN_COURSE_ID TEXT,
            $COLUMN_DATE TEXT,
            $COLUMN_TEACHER TEXT,
            $COLUMN_COMMENT TEXT,
            $COLUMN_CREATED_AT TEXT,
            $COLUMN_UPDATED_AT TEXT,
            $COLUMN_SYNCED INT,
            FOREIGN KEY($COLUMN_COURSE_ID) REFERENCES $COURSE_TABLE_NAME(id) ON DELETE CASCADE
        )
    """
        db?.execSQL(CREATE_CLASS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME");
        onCreate(db)
    }

    fun createClass(clazz: Class) : Long {
        val classId = clazz.id.ifEmpty { UUID .randomUUID().toString() }
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, classId)
            put(COLUMN_DATE, clazz.date.toString())
            put(COLUMN_COURSE_ID, clazz.courseId)
            put(COLUMN_TEACHER, clazz.teacher)
            put(COLUMN_COMMENT, clazz.comment)
            put(COLUMN_CREATED_AT, clazz.createdAt.toString())
            put(COLUMN_UPDATED_AT, clazz.updatedAt.toString())
            put(COLUMN_SYNCED, 0)
            put(COLUMN_ISDELETED, 0)
        }

        return db.insert(TABLE_NAME, null, values)
    }

    fun getAllClasses(): List<Class> {
        val classes = mutableListOf<Class>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ISDELETED = ?", arrayOf("0"))

        if (cursor.moveToFirst()) {
            do {
                val clazz = com.example.universalyoga.Class(
                    id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    date = convertStringToTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(
                        COLUMN_DATE
                    ))),
                    courseId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE_ID)),
                    teacher = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEACHER)),
                    comment = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT)),
                    createdAt = convertStringToTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(
                        COLUMN_CREATED_AT
                    ))) ,
                    updatedAt = convertStringToTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(
                        COLUMN_UPDATED_AT
                    ))),
                    synced = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SYNCED)),
                    isDeleted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ISDELETED)),
                )
                classes.add(clazz)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return classes
    }

    fun getUnsyncedClass(): List<Class> {
        val clazzes = mutableListOf<Class>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${TABLE_NAME} WHERE $COLUMN_SYNCED = ?",
            arrayOf("0")
        )

        var clazz: Class? = null
        if (cursor.moveToFirst()) {
            do {
                val clazz = Class(
                    id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    date = convertStringToTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(
                        COLUMN_DATE
                    ))),
                    courseId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE_ID)),
                    teacher = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEACHER)),
                    comment = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT)),
                    createdAt = convertStringToTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(
                        COLUMN_CREATED_AT
                    ))) ,
                    updatedAt = convertStringToTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(
                        COLUMN_UPDATED_AT
                    ))),
                    synced = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SYNCED)),
                    isDeleted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ISDELETED)),
                )
                clazzes.add(clazz)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return clazzes
    }

    fun deleteClass(id:String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_NAME,"$COLUMN_ID=?", arrayOf(id.toString()))
    }

    fun softDelete(clazz: Class): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("date", clazz.date.toString())
            put("courseId", clazz.courseId)
            put("teacher", clazz.teacher)
            put("comment", clazz.comment)
            put("synced", 0)
            put("isDeleted", 1)
            put("updatedAt", Timestamp(System.currentTimeMillis()).toString()) // Update the timestamp
        }

        // Define the WHERE clause and arguments
        val selection = "id = ?"
        val selectionArgs = arrayOf(clazz.id)
        // Perform the update and return the number of rows affected
        return db.update("classes", values, selection, selectionArgs)
    }

    fun updateClass(clazz: Class): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("date", clazz.date.toString())
            put("courseId", clazz.courseId)
            put("teacher", clazz.teacher)
            put("comment", clazz.comment)
            put("synced", 0)
            put("updatedAt", Timestamp(System.currentTimeMillis()).toString()) // Update the timestamp
        }

        // Define the WHERE clause and arguments
        val selection = "id = ?"
        val selectionArgs = arrayOf(clazz.id)
        // Perform the update and return the number of rows affected
        return db.update("classes", values, selection, selectionArgs)
    }

    fun insertOrUpdateClasses(clazz: Class) {
        val remoteCreateAt = clazz.createdAt.time
        val remoteUpdatedAtMillis = clazz.updatedAt.time
        val dateFromRemote = clazz.date.time
        val db = this.writableDatabase

        // Query to check if the course exists and its updatedAt timestamp
        val selection = "${COLUMN_ID} = ?"
        val selectionArgs = arrayOf(clazz.id)
        val cursor = db.query(TABLE_NAME, arrayOf(COLUMN_UPDATED_AT), selection, selectionArgs, null, null, null)

        val values = ContentValues().apply {
            put("id", clazz.id)
            put("date", Timestamp(dateFromRemote).toString())
            put("courseId", clazz.courseId)
            put("comment", clazz.comment)
            put("teacher", clazz.teacher)
            put("isDeleted", clazz.isDeleted)
            put("synced", 1)
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

    fun changeToSynced(classes: List<Class>) {
        val db = this.writableDatabase
        db.beginTransaction() // Start a transaction for efficiency
        try {
            val selection = "id = ?"
            for (clazz in classes) {
                val selectionArgs = arrayOf(clazz.id)
                val values = ContentValues().apply {
                    put("date", clazz.date.toString())
                    put("courseId", clazz.courseId)
                    put("teacher", clazz.teacher)
                    put("comment", clazz.comment)
                    put("synced", 1)
                }
                db.update("classes", values, selection, selectionArgs)
            }
            db.setTransactionSuccessful() // Mark the transaction as successful
        } finally {
            db.endTransaction() // Always end the transaction, even if there's an exception
        }
    }

    fun changeToSyncedById(clazz: Class): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("date", clazz.date.toString())
            put("courseId", clazz.courseId)
            put("teacher", clazz.teacher)
            put("comment", clazz.comment)
            put("synced", 1)
        }

        // Define the WHERE clause and arguments
        val selection = "id = ?"
        val selectionArgs = arrayOf(clazz.id)
        // Perform the update and return the number of rows affected
        return db.update("classes", values, selection, selectionArgs)
    }

    fun searchClasses(date: String? = null, teacher: String? = null, day: Int? = null): List<Class> {
        val db = this.readableDatabase
        val classes = mutableListOf<Class>()

        // Build the WHERE clause dynamically based on non-null search parameters
        val selection = StringBuilder()
        val selectionArgs = mutableListOf<String>()

        // Always include isDeleted check
        selection.append("$COLUMN_ISDELETED = 0")

        if (!date.isNullOrEmpty()) {
            selection.append(" AND DATE($COLUMN_DATE) = DATE(?)")
            selectionArgs.add(date.split(" ")[0]) // Extract only the date part (YYYY-MM-DD)
        }

        if (!teacher.isNullOrEmpty()) {
            selection.append(" AND $COLUMN_TEACHER LIKE ?")
            selectionArgs.add("%$teacher%")
        }

        if (day != null) {
            selection.append(" AND strftime('%w', $COLUMN_DATE) = ?") // %w returns day of week
            selectionArgs.add(day.toString()) // Convert day to string
        }

        Log.d("TAG", "searchClasses: ${selection} ${selectionArgs}")

        // Execute the query
        val cursor = db.query(
            TABLE_NAME,
            null,  // Select all columns
            selection.toString(),
            selectionArgs.toTypedArray(),
            null,
            null,
            null
        )

        // Process the cursor
        if (cursor.moveToFirst()) {
            do {
                val clazz = Class(
                    id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    date = convertStringToTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))),
                    courseId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE_ID)),
                    teacher = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEACHER)),
                    comment = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT)),
                    createdAt = convertStringToTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT))),
                    updatedAt = convertStringToTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT))),
                    synced = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SYNCED)),
                    isDeleted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ISDELETED)),
                )
                classes.add(clazz)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return classes
    }










    fun convertStringToTimestamp(createdAtString: String): Timestamp {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = dateFormat.parse(createdAtString)
        return Timestamp(date.time)
    }

}
