package com.example.notebook

import android.annotation.SuppressLint
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddNoteActivity : AppCompatActivity() {
    private lateinit var titleEditText: EditText
    private lateinit var textEditText: EditText
    private val selectedTags = mutableListOf<Int>()

    private val databaseHelper by lazy { DatabaseHelper(this) }

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        titleEditText = findViewById(R.id.title)
        textEditText = findViewById(R.id.text)

        val addButton: Button = findViewById(R.id.add)
        addButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val text = textEditText.text.toString()
            if (title.isBlank() || text.isBlank()) {
                Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

            val id = databaseHelper.insertNote(title, text, date)
            if (id == -1L) {
                Toast.makeText(this, "Ошибка сохранения!", Toast.LENGTH_SHORT).show()
            } else {
                // Сохранить выбранные теги в таблице связей
                for (tagId in selectedTags) {
                    databaseHelper.insertNoteTag(id.toInt(), tagId)
                }
                setResult(RESULT_OK)
                finish()
            }
        }

        val tagListView: ListView = findViewById(R.id.tags)
        val tags: List<Tag> = databaseHelper.getAllTags().toMutableList()
        val adapter =
            object : ArrayAdapter<Tag>(this, android.R.layout.simple_list_item_multiple_choice, tags) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    val tagName = tags[position].name
                    val textView = view.findViewById<TextView>(android.R.id.text1)

                    textView.text = tagName
                    textView.isActivated = selectedTags.contains(tags[position].id)

                    return view
                }
            }
        tagListView.adapter = adapter
        tagListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        tagListView.setOnItemClickListener { _, view, position, _ ->
            val tagId = tags[position].id
            if (selectedTags.contains(tagId)) {
                selectedTags.remove(tagId)
            } else {
                selectedTags.add(tagId)
            }

            // Обновить флажок на элементе списка
            view.findViewById<TextView>(android.R.id.text1).isActivated = selectedTags.contains(tagId)
        }
    }
}