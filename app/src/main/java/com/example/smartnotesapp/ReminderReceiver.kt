package com.example.smartnotesapp

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        // CHECK FIRST
        val pref = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val isEnabled = pref.getBoolean("notifications", true)

        if (!isEnabled) return
        val message = intent.getStringExtra("msg") ?: "Reminder!"

        val channelId = "reminder_channel_sound"

        val alarmSound = RingtoneManager.getDefaultUri(
            RingtoneManager.TYPE_ALARM
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                channelId,
                "Reminder Channel",
                NotificationManager.IMPORTANCE_HIGH
            )

            channel.setSound(alarmSound, audioAttributes)

            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Reminder 🔔")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(alarmSound)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}