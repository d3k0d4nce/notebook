package com.example.notebook

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TagsActivity : AppCompatActivity() {

    private lateinit var listView: GridView
    private lateinit var notesButton: Button
    private lateinit var tagsButton: Button
    private lateinit var addTagButton: Button
    private lateinit var tags: MutableList<Tag>

    private val databaseHelper = DatabaseHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tags)

        listView = findViewById(R.id.listTags)
        notesButton = findViewById(R.id.notes)
        tagsButton = findViewById(R.id.tags)
        addTagButton = findViewById(R.id.addNote)

        loadTags()

        notesButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        tagsButton.setOnClickListener {
            loadTags()
        }

        addTagButton.setOnClickListener {
            val intent = Intent(this, AddTagActivity::class.java)
            startActivityForResult(intent, 3)
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val tag = listView.getItemAtPosition(position) as Tag
            showEditTagDialog(tag.name) // Передаем только название тега
        }
    }
    private fun loadTags() {
        // Получить все заметки при запуске приложения
        tags = databaseHelper.getAllTags().toMutableList()
        val adapter =
            object : ArrayAdapter<Tag>(this, android.R.layout.simple_list_item_1, tags) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    val tagName = tags[position] // Изменил на notes[position]
                    val textView = view.findViewById<TextView>(android.R.id.text1)

                    textView.text = tagName.name

                    return view
                }
            }
        listView.adapter = adapter
    }

    private fun showEditTagDialog(tagName: String) {
        val intent = Intent(this, EditTagActivity::class.java)
        intent.putExtra("tag", tagName)
        startActivityForResult(intent, 4)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3 || requestCode == 4 && resultCode == Activity.RESULT_OK) {
            tagsButton.callOnClick() // Обновляем список тегов после добавления или редактирования
        }
    }
}