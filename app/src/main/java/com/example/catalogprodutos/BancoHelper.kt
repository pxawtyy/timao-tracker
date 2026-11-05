package com.example.catalogprodutos

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BancoHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "controle_viagens.db"
        const val DATABASE_VERSION = 1

        const val TABELA_VIAGENS = "viagens"
        const val COLUNA_ID = "id"
        const val COLUNA_DATA = "data"
        const val COLUNA_KM_INICIAL = "km_inicial"
        const val COLUNA_KM_FINAL = "km_final"
        const val COLUNA_LITROS = "litros"
        const val COLUNA_COMBUSTIVEL = "combustivel"
        const val COLUNA_PEDAGIO = "pedagio"
        const val COLUNA_VALOR = "valor"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val sqlCriarTabela = """
            CREATE TABLE $TABELA_VIAGENS (
                $COLUNA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUNA_DATA TEXT NOT NULL,
                $COLUNA_KM_INICIAL REAL NOT NULL,
                $COLUNA_KM_FINAL REAL NOT NULL,
                $COLUNA_LITROS REAL NOT NULL,
                $COLUNA_COMBUSTIVEL TEXT NOT NULL,
                $COLUNA_PEDAGIO REAL NOT NULL,
                $COLUNA_VALOR REAL NOT NULL
            )
        """.trimIndent()
        db.execSQL(sqlCriarTabela)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABELA_VIAGENS")
        onCreate(db)
    }
}
