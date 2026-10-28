package com.example.midexam

import android.app.Activity
import android.os.Bundle
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

class DatabaseHelper(context: android.content.Context) :
    SQLiteOpenHelper(context, "StudentDB.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE students (" +
                    "student_id TEXT PRIMARY KEY, " +
                    "student_name TEXT NOT NULL, " +
                    "student_age INTEGER NOT NULL CHECK(student_age BETWEEN 17 AND 50))"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS students")
        onCreate(db)
    }

    fun addStudent(name: String, id: String, age: Int): Boolean {
        val values = ContentValues().apply {
            put("student_name", name)
            put("student_id", id)
            put("student_age", age)
        }
        return writableDatabase.insert("students", null, values) != -1L
    }

    fun updateStudent(name: String, id: String, age: Int): Boolean {
        val values = ContentValues().apply {
            put("student_name", name)
            put("student_age", age)
        }
        return writableDatabase.update(
            "students", values, "student_id = ?", arrayOf(id)
        ) > 0
    }

    fun deleteStudent(id: String): Boolean {
        return writableDatabase.delete(
            "students", "student_id = ?", arrayOf(id)
        ) > 0
    }

    fun getStudents(): Cursor {
        return readableDatabase.rawQuery(
            "SELECT student_id, student_name, student_age FROM students",
            null
        )
    }
}

class MainActivity : Activity() {

    private lateinit var db: DatabaseHelper
    private lateinit var etName: EditText
    private lateinit var etId: EditText
    private lateinit var seekAge: SeekBar
    private lateinit var tvAge: TextView
    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DatabaseHelper(this)
        etName = findViewById(R.id.etName)
        etId = findViewById(R.id.etId)
        seekAge = findViewById(R.id.seekAge)
        tvAge = findViewById(R.id.tvAge)
        tvResult = findViewById(R.id.tvResult)

        seekAge.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?, progress: Int, fromUser: Boolean
            ) {
                tvAge.text = "Student Age: ${progress + 17}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        findViewById<Button>(R.id.btnAdd).setOnClickListener {
            val name = etName.text.toString().trim()
            val id = etId.text.toString().trim()
            val age = seekAge.progress + 17

            if (name.isEmpty() || id.isEmpty()) {
                message("Enter student name and ID")
            } else if (db.addStudent(name, id, age)) {
                message("Student added")
                showStudents()
            } else {
                message("Could not add student. Check if ID already exists.")
            }
        }

        findViewById<Button>(R.id.btnView).setOnClickListener {
            showStudents()
        }

        findViewById<Button>(R.id.btnEdit).setOnClickListener {
            val name = etName.text.toString().trim()
            val id = etId.text.toString().trim()
            val age = seekAge.progress + 17

            if (name.isEmpty() || id.isEmpty()) {
                message("Enter student name and ID")
            } else if (db.updateStudent(name, id, age)) {
                message("Student updated")
                showStudents()
            } else {
                message("Student ID not found")
            }
        }

        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            val id = etId.text.toString().trim()

            if (id.isEmpty()) {
                message("Enter Student ID")
            } else if (db.deleteStudent(id)) {
                message("Student deleted")
                showStudents()
            } else {
                message("Student ID not found")
            }
        }
    }

    private fun showStudents() {
        val cursor = db.getStudents()
        val result = StringBuilder()

        cursor.use {
            while (it.moveToNext()) {
                result.append("ID: ${it.getString(0)}\n")
                result.append("Name: ${it.getString(1)}\n")
                result.append("Age: ${it.getInt(2)}\n\n")
            }
        }

        tvResult.text = if (result.isEmpty()) {
            "No student records found."
        } else {
            result.toString()
        }
    }

    private fun message(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}