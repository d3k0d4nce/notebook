package com.example.notebook

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import java.sql.SQLException

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "notepad.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NOTES = "notes"
        private const val TABLE_TAGS = "tags"
        private const val TABLE_NOTES_TAGS = "notes_tags"
        const val COLUMN_ID = "_id"
        private const val COLUMN_NOTE_ID = "_id_note"
        const val COLUMN_TAG_ID = "_id_tag"
        const val COLUMN_TITLE = "title"
        const val COLUMN_TEXT = "text"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_TAG_NAME = "tag_name"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE $TABLE_NOTES (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_TITLE TEXT, " +
                    "$COLUMN_TEXT TEXT, " +
                    "$COLUMN_CREATED_AT DATETIME" +
                    ")"
        )
        db.execSQL(
            "CREATE TABLE $TABLE_TAGS (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_TAG_NAME TEXT UNIQUE" +
                    ")"
        )
        db.execSQL(
            "CREATE TABLE $TABLE_NOTES_TAGS (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_NOTE_ID INTEGER REFERENCES $TABLE_NOTES($COLUMN_ID), " +
                    "$COLUMN_TAG_ID INTEGER REFERENCES $TABLE_TAGS($COLUMN_ID), " +
                    "UNIQUE ($COLUMN_NOTE_ID, $COLUMN_TAG_ID)" +
                    ")"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TAGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTES_TAGS")
        onCreate(db)
    }

    @SuppressLint("Range")
    fun getAllNotes(): MutableList<Note> {
        val cursor = readableDatabase.query(TABLE_NOTES,
            arrayOf(COLUMN_ID, COLUMN_TITLE, COLUMN_TEXT, COLUMN_CREATED_AT),
            null, null, null, null, "$COLUMN_CREATED_AT DESC")

        val notes = mutableListOf<Note>()
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
            val title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE))
            val text = cursor.getString(cursor.getColumnIndex(COLUMN_TEXT))
            val date = cursor.getString(cursor.getColumnIndex(COLUMN_CREATED_AT))

            notes.add(Note(id, title, text, date))
        }
        cursor.close()

        return notes
    }


    fun getNote(id: Int): Cursor {
        return readableDatabase.query(TABLE_NOTES, null, "$COLUMN_ID = ?", arrayOf(id.toString()), null, null, null)
    }

    fun insertNote(title: String, text: String, date: String): Long {
        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_TEXT, text)
            put(COLUMN_CREATED_AT, date)
        }
        return writableDatabase.insert(TABLE_NOTES, null, values)
    }

    fun updateNote(id: Int, title: String, text: String): Int {
        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_TEXT, text)
        }
        return writableDatabase.update(TABLE_NOTES, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun deleteNote(id: Int): Int {
        return writableDatabase.delete(TABLE_NOTES, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    @SuppressLint("Range")
    fun getAllTags(): MutableList<Tag> {
        val tags = mutableListOf<Tag>()
        // Open the database for reading
        val database = readableDatabase
        // Execute the query to get all tags
        val cursor: Cursor? = try {
            database.query(TABLE_TAGS, null, null, null, null, null, COLUMN_TAG_NAME)
        } catch (e: SQLException) {
            Log.e("DatabaseHelper", "Error getting tags from database", e)
            null
        }
        // Check if the cursor is not null
        if (cursor != null) {
            // Iterate over the cursor and add tags to the list
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
                val name = cursor.getString(cursor.getColumnIndex(COLUMN_TAG_NAME))
                tags.add(Tag(id, name))
            }
            // Close the cursor
            cursor.close()
        }
        // Close the database connection
        database.close()
        return tags
    }

    fun getTag(name: String): Cursor {
        return readableDatabase.query(TABLE_TAGS, null, "$COLUMN_TAG_NAME = ?", arrayOf(name), null, null, null)
    }

    fun insertTag(name: String): Long {
        val values = ContentValues().apply {
            put(COLUMN_TAG_NAME, name)
        }
        return writableDatabase.insert(TABLE_TAGS, null, values)
    }

    fun updateTag(name: String, newName: String): Int {
        val values = ContentValues().apply {
            put(COLUMN_TAG_NAME, newName)
        }
        return writableDatabase.update(TABLE_TAGS, values, "$COLUMN_TAG_NAME = ?", arrayOf(name))
    }

    fun deleteTag(name: String): Int {
        return writableDatabase.delete(TABLE_TAGS, "$COLUMN_TAG_NAME = ?", arrayOf(name))
    }

    fun getNotesByTag(tagName: String): Cursor {
        val subquery = "SELECT $COLUMN_NOTE_ID FROM $TABLE_NOTES_TAGS WHERE $COLUMN_TAG_ID = (SELECT $COLUMN_ID FROM $TABLE_TAGS WHERE $COLUMN_TAG_NAME = '$tagName')"
        return readableDatabase.rawQuery("SELECT * FROM $TABLE_NOTES WHERE $COLUMN_ID IN ($subquery)", null)
    }

    fun getNoteTags(noteId: Int): Cursor {
        val subquery = "SELECT $COLUMN_TAG_ID FROM $TABLE_NOTES_TAGS WHERE $COLUMN_NOTE_ID = ?"
        return readableDatabase.rawQuery("SELECT $COLUMN_TAG_NAME FROM $TABLE_TAGS WHERE $COLUMN_ID IN ($subquery)", arrayOf(noteId.toString()))
    }

    fun insertNoteTag(noteId: Int, tagId: Int): Long {
        val values = ContentValues().apply {
            put(COLUMN_NOTE_ID, noteId)
            put(COLUMN_TAG_ID, tagId)
        }
        return writableDatabase.insert(TABLE_NOTES_TAGS, null, values)
    }

    fun deleteNoteTag(noteId: Int, tagId: Int): Int {
        return writableDatabase.delete(TABLE_NOTES_TAGS, "$COLUMN_NOTE_ID = ? AND $COLUMN_TAG_ID = ?", arrayOf(noteId.toString(), tagId.toString()))
    }
}