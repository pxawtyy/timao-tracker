package com.example.previsaotempo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var cidadeTexto by remember { mutableStateOf("") }
                    var previsao by remember { mutableStateOf<Previsao?>(null) }
                    var carregando by remember { mutableStateOf(false) }
                    var mensagem by remember { mutableStateOf("Informe uma cidade e toque em consultar.") }

                    val escopo = rememberCoroutineScope()
                    val repository = remember { PrevisaoRepository() }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Previsão do Tempo",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Consulte a temperatura atual de uma cidade via API REST."
                        )

                        OutlinedTextField(
                            value = cidadeTexto,
                            onValueChange = { cidadeTexto = it },
                            label = { Text("Nome da cidade") },
                            placeholder = { Text("Ex: Diadema, São Paulo, Santos") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val cidade = cidadeTexto.trim()
                                if (cidade.isEmpty()) {
                                    mensagem = "Por favor, digite o nome de uma cidade."
                                    previsao = null
                                    return@Button
                                }

                                escopo.launch {
                                    carregando = true
                                    mensagem = "Consultando a previsão..."
                                    previsao = null

                                    try {
                                        val resultado = repository.buscarPrevisao(cidade)
                                        previsao = resultado
                                        mensagem = "Consulta realizada com sucesso!"
                                    } catch (e: Exception) {
                                        mensagem = "Não foi possível obter a previsão: " +
                                                (e.message ?: "erro inesperado")
                                    } finally {
                                        carregando = false
                                    }
                                }
                            },
                            enabled = !carregando,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Consultar Previsão")
                        }

                        if (carregando) {
                            CircularProgressIndicator()
                        }

                        Text(
                            text = mensagem,
                            color = if (previsao == null && !carregando && mensagem.startsWith("Não foi possível"))
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurface
                        )

                        previsao?.let { dados ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = dados.cidade,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = String.format(Locale.getDefault(), "Temperatura: %.1f °C", dados.temperatura),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Dados obtidos em tempo real via Open-Meteo REST API.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
