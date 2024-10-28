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
        }

        return db.insert(TABLE_NAME, null, values)
    }

    fun getAllClasses(): List<Class> {
        val classes = mutableListOf<Class>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

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
                    )))
                )
                classes.add(clazz)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return classes
    }

    fun deleteClass(id:String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_NAME,"$COLUMN_ID=?", arrayOf(id.toString()))
    }

    fun updateClass(clazz: Class): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("date", clazz.date.toString())
            put("courseId", clazz.courseId)
            put("teacher", clazz.teacher)
            put("comment", clazz.comment)
            put("updatedAt", Timestamp(System.currentTimeMillis()).toString()) // Update the timestamp
        }
//        2024-10-20 23:39:08.605

        // Define the WHERE clause and arguments
        val selection = "id = ?"
        val selectionArgs = arrayOf(clazz.id)
        Log.d("TAG", "updateClass: ${values}")
        // Perform the update and return the number of rows affected
        return db.update("classes", values, selection, selectionArgs)
    }


    fun convertStringToTimestamp(createdAtString: String): Timestamp {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = dateFormat.parse(createdAtString)
        return Timestamp(date.time)
    }

}
