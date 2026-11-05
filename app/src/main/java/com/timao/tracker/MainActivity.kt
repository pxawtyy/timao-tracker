package com.timao.tracker

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.catalogprodutos.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa canal de notificação oficial do Timão
        TimaoNotificationHelper.criarCanal(this)

        setContent {
            TimaoApp()
        }
    }
}

// CORES ULTRA CORINTHIANAS
val PretoTimao = Color(0xFF000000)
val BrancoTimao = Color(0xFFFFFFFF)
val VermelhoSangue = Color(0xFFD32F2F)
val OuroMundial = Color(0xFFFFD700)
val CinzaChumbo = Color(0xFF1E1E1E)

@Composable
fun TimaoApp() {
    val context = LocalContext.current

    // Controladores de dados
    val db = remember { TimaoDbHelper(context) }
    val dataStore = remember { TimaoDataStore(context) }
    val climaRepo = remember { ClimaItaqueraRepository() }

    // Estado do Perfil (DataStore)
    val perfil by dataStore.perfilFlow.collectAsState(
        initial = TimaoPerfil("Gavião da Fiel", "Gaviões da Fiel", true, "CÊ É LOUCO PÃO DE QUEIJO! 🧀⚽")
    )

    // Navegação simples entre 4 Telas
    var telaAtual by remember { mutableStateOf(0) }

    // Launcher de permissão de notificação Android 13+
    val permissaoNotifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedida ->
        if (concedida) {
            Toast.makeText(context, "🦅 ALERTA DO TIMÃO AUTORIZADO! VAI CORINTHIANS!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "⚠️ Permissão negada! Como vai saber a hora do jogo?!", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            permissaoNotifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PretoTimao)
                    .border(BorderStroke(3.dp, VermelhoSangue))
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_estrela_mundial),
                        contentDescription = "Mundial 2000",
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Image(
                        painter = painterResource(id = R.drawable.ic_corinthians_escudo),
                        contentDescription = "Escudo do Corinthians",
                        modifier = Modifier.size(46.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Image(
                        painter = painterResource(id = R.drawable.ic_estrela_mundial),
                        contentDescription = "Mundial 2012",
                        modifier = Modifier.size(26.dp)
                    )
                }

                Text(
                    text = "🦅 TIMÃO TRACKER 🦅",
                    color = BrancoTimao,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "AQUI É CORINTHIANS! (1910 - SEMPRE)",
                    color = VermelhoSangue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Salve, ${perfil.nome}! [${perfil.torcida}]",
                    color = OuroMundial,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = PretoTimao,
                contentColor = BrancoTimao,
                modifier = Modifier.border(BorderStroke(2.dp, BrancoTimao))
            ) {
                NavigationBarItem(
                    selected = telaAtual == 0,
                    onClick = { telaAtual = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = if (telaAtual == 0) VermelhoSangue else BrancoTimao) },
                    label = { Text("Arena", color = BrancoTimao, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = telaAtual == 1,
                    onClick = { telaAtual = 1 },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Gastos", tint = if (telaAtual == 1) VermelhoSangue else BrancoTimao) },
                    label = { Text("Promessas", color = BrancoTimao, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = telaAtual == 2,
                    onClick = { telaAtual = 2 },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Alarme", tint = if (telaAtual == 2) VermelhoSangue else BrancoTimao) },
                    label = { Text("Alarme", color = BrancoTimao, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = telaAtual == 3,
                    onClick = { telaAtual = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil", tint = if (telaAtual == 3) VermelhoSangue else BrancoTimao) },
                    label = { Text("Fiel", color = BrancoTimao, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CinzaChumbo)
        ) {
            when (telaAtual) {
                0 -> TelaHomeArena(climaRepo, perfil)
                1 -> TelaPromessasEGastos(db)
                2 -> TelaAlarmeJogo(context)
                3 -> TelaPerfilFiel(dataStore, perfil)
            }
        }
    }
}

// -------------------------------------------------------------
// TELA 1: CALDEIRÃO DE ITAQUERA & API REST CLIMA
// -------------------------------------------------------------
@Composable
fun TelaHomeArena(climaRepo: ClimaItaqueraRepository, perfil: TimaoPerfil) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var clima by remember { mutableStateOf<ClimaArena?>(null) }
    var carregando by remember { mutableStateOf(false) }
    var erroMsg by remember { mutableStateOf("") }

    fun carregarClima() {
        scope.launch {
            carregando = true
            erroMsg = ""
            try {
                clima = climaRepo.buscarClimaArena()
            } catch (e: Exception) {
                erroMsg = "Deu ruim no sinal da Arena: ${e.message}"
            } finally {
                carregando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        carregarClima()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Faixa de Alerta Craque Neto
        Card(
            colors = CardDefaults.cardColors(containerColor = VermelhoSangue),
            border = BorderStroke(3.dp, BrancoTimao),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_craque_neto),
                    contentDescription = "Craque Neto",
                    modifier = Modifier
                        .size(52.dp)
                        .rotate(-8f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "PALAVRA DO CRAQUE NETO:",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = OuroMundial
                    )
                    Text(
                        text = perfil.fraseFavorita,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = BrancoTimao
                    )
                }
            }
        }

        // Card da API REST: Clima Neo Química Arena
        Card(
            colors = CardDefaults.cardColors(containerColor = PretoTimao),
            border = BorderStroke(2.dp, OuroMundial),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🏟️ CLIMA AO VIVO EM ITAQUERA (API REST)",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = BrancoTimao
                )
                Text(
                    text = "Neo Química Arena • Coordenadas da Fiel",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (carregando) {
                    CircularProgressIndicator(color = VermelhoSangue)
                } else if (erroMsg.isNotEmpty()) {
                    Text(text = erroMsg, color = VermelhoSangue, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = { carregarClima() },
                        colors = ButtonDefaults.buttonColors(containerColor = VermelhoSangue)
                    ) {
                        Text("Tentar de Novo!")
                    }
                } else if (clima != null) {
                    val c = clima!!
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${c.temperatura} °C",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                            color = OuroMundial
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Umidade: ${c.umidade}%", color = BrancoTimao, fontWeight = FontWeight.SemiBold)
                            Text(text = "Status: FIRME", color = VermelhoSangue, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = c.descricaoTempo,
                        color = BrancoTimao,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = c.recomendacaoNeto,
                        color = OuroMundial,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { carregarClima() },
                    colors = ButtonDefaults.buttonColors(containerColor = BrancoTimao),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Atualizar Clima da Arena", color = PretoTimao, fontWeight = FontWeight.Black)
                }
            }
        }

        // Botão Disparo Imediato de Notificação
        Button(
            onClick = {
                TimaoNotificationHelper.dispararNotificacao(
                    context,
                    999,
                    "🔥 GOL DO CORINTHIANS! 🦅",
                    "CÊ É LOUCO PÃO DE QUEIJO! O Timão acaba de sacudir o adversário!"
                )
                Toast.makeText(context, "Notificação enviada no seu celular!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = VermelhoSangue),
            border = BorderStroke(2.dp, BrancoTimao),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🔔 TESTAR NOTIFICAÇÃO DO CORINTHIANS AGORA!",
                fontWeight = FontWeight.Black,
                color = BrancoTimao,
                fontSize = 12.sp
            )
        }
    }
}

// -------------------------------------------------------------
// TELA 2: PROMESSAS E GASTOS (PERSISTÊNCIA SQLITE)
// -------------------------------------------------------------
@Composable
fun TelaPromessasEGastos(db: TimaoDbHelper) {
    val context = LocalContext.current
    var listaRegistros by remember { mutableStateOf(listOf<TimaoRegistro>()) }

    var tipoSelecionado by remember { mutableStateOf("PROMESSA") }
    var descricao by remember { mutableStateOf("") }
    var valorTexto by remember { mutableStateOf("") }

    fun atualizarLista() {
        listaRegistros = db.listarRegistros()
    }

    LaunchedEffect(Unit) {
        atualizarLista()
    }

    val totalGastos = listaRegistros.filter { it.tipo == "GASTO" }.sumOf { it.valorOuSacrificio }
    val promessasPendentes = listaRegistros.count { it.tipo == "PROMESSA" && !it.cumprido }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PretoTimao),
                border = BorderStroke(2.dp, BrancoTimao),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "📝 LIVRO DE PROMESSAS E GASTOS (SQLite)",
                        color = OuroMundial,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Gastos acumulados com o Coringão: R$ ${String.format(Locale.US, "%.2f", totalGastos)}",
                        color = BrancoTimao,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Promessas para São Jorge a pagar: $promessasPendentes",
                        color = VermelhoSangue,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Formulário de Cadastro
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                border = BorderStroke(1.dp, VermelhoSangue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "NOVO REGISTRO DO FIEL",
                        color = BrancoTimao,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { tipoSelecionado = "PROMESSA" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (tipoSelecionado == "PROMESSA") VermelhoSangue else Color.DarkGray
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🙏 Promessa", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { tipoSelecionado = "GASTO" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (tipoSelecionado == "GASTO") OuroMundial else Color.DarkGray
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("💸 Gasto (R$)", color = PretoTimao, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedTextField(
                        value = descricao,
                        onValueChange = { descricao = it },
                        label = { Text("O que é? (Ex: Subir a ladeira de joelhos / Ingressos)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BrancoTimao,
                            unfocusedTextColor = BrancoTimao,
                            focusedBorderColor = VermelhoSangue,
                            unfocusedBorderColor = BrancoTimao
                        )
                    )

                    OutlinedTextField(
                        value = valorTexto,
                        onValueChange = { valorTexto = it },
                        label = { Text(if (tipoSelecionado == "GASTO") "Valor em R$" else "Dias de Promessa / Valor Simbólico") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BrancoTimao,
                            unfocusedTextColor = BrancoTimao,
                            focusedBorderColor = VermelhoSangue,
                            unfocusedBorderColor = BrancoTimao
                        )
                    )

                    Button(
                        onClick = {
                            val v = valorTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
                            if (descricao.isBlank()) {
                                Toast.makeText(context, "Preencha a descrição, seu pé de rato!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val dataFormatada = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                            db.inserirRegistro(
                                TimaoRegistro(
                                    tipo = tipoSelecionado,
                                    descricao = descricao.trim(),
                                    valorOuSacrificio = v,
                                    data = dataFormatada,
                                    cumprido = false
                                )
                            )
                            descricao = ""
                            valorTexto = ""
                            atualizarLista()
                            Toast.makeText(context, "Gravado com sucesso no SQLite do Timão!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrancoTimao),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("GRAVAR NO SQLITE!", color = PretoTimao, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        item {
            Text(
                text = "HISTÓRICO GRAVADO NO BANCO (${listaRegistros.size}):",
                color = OuroMundial,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        items(listaRegistros, key = { it.id }) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = PretoTimao),
                border = BorderStroke(2.dp, if (item.tipo == "GASTO") OuroMundial else VermelhoSangue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (item.tipo == "GASTO") "💸 [GASTO]" else "🙏 [PROMESSA]",
                                color = if (item.tipo == "GASTO") OuroMundial else VermelhoSangue,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (item.cumprido) {
                                Text(text = "✅ PAGO/CUMPRIDO", color = Color.Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text(text = "⏳ PENDENTE", color = Color.Yellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(
                            text = item.descricao,
                            color = BrancoTimao,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (item.tipo == "GASTO") "Custo: R$ ${String.format(Locale.US, "%.2f", item.valorOuSacrificio)}"
                            else "Intensidade/Sacrifício: ${item.valorOuSacrificio.toInt()}",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )

                        Text(text = "Data: ${item.data}", color = Color.Gray, fontSize = 10.sp)
                    }

                    Row {
                        IconButton(onClick = {
                            db.alternarCumprido(item.id, !item.cumprido)
                            atualizarLista()
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Alternar Cumprido", tint = Color.Green)
                        }

                        IconButton(onClick = {
                            db.deletarRegistro(item.id)
                            atualizarLista()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = VermelhoSangue)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TELA 3: AGENDAMENTO DE ALARME & NOTIFICAÇÕES LOCAIS
// -------------------------------------------------------------
@Composable
fun TelaAlarmeJogo(context: Context) {
    var tituloAlarme by remember { mutableStateOf("VAI CORINTHIANS! HORA DO JOGO! 🦅") }
    var mensagemAlarme by remember { mutableStateOf("Desliga tudo que o Timão vai entrar em campo! VAMO GANHAR!") }

    var horarioSelecionado by remember {
        mutableLongStateOf(System.currentTimeMillis() + 10 * 60 * 1000) // 10 min a frente
    }

    val formatoDataHora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun escolherDataHora() {
        val cal = Calendar.getInstance().apply { timeInMillis = horarioSelecionado }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, day)

                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        cal.set(Calendar.HOUR_OF_DAY, hour)
                        cal.set(Calendar.MINUTE, minute)
                        cal.set(Calendar.SECOND, 0)
                        horarioSelecionado = cal.timeInMillis
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PretoTimao),
            border = BorderStroke(2.dp, VermelhoSangue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "⏰ ALARME DE JOGO DA FIEL (AlarmManager)",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = OuroMundial
                )
                Text(
                    text = "Programe para ser alertado mesmo se o celular estiver bloqueado ou com o app fechado!",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }
        }

        OutlinedTextField(
            value = tituloAlarme,
            onValueChange = { tituloAlarme = it },
            label = { Text("Título da Notificação") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = BrancoTimao,
                unfocusedTextColor = BrancoTimao,
                focusedBorderColor = VermelhoSangue,
                unfocusedBorderColor = BrancoTimao
            )
        )

        OutlinedTextField(
            value = mensagemAlarme,
            onValueChange = { mensagemAlarme = it },
            label = { Text("Recado Urgente") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = BrancoTimao,
                unfocusedTextColor = BrancoTimao,
                focusedBorderColor = VermelhoSangue,
                unfocusedBorderColor = BrancoTimao
            )
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            border = BorderStroke(1.dp, BrancoTimao),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { escolherDataHora() }
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Horário Programado do Alarme:", color = Color.Gray, fontSize = 11.sp)
                Text(
                    text = formatoDataHora.format(Date(horarioSelecionado)),
                    color = OuroMundial,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
                Text(text = "(Toque aqui para mudar o dia e hora)", color = VermelhoSangue, fontSize = 11.sp)
            }
        }

        Button(
            onClick = {
                val sucesso = TimaoNotificationHelper.agendarAlarme(
                    context,
                    Random().nextInt(10000),
                    tituloAlarme,
                    mensagemAlarme,
                    horarioSelecionado
                )
                if (sucesso) {
                    Toast.makeText(context, "✅ ALARME DO CORINTHIANS ATIVADO!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "⚠️ Permissão de alarme exato necessária nas configs do Android!", Toast.LENGTH_LONG).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = VermelhoSangue),
            border = BorderStroke(2.dp, BrancoTimao),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("PROGRAMAR ALARME NO ALARMMANAGER!", color = BrancoTimao, fontWeight = FontWeight.Black)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Alarme rápido de 10 segundos para teste de bancada
        Button(
            onClick = {
                val tempo10s = System.currentTimeMillis() + 10000
                TimaoNotificationHelper.agendarAlarme(
                    context,
                    1910,
                    "🦅 ALARME DE TESTE DO TIMÃO!",
                    "Acabou os 10 segundos! CÊ É LOUCO PÃO DE QUEIJO!",
                    tempo10s
                )
                Toast.makeText(context, "Agendado para daqui a 10 segundos! Pode bloquear a tela!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("⏱️ Teste Rápido (Dispara em 10 segundos)", color = BrancoTimao)
        }
    }
}

// -------------------------------------------------------------
// TELA 4: PREFERÊNCIAS DO TORCEDOR (DATASTORE)
// -------------------------------------------------------------
@Composable
fun TelaPerfilFiel(dataStore: TimaoDataStore, perfil: TimaoPerfil) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nome by remember(perfil) { mutableStateOf(perfil.nome) }
    var torcida by remember(perfil) { mutableStateOf(perfil.torcida) }
    var modoPistola by remember(perfil) { mutableStateOf(perfil.modoPistola) }
    var frase by remember(perfil) { mutableStateOf(perfil.fraseFavorita) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PretoTimao),
            border = BorderStroke(2.dp, OuroMundial),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "⚙️ PREFERÊNCIAS DO FIEL (DataStore)",
                    color = OuroMundial,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
                Text(
                    text = "Os dados salvos aqui persistem nas Preferências do Jetpack DataStore.",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }
        }

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome ou Apelido da Fiel") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = BrancoTimao,
                unfocusedTextColor = BrancoTimao,
                focusedBorderColor = VermelhoSangue,
                unfocusedBorderColor = BrancoTimao
            )
        )

        OutlinedTextField(
            value = torcida,
            onValueChange = { torcida = it },
            label = { Text("Organizada / Setor (Ex: Gaviões, Camisa 12, Setor Norte)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = BrancoTimao,
                unfocusedTextColor = BrancoTimao,
                focusedBorderColor = VermelhoSangue,
                unfocusedBorderColor = BrancoTimao
            )
        )

        OutlinedTextField(
            value = frase,
            onValueChange = { frase = it },
            label = { Text("Grito de Guerra / Craque Neto") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = BrancoTimao,
                unfocusedTextColor = BrancoTimao,
                focusedBorderColor = VermelhoSangue,
                unfocusedBorderColor = BrancoTimao
            )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Switch(
                checked = modoPistola,
                onCheckedChange = { modoPistola = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = VermelhoSangue,
                    checkedTrackColor = BrancoTimao
                )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = "Modo 'Craque Neto Pistola'", color = BrancoTimao, fontWeight = FontWeight.Bold)
                Text(text = "Ativa alertas mais agressivos da Fiel", color = Color.Gray, fontSize = 11.sp)
            }
        }

        Button(
            onClick = {
                scope.launch {
                    dataStore.salvarPerfil(nome.trim(), torcida.trim(), modoPistola, frase.trim())
                    Toast.makeText(context, "✅ Dados salvos com sucesso no DataStore!", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = VermelhoSangue),
            border = BorderStroke(2.dp, BrancoTimao),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SALVAR PREFERÊNCIAS NO DATASTORE", color = BrancoTimao, fontWeight = FontWeight.Black)
        }
    }
}
