package com.example.cotacaodolar

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class CotacaoRepository {

    suspend fun buscarCotacaoDolar(): Cotacao = withContext(Dispatchers.IO) {
        val url = URL("https://open.er-api.com/v6/latest/USD")
        val conexao = url.openConnection() as HttpURLConnection

        try {
            conexao.requestMethod = "GET"
            conexao.connectTimeout = 10000
            conexao.readTimeout = 10000

            val codigoResposta = conexao.responseCode
            val fluxo = if (codigoResposta in 200..299) {
                conexao.inputStream
            } else {
                conexao.errorStream
            }

            val resposta = fluxo?.bufferedReader()?.use { it.readText() }
                ?: throw IOException("A API não retornou conteúdo.")

            if (codigoResposta !in 200..299) {
                throw IOException("A API retornou HTTP $codigoResposta.")
            }

            val json = JSONObject(resposta)
            val resultado = json.optString("result")

            if (resultado != "success") {
                throw IOException("A API não confirmou o sucesso da consulta.")
            }

            val taxas = json.getJSONObject("rates")
            val cotacaoBrl = taxas.getDouble("BRL")

            Cotacao(
                moedaBase = json.optString("base_code", "USD"),
                valorEmReais = cotacaoBrl
            )
        } finally {
            conexao.disconnect()
        }
    }
}
