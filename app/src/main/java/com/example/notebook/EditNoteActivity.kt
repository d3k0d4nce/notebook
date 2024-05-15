package com.example.notebook

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class EditNoteActivity : AppCompatActivity() {
    private lateinit var titleEditText: EditText
    private lateinit var textEditText: EditText
    private lateinit var note: Note

    private val databaseHelper = DatabaseHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        titleEditText = findViewById(R.id.title)
        textEditText = findViewById(R.id.text)

        // Получить выбранную заметку из намерения
        note = intent.getSerializableExtra("note") as Note

        // Заполнить поля заметкой
        titleEditText.setText(note.title)
        textEditText.setText(note.text)

        val saveButton: Button = findViewById(R.id.save)
        saveButton.setOnClickListener {
            // Обновить заметку
            note.title = titleEditText.text.toString()
            note.text = textEditText.text.toString()

            // Сохранить заметку в базу данных
            databaseHelper.updateNote(note.id, note.title, note.text)

            // Вернуться в MainActivity
            setResult(Activity.RESULT_OK)
            finish()
        }

        val deleteButton: Button = findViewById(R.id.delete)
        deleteButton.setOnClickListener {
            // Удалить заметку из базы данных
            databaseHelper.deleteNote(note.id)

            // Вернуться в MainActivity
            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}
