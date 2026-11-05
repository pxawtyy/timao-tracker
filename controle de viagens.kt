package com.fatec.andrasbh

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

// 1. Modelo de Dados da Viagem
class Viagem(
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

// 2. Banco de Dados SQLite (SQLiteOpenHelper)
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

// 3. Repositório para operações no Banco SQLite
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
}

// 4. Activity Principal com Jetpack Compose
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Scaffold(
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                ControleViagensScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}

@Composable
fun ControleViagensScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repository = remember { ViagemRepository(context) }

    var data by remember { mutableStateOf("") }
    var kmInicial by remember { mutableStateOf("") }
    var kmFinal by remember { mutableStateOf("") }
    var litros by remember { mutableStateOf("") }
    var combustivel by remember { mutableStateOf("") }
    var pedagio by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }

    var totalGasto by remember { mutableStateOf(0.0) }
    var totalKm by remember { mutableStateOf(0.0) }
    var totalLitros by remember { mutableStateOf(0.0) }
    var totalCombustivel by remember { mutableStateOf(0.0) }
    var totalPedagio by remember { mutableStateOf(0.0) }

    val viagens = remember { mutableStateListOf<Viagem>() }

    // Consulta os registros no SQLite e recalcula os totais
    fun carregarDados() {
        viagens.clear()
        val listaDoBanco = repository.listar()
        viagens.addAll(listaDoBanco)

        totalKm = listaDoBanco.sumOf { it.calcularDistancia() }
        totalGasto = listaDoBanco.sumOf { it.calcularCustoTotal() }
        totalLitros = listaDoBanco.sumOf { it.litros }
        totalCombustivel = listaDoBanco.sumOf { it.valor }
        totalPedagio = listaDoBanco.sumOf { it.pedagio }
    }

    // Persistência: busca os dados do SQLite ao abrir o app
    LaunchedEffect(Unit) {
        carregarDados()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Controle de Viagens",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        TextField(
            value = data,
            onValueChange = { data = it },
            label = { Text("Data") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))

        TextField(
            value = kmInicial,
            onValueChange = { kmInicial = it },
            label = { Text("KM Inicial") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))

        TextField(
            value = kmFinal,
            onValueChange = { kmFinal = it },
            label = { Text("KM Final") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))

        TextField(
            value = litros,
            onValueChange = { litros = it },
            label = { Text("Litros") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))

        TextField(
            value = combustivel,
            onValueChange = { combustivel = it },
            label = { Text("Combustivel") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))

        TextField(
            value = pedagio,
            onValueChange = { pedagio = it },
            label = { Text("Pedagio") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))

        TextField(
            value = valor,
            onValueChange = { valor = it },
            label = { Text("Valor Combustivel") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                val vKmInicial = kmInicial.toDoubleOrNull() ?: 0.0
                val vKmFinal = kmFinal.toDoubleOrNull() ?: 0.0
                val vLitros = litros.toDoubleOrNull() ?: 0.0
                val vPedagio = pedagio.toDoubleOrNull() ?: 0.0
                val vValor = valor.toDoubleOrNull() ?: 0.0

                val viagem = Viagem(
                    data = data,
                    kmInicial = vKmInicial,
                    kmFinal = vKmFinal,
                    litros = vLitros,
                    combustivel = combustivel,
                    pedagio = vPedagio,
                    valor = vValor
                )

                // 1. Cadastra e grava no SQLite
                repository.inserir(viagem)

                // 2. Consulta novamente do banco e atualiza a tela
                carregarDados()

                // Limpa os campos
                data = ""
                kmInicial = ""
                kmFinal = ""
                litros = ""
                combustivel = ""
                pedagio = ""
                valor = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cadastrar")
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Viagens Cadastradas (${viagens.size}):",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        if (viagens.isEmpty()) {
            Text(
                text = "Nenhuma viagem cadastrada ainda.",
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            for (viagem in viagens) {
                Text(
                    text = "${viagem.data} - ${viagem.combustivel}: " +
                            "${viagem.calcularDistancia()} km - " +
                            "R$ ${String.format(Locale.US, "%.2f", viagem.calcularCustoTotal())} " +
                            "(Combustível: R$ ${viagem.valor} | Pedágio: R$ ${viagem.pedagio})",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        val mediaKmL = if (totalLitros > 0.0) totalKm / totalLitros else 0.0

        Text(
            text = "Total de km: $totalKm km",
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Gastos com combustível: R$ ${String.format(Locale.US, "%.2f", totalCombustivel)}",
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Gastos com pedágios: R$ ${String.format(Locale.US, "%.2f", totalPedagio)}",
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Total gasto: R$ ${String.format(Locale.US, "%.2f", totalGasto)}",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Media km/l: ${String.format(Locale.US, "%.2f", mediaKmL)}",
            fontWeight = FontWeight.Medium
        )
    }
}
