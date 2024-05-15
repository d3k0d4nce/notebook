package com.example.notebook

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.Serializable
import java.sql.SQLException
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var notesButton: Button
    private lateinit var tagsButton: Button
    private lateinit var addNoteButton: Button
    private lateinit var notes: MutableList<Note> // Изменил на MutableList<Note>

    private val MAX_DISPLAY_LENGTH = 30 // Максимальная длина отображаемого текста

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.list)
        notesButton = findViewById(R.id.notes)
        tagsButton = findViewById(R.id.tags)
        addNoteButton = findViewById(R.id.addNote)

        val databaseHelper = DatabaseHelper(this)

        // Получить все заметки при запуске приложения
        try {
            notes = databaseHelper.getAllNotes() // Заверните в try-catch для обработки исключений
        } catch (e: SQLException) {
            Log.e("MainActivity", "Ошибка при получении заметок из базы данных", e)
            Toast.makeText(this, "Произошла ошибка. Попробуйте позже.", Toast.LENGTH_SHORT).show()
        }

        val adapter = object : ArrayAdapter<Note>(this, android.R.layout.simple_list_item_1, notes) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                val note = notes[position]

                // Создать строку с информацией о заметке
                var noteText = "${note.title}\n"
                if (note.text.length > MAX_DISPLAY_LENGTH) {
                    noteText += "${note.text.substring(0, MAX_DISPLAY_LENGTH) + "..."}\n"
                } else {
                    noteText += "${note.text}\n"
                }
                noteText += note.date

                // Добавить информацию о тегах
                noteText += "\nТеги: "
                val noteTags = databaseHelper.getNoteTags(note.id)
                if (noteTags.moveToFirst()) {
                    do {
                        noteText += noteTags.getString(noteTags.getColumnIndex(DatabaseHelper.COLUMN_TAG_NAME)) + ", "
                    } while (noteTags.moveToNext())

                    // Удалить последнюю запятую
                    noteText = noteText.substring(0, noteText.length - 2)
                } else {
                    noteText += "Нет тегов"
                }

                textView.text = noteText

                return view
            }
        }
        listView.adapter = adapter

        addNoteButton.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            startActivityForResult(intent, 1)
        }

        notesButton.setOnClickListener {
            notes.clear()
            notes.addAll(databaseHelper.getAllNotes()) // Заменено на addAll для копирования результатов запроса в список
            adapter.notifyDataSetChanged()
        }

        tagsButton.setOnClickListener {
            val intent = Intent(this, TagsActivity::class.java)
            startActivity(intent)
        }

        listView.setOnItemClickListener { parent, view, position, id ->
            val selectedNote = notes[position]

            val intent = Intent(this, EditNoteActivity::class.java)
            intent.putExtra("note", selectedNote as Serializable)
            startActivityForResult(intent, 2)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 || requestCode == 2 && resultCode == Activity.RESULT_OK) {
            notesButton.callOnClick() // Обновляем список заметок после добавления или редактирования
        }
    }
}

data class Note(
    val id: Int,
    var title: String,
    var text: String,
    val date: String
) : Serializable