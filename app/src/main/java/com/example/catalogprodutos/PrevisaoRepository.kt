package com.example.catalogprodutos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class PrevisaoRepository {

    suspend fun buscarPrevisao(cidade: String): Previsao = withContext(Dispatchers.IO) {
        val cidadeFormatada = cidade.trim()
        if (cidadeFormatada.isEmpty()) {
            throw IOException("O nome da cidade não pode estar em branco.")
        }

        // 1. Obter coordenadas geográficas da cidade utilizando a API de geocodificação
        val nomeCodificado = URLEncoder.encode(cidadeFormatada, "UTF-8")
        val urlGeocoding = URL("https://geocoding-api.open-meteo.com/v1/search?name=$nomeCodificado&count=1&language=pt&format=json")
        val conexaoGeocoding = urlGeocoding.openConnection() as HttpURLConnection

        val respostaGeocoding: String
        try {
            conexaoGeocoding.requestMethod = "GET"
            conexaoGeocoding.connectTimeout = 10000
            conexaoGeocoding.readTimeout = 10000

            val codigo = conexaoGeocoding.responseCode
            val fluxo = if (codigo in 200..299) conexaoGeocoding.inputStream else conexaoGeocoding.errorStream
            respostaGeocoding = fluxo?.bufferedReader()?.use { it.readText() }
                ?: throw IOException("A API de busca não retornou conteúdo.")

            if (codigo !in 200..299) {
                throw IOException("A API de busca retornou HTTP $codigo.")
            }
        } finally {
            conexaoGeocoding.disconnect()
        }

        val jsonGeocoding = JSONObject(respostaGeocoding)
        val resultados = jsonGeocoding.optJSONArray("results")

        if (resultados == null || resultados.length() == 0) {
            throw IOException("Cidade \"$cidadeFormatada\" não foi encontrada.")
        }

        val cidadeObj = resultados.getJSONObject(0)
        val nomeCidade = cidadeObj.getString("name")
        val pais = cidadeObj.optString("country", "")
        val latitude = cidadeObj.getDouble("latitude")
        val longitude = cidadeObj.getDouble("longitude")

        // 2. Consultar a previsão meteorológica com as coordenadas obtidas
        val urlPrevisao = URL("https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current=temperature_2m")
        val conexaoPrevisao = urlPrevisao.openConnection() as HttpURLConnection

        val respostaPrevisao: String
        try {
            conexaoPrevisao.requestMethod = "GET"
            conexaoPrevisao.connectTimeout = 10000
            conexaoPrevisao.readTimeout = 10000

            val codigo = conexaoPrevisao.responseCode
            val fluxo = if (codigo in 200..299) conexaoPrevisao.inputStream else conexaoPrevisao.errorStream
            respostaPrevisao = fluxo?.bufferedReader()?.use { it.readText() }
                ?: throw IOException("A API de previsão não retornou conteúdo.")

            if (codigo !in 200..299) {
                throw IOException("A API meteorológica retornou HTTP $codigo.")
            }
        } finally {
            conexaoPrevisao.disconnect()
        }

        val jsonPrevisao = JSONObject(respostaPrevisao)
        val current = jsonPrevisao.getJSONObject("current")
        val temperatura = current.getDouble("temperature_2m")

        val nomeFormatado = if (pais.isNotEmpty()) "$nomeCidade, $pais" else nomeCidade

        Previsao(
            cidade = nomeFormatado,
            temperatura = temperatura
        )
    }
}
