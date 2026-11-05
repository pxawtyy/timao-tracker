package com.timao.tracker

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class TimaoRegistro(
    val id: Int = 0,
    val tipo: String, // "PROMESSA" ou "GASTO"
    val descricao: String,
    val valorOuSacrificio: Double, // Valor em R$ ou dias de promessa
    val data: String,
    val cumprido: Boolean = false
)

class TimaoDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "timao_loucura.db"
        const val DATABASE_VERSION = 1

        const val TABELA_REGISTROS = "registros_fiel"
        const val COLUNA_ID = "id"
        const val COLUNA_TIPO = "tipo"
        const val COLUNA_DESCRICAO = "descricao"
        const val COLUNA_VALOR = "valor"
        const val COLUNA_DATA = "data"
        const val COLUNA_CUMPRIDO = "cumprido"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val sqlCriarTabela = """
            CREATE TABLE $TABELA_REGISTROS (
                $COLUNA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUNA_TIPO TEXT NOT NULL,
                $COLUNA_DESCRICAO TEXT NOT NULL,
                $COLUNA_VALOR REAL NOT NULL,
                $COLUNA_DATA TEXT NOT NULL,
                $COLUNA_CUMPRIDO INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(sqlCriarTabela)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABELA_REGISTROS")
        onCreate(db)
    }

    fun inserirRegistro(registro: TimaoRegistro): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUNA_TIPO, registro.tipo)
            put(COLUNA_DESCRICAO, registro.descricao)
            put(COLUNA_VALOR, registro.valorOuSacrificio)
            put(COLUNA_DATA, registro.data)
            put(COLUNA_CUMPRIDO, if (registro.cumprido) 1 else 0)
        }
        return db.insert(TABELA_REGISTROS, null, values)
    }

    fun listarRegistros(): List<TimaoRegistro> {
        val lista = mutableListOf<TimaoRegistro>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABELA_REGISTROS,
            null,
            null,
            null,
            null,
            null,
            "$COLUNA_ID DESC"
        )

        cursor.use {
            val idIndex = it.getColumnIndexOrThrow(COLUNA_ID)
            val tipoIndex = it.getColumnIndexOrThrow(COLUNA_TIPO)
            val descIndex = it.getColumnIndexOrThrow(COLUNA_DESCRICAO)
            val valIndex = it.getColumnIndexOrThrow(COLUNA_VALOR)
            val dataIndex = it.getColumnIndexOrThrow(COLUNA_DATA)
            val cumpIndex = it.getColumnIndexOrThrow(COLUNA_CUMPRIDO)

            while (it.moveToNext()) {
                lista.add(
                    TimaoRegistro(
                        id = it.getInt(idIndex),
                        tipo = it.getString(tipoIndex),
                        descricao = it.getString(descIndex),
                        valorOuSacrificio = it.getDouble(valIndex),
                        data = it.getString(dataIndex),
                        cumprido = it.getInt(cumpIndex) == 1
                    )
                )
            }
        }
        return lista
    }

    fun alternarCumprido(id: Int, novoStatus: Boolean): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUNA_CUMPRIDO, if (novoStatus) 1 else 0)
        }
        return db.update(TABELA_REGISTROS, values, "$COLUNA_ID = ?", arrayOf(id.toString()))
    }

    fun deletarRegistro(id: Int): Int {
        val db = writableDatabase
        return db.delete(TABELA_REGISTROS, "$COLUNA_ID = ?", arrayOf(id.toString()))
    }
}
