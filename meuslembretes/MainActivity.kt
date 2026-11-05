
package com.example.meuslembretes

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.net.toUri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.createChannel(this)

        setContent {
            MaterialTheme {
                ReminderScreen()
            }
        }
    }
}

@Composable
private fun ReminderScreen() {

    val context = LocalContext.current

    val db = remember {
        ReminderDbHelper(context)
    }

    var description by remember { mutableStateOf("") }

    var selectedTime by remember {
        mutableLongStateOf(
            System.currentTimeMillis() + 60 * 60 * 1000
        )
    }

    var reminders by remember {
        mutableStateOf(db.getAll())
    }

    var message by remember {
        mutableStateOf("")
    }

    // Solicitação da permissão de notificações
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->

        message = if (granted) {
            "Permissão concedida. Toque em Salvar novamente."
        } else {
            "Permissão de notificações não concedida."
        }
    }

    // Seleção da data
    fun chooseDate() {

        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedTime
        }

        DatePickerDialog(
            context,
            { _, year, month, day ->

                val updated = Calendar.getInstance().apply {
                    timeInMillis = selectedTime
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, day)
                }

                selectedTime = updated.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Seleção do horário
    fun chooseHour() {

        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedTime
        }

        TimePickerDialog(
            context,
            { _, hour, minute ->

                val updated = Calendar.getInstance().apply {
                    timeInMillis = selectedTime
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                selectedTime = updated.timeInMillis
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    // Cadastro do lembrete
    fun save() {

        if (description.isBlank()) {
            message = "Digite a descrição do lembrete."
            return
        }

        if (selectedTime <= System.currentTimeMillis()) {
            message = "Escolha uma data e horário futuros."
            return
        }

        // Verifica permissão de notificações (Android 13+)
        if (
            Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
            return
        }

        // Verifica autorização para alarmes exatos (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            val manager =
                context.getSystemService(AlarmManager::class.java)

            if (!manager.canScheduleExactAlarms()) {

                val intent = Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                ).apply {
                    data = "package:${context.packageName}".toUri()
                }

                context.startActivity(intent)

                message = "Autorize alarmes exatos e toque em Salvar novamente."
                return
            }
        }

        // Salva no SQLite
        val id = db.insert(
            description.trim(),
            selectedTime
        )

        // Agenda a notificação
        try {

            val exact = AlarmScheduler.schedule(
                context,
                id,
                description.trim(),
                selectedTime
            )

            if (exact) {

                reminders = db.getAll()
                description = ""
                message = "Lembrete agendado para o horário escolhido."

            } else {

                // Remove o registro se não conseguiu agendar
                AlarmScheduler.cancel(context, id)
                db.delete(id)

                message = "Não foi possível agendar o alarme exato."
            }

        } catch (_: SecurityException) {

            db.delete(id)

            message = "O Android não autorizou o alarme exato."
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,

        floatingActionButton = {
            FloatingActionButton(
                onClick = { save() }
            ) {
                Text("+")
            }
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {

            Text(
                "MEUS LEMBRETES",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                "Organize seu dia",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(18.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = {
                    Text("O que você precisa lembrar?")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedButton(
                    onClick = { chooseDate() }
                ) {
                    Text(
                        SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.forLanguageTag("pt-BR")
                        ).format(Date(selectedTime))
                    )
                }

                OutlinedButton(
                    onClick = { chooseHour() }
                ) {
                    Text(
                        SimpleDateFormat(
                            "HH:mm",
                            Locale.forLanguageTag("pt-BR")
                        ).format(Date(selectedTime))
                    )
                }
            }

            if (message.isNotBlank()) {

                Spacer(Modifier.height(8.dp))

                Text(
                    message,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(22.dp))

            Text(
                "Lembretes cadastrados",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(8.dp))

            if (reminders.isEmpty()) {

                Text("Nenhum lembrete cadastrado.")

            } else {

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {

                    items(
                        reminders,
                        key = { it.id }
                    ) { reminder ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                        ) {

                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Text(
                                        reminder.description,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Text(
                                        SimpleDateFormat(
                                            "dd/MM/yyyy 'às' HH:mm",
                                            Locale.forLanguageTag("pt-BR")
                                        ).format(Date(reminder.timeMillis))
                                    )
                                }

                                TextButton(
                                    onClick = {

                                        AlarmScheduler.cancel(
                                            context,
                                            reminder.id
                                        )

                                        db.delete(reminder.id)

                                        reminders = db.getAll()
                                    }
                                ) {
                                    Text("Excluir")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
