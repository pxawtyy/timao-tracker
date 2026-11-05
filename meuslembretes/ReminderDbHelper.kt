package com.example.meuslembretes

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class ReminderItem(val id: Int, val description: String, val timeMillis: Long)

class ReminderDbHelper(context: Context) :
    SQLiteOpenHelper(context, "lembretes.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE lembretes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                descricao TEXT NOT NULL,
                horario INTEGER NOT NULL
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    fun insert(description: String, timeMillis: Long): Int {
        val values = ContentValues().apply {
            put("descricao", description)
            put("horario", timeMillis)
        }
        return writableDatabase.insert("lembretes", null, values).toInt()
    }

    fun getAll(): List<ReminderItem> {
        val result = mutableListOf<ReminderItem>()
        readableDatabase.rawQuery(
            "SELECT id, descricao, horario FROM lembretes ORDER BY horario", null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result.add(ReminderItem(cursor.getInt(0), cursor.getString(1), cursor.getLong(2)))
            }
        }
        return result
    }

    fun delete(id: Int) {
        writableDatabase.delete("lembretes", "id = ?", arrayOf(id.toString()))
    }
}
