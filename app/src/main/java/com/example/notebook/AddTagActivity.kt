package com.example.notebook

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddTagActivity : AppCompatActivity() {

    private lateinit var tagNameEditText: EditText
    private lateinit var addTagButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_tag)

        tagNameEditText = findViewById(R.id.tag_name)
        addTagButton  = findViewById(R.id.add)

        val databaseHelper = DatabaseHelper(this)

        addTagButton.setOnClickListener {
            val tagName = tagNameEditText.text.toString()
            if (tagName.isNotBlank()) {
                val id = databaseHelper.insertTag(tagName)
                if (id != -1L) {
                    Toast.makeText(this, "Тег добавлен!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Не удалось добавить тег!", Toast.LENGTH_SHORT).show()
                }
            }
            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}