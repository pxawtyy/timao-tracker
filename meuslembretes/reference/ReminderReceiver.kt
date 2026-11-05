package com.example.meuslembretes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", 0)
        val description = intent.getStringExtra("description") ?: "Você tem um lembrete"
        NotificationHelper.show(context, id, description)
    }
}
