package com.timao.tracker

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class TimaoAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", 1910)
        val titulo = intent.getStringExtra("titulo") ?: "🚨 ALERTA DA FIEL TORCIDA! 🦅"
        val mensagem = intent.getStringExtra("mensagem") ?: "CÊ É LOUCO PÃO DE QUEIJO! O CORINTHIANS VAI JOGAR!"

        TimaoNotificationHelper.dispararNotificacao(context, id, titulo, mensagem)
    }
}

object TimaoNotificationHelper {
    const val CHANNEL_ID = "timao_channel_fiel"

    fun criarCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID,
                "Notificações da Fiel Torcida",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarmes e avisos urgentes do Sport Club Corinthians Paulista"
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }
    }

    fun dispararNotificacao(context: Context, id: Int, titulo: String, mensagem: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notificacao = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(titulo)
            .setContentText(mensagem)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensagem))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(id, notificacao)
    }

    fun agendarAlarme(context: Context, id: Int, titulo: String, mensagem: String, tempoMillis: Long): Boolean {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !manager.canScheduleExactAlarms()) {
            return false
        }

        val intent = Intent(context, TimaoAlarmReceiver::class.java).apply {
            putExtra("id", id)
            putExtra("titulo", titulo)
            putExtra("mensagem", mensagem)
        }

        val pending = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        manager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            tempoMillis,
            pending
        )
        return true
    }
}
