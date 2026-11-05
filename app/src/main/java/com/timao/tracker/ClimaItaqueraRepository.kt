package com.timao.tracker

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class ClimaArena(
    val temperatura: Double,
    val umidade: Int,
    val descricaoTempo: String,
    val recomendacaoNeto: String
)

class ClimaItaqueraRepository {

    // Coordenadas exatas da Neo Química Arena (Itaquera - SP)
    private val latitude = -23.5453
    private val longitude = -46.4742

    suspend fun buscarClimaArena(): ClimaArena = withContext(Dispatchers.IO) {
        val urlApi = URL("https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current=temperature_2m,relative_humidity_2m,weather_code")
        val conexao = urlApi.openConnection() as HttpURLConnection

        val resposta: String
        try {
            conexao.requestMethod = "GET"
            conexao.connectTimeout = 8000
            conexao.readTimeout = 8000

            val codigo = conexao.responseCode
            val stream = if (codigo in 200..299) conexao.inputStream else conexao.errorStream
            resposta = stream?.bufferedReader()?.use { it.readText() }
                ?: throw IOException("A Arena não respondeu! Sinal fraco em Itaquera.")

            if (codigo !in 200..299) {
                throw IOException("Erro ao conectar na Arena: HTTP $codigo")
            }
        } finally {
            conexao.disconnect()
        }

        val json = JSONObject(resposta)
        val current = json.getJSONObject("current")
        val temperatura = current.getDouble("temperature_2m")
        val umidade = current.getInt("relative_humidity_2m")
        val weatherCode = current.getInt("weather_code")

        val descricao = when (weatherCode) {
            0 -> "Céu limpo em Itaquera! Solzão de rachar o Morumbi!"
            1, 2, 3 -> "Parcialmente nublado! Clima clássico de decisão!"
            45, 48 -> "Neblina na Fiel! Clima tenso de mata-mata!"
            51, 53, 55, 61, 63, 65 -> "Chuva em Itaquera! Raça, suor e gol de cabeça aos 48'!"
            71, 73, 75 -> "Frio de congelar rival em Itaquera!"
            95, 96, 99 -> "Tempestade de trovoada! São Jorge tá furioso!"
            else -> "Tempo instável, mas o Timão é soberano!"
        }

        val recomendacao = when {
            temperatura >= 28.0 -> "🔥 CALOR DO CÃO! Leva a regata do Timão e bebe água porque lá dentro o bicho pega!"
            temperatura in 18.0..27.9 -> "🦅 CLIMA PERFEITO PRO JOGO! Manto sagrado titular número 10 e garganta afiada!"
            else -> "🥶 TÁ FRIO DEMAIS! Bota o agasalho corta-vento do Corinthians e pula os 90 minutos pra esquentar!"
        }

        ClimaArena(
            temperatura = temperatura,
            umidade = umidade,
            descricaoTempo = descricao,
            recomendacaoNeto = recomendacao
        )
    }
}
