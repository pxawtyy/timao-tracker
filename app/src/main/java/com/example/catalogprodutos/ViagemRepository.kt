package com.example.catalogprodutos

import android.content.ContentValues
import android.content.Context
import android.database.Cursor

data class Viagem(
    val id: Int = 0,
    val data: String,
    val kmInicial: Double,
    val kmFinal: Double,
    val litros: Double,
    val combustivel: String,
    val pedagio: Double,
    val valor: Double
) {
    fun calcularDistancia(): Double {
        return kmFinal - kmInicial
    }

    fun calcularCustoTotal(): Double {
        return valor + pedagio
    }
}

class ViagemRepository(context: Context) {

    private val dbHelper = BancoHelper(context)

    fun inserir(viagem: Viagem): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(BancoHelper.COLUNA_DATA, viagem.data)
            put(BancoHelper.COLUNA_KM_INICIAL, viagem.kmInicial)
            put(BancoHelper.COLUNA_KM_FINAL, viagem.kmFinal)
            put(BancoHelper.COLUNA_LITROS, viagem.litros)
            put(BancoHelper.COLUNA_COMBUSTIVEL, viagem.combustivel)
            put(BancoHelper.COLUNA_PEDAGIO, viagem.pedagio)
            put(BancoHelper.COLUNA_VALOR, viagem.valor)
        }
        return db.insert(BancoHelper.TABELA_VIAGENS, null, values)
    }

    fun listar(): List<Viagem> {
        val lista = mutableListOf<Viagem>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            BancoHelper.TABELA_VIAGENS,
            null,
            null,
            null,
            null,
            null,
            "${BancoHelper.COLUNA_ID} ASC"
        )

        cursor.use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndexOrThrow(BancoHelper.COLUNA_ID)
                val dataIndex = it.getColumnIndexOrThrow(BancoHelper.COLUNA_DATA)
                val kmIniIndex = it.getColumnIndexOrThrow(BancoHelper.COLUNA_KM_INICIAL)
                val kmFimIndex = it.getColumnIndexOrThrow(BancoHelper.COLUNA_KM_FINAL)
                val litrosIndex = it.getColumnIndexOrThrow(BancoHelper.COLUNA_LITROS)
                val combIndex = it.getColumnIndexOrThrow(BancoHelper.COLUNA_COMBUSTIVEL)
                val pedagioIndex = it.getColumnIndexOrThrow(BancoHelper.COLUNA_PEDAGIO)
                val valorIndex = it.getColumnIndexOrThrow(BancoHelper.COLUNA_VALOR)

                do {
                    val viagem = Viagem(
                        id = it.getInt(idIndex),
                        data = it.getString(dataIndex),
                        kmInicial = it.getDouble(kmIniIndex),
                        kmFinal = it.getDouble(kmFimIndex),
                        litros = it.getDouble(litrosIndex),
                        combustivel = it.getString(combIndex),
                        pedagio = it.getDouble(pedagioIndex),
                        valor = it.getDouble(valorIndex)
                    )
                    lista.add(viagem)
                } while (it.moveToNext())
            }
        }
        return lista
    }

    fun deletar(id: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            BancoHelper.TABELA_VIAGENS,
            "${BancoHelper.COLUNA_ID} = ?",
            arrayOf(id.toString())
        )
    }
}
