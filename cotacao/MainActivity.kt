package com.example.cotacaodolar

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var valorEmReaisTexto by remember { mutableStateOf("200,00") }
                    var cotacao by remember { mutableStateOf<Cotacao?>(null) }
                    var valorEmDolares by remember { mutableStateOf<Double?>(null) }
                    var carregando by remember { mutableStateOf(false) }
                    var mensagem by remember { mutableStateOf("Toque em consultar para buscar a cotação atual.") }

                    val escopo = rememberCoroutineScope()
                    val repository = remember { CotacaoRepository() }
                    val formatoReal = remember {
                        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
                    }
                    val formatoDolar = remember {
                        NumberFormat.getCurrencyInstance(Locale.US)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Cotação do dólar",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "Consulte uma API e calcule o valor aproximado em dólares."
                        )

                        OutlinedTextField(
                            value = valorEmReaisTexto,
                            onValueChange = { valorEmReaisTexto = it },
                            label = { Text("Valor em reais (R$)") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val valorEmReais = valorEmReaisTexto
                                    .trim()
                                    .replace(",", ".")
                                    .toDoubleOrNull()

                                if (valorEmReais == null || valorEmReais < 0) {
                                    mensagem = "Informe um valor válido e não negativo."
                                    valorEmDolares = null
                                    return@Button
                                }

                                escopo.launch {
                                    carregando = true
                                    mensagem = "Consultando a API..."
                                    valorEmDolares = null

                                    try {
                                        val cotacaoAtual = repository.buscarCotacaoDolar()
                                        cotacao = cotacaoAtual
                                        valorEmDolares =
                                            valorEmReais / cotacaoAtual.valorEmReais
                                        mensagem = "Cotação consultada com sucesso."
                                    } catch (e: Exception) {
                                        mensagem = "Não foi possível consultar a cotação: " +
                                                (e.message ?: "erro inesperado")
                                    } finally {
                                        carregando = false
                                    }
                                }
                            },
                            enabled = !carregando,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Consultar e converter")
                        }

                        if (carregando) {
                            CircularProgressIndicator()
                        }

                        Text(text = mensagem)

                        cotacao?.let {
                            Text(
                                text = "1 USD = ${formatoReal.format(it.valorEmReais)}",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        valorEmDolares?.let {
                            Text(
                                text = "Valor aproximado: ${formatoDolar.format(it)}",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "A cotação é uma taxa de referência e pode diferir do valor " +
                                    "comercial aplicado por bancos e casas de câmbio.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
