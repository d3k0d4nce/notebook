package com.example.notebook

import android.annotation.SuppressLint
import android.app.Activity
import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditTagActivity : AppCompatActivity() {
    private lateinit var tagEditText: EditText
    private lateinit var tag: String
    private lateinit var notesListView: ListView
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_tag)

        tagEditText = findViewById(R.id.tag_text)
        notesListView = findViewById(R.id.notes_list)
        databaseHelper = DatabaseHelper(this)

        // Получить выбранный тег из намерения
        tag = intent.getStringExtra("tag") ?: ""

        // Заполнить поле тегом
        tagEditText.setText(tag)

        // Загрузить список заметок при создании Activity
        loadNotesForTag()

        val saveButton: Button = findViewById(R.id.save)
        saveButton.setOnClickListener {
            // Обновить тег
            val newTag = tagEditText.text.toString()
            if (newTag.isBlank()) {
                Toast.makeText(this, "Введите название тега!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val databaseHelper = DatabaseHelper(this)
            databaseHelper.updateTag(tag, newTag)

            // Вернуться в TagsActivity
            setResult(Activity.RESULT_OK)
            finish()
        }

        val deleteButton: Button = findViewById(R.id.delete)
        deleteButton.setOnClickListener {
            // Удалить тег
            val databaseHelper = DatabaseHelper(this)
            databaseHelper.deleteTag(tag)

            // Вернуться в TagsActivity
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun loadNotesForTag() {
        // Получить название тега из поля ввода
        val tagName = tagEditText.text.toString()

        // Проверка на пустое имя тега
        if (tagName.isBlank()) {
            Toast.makeText(this, "Введите название тега!", Toast.LENGTH_SHORT).show()
            return
        }

        // Получить заметки, связанные с тегом
        val notesByTag = databaseHelper.getNotesByTag(tagName)

        // Настроить адаптер для отображения заметок
        val adapter = object : ArrayAdapter<Note>(this, android.R.layout.simple_list_item_1, getNotesFromCursor(notesByTag)) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                val note = getItem(position) // Получить заметку из адаптера

                // Отобразить информацию о заметке
                if (note != null) {
                    textView.text = "${note.title}\n${note.text}\n${note.date}"
                }

                return view
            }
        }
        notesListView.adapter = adapter
    }

    @SuppressLint("Range")
    private fun getNotesFromCursor(cursor: Cursor): List<Note> {
        val notes = mutableListOf<Note>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID))
                val title = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TITLE))
                val text = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TEXT))
                val date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CREATED_AT))
                notes.add(Note(id, title, text, date))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return notes
    }
}