package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.UserPreferences

object NotificationHelper {
    private const val CHANNEL_ANALYSIS_ID = "reel_analysis_channel"
    private const val CHANNEL_ANALYSIS_NAME = "Analyses de Reels"
    private const val CHANNEL_ANALYSIS_DESC = "Alertes lorsque la synthèse d'un Reel est disponible."

    private const val CHANNEL_REMINDERS_ID = "reminders_channel"
    private const val CHANNEL_REMINDERS_NAME = "Rappels & Mémorisation"
    private const val CHANNEL_REMINDERS_DESC = "Rappels pour ré-explorer vos sauvegardes et souvenirs."

    private const val CHANNEL_SUGGESTIONS_ID = "suggestions_channel"
    private const val CHANNEL_SUGGESTIONS_NAME = "Conseils IA RemX"
    private const val CHANNEL_SUGGESTIONS_DESC = "Conseils personnalisés par l'assistant IA."

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channelAnalysis = NotificationChannel(CHANNEL_ANALYSIS_ID, CHANNEL_ANALYSIS_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = CHANNEL_ANALYSIS_DESC
            }
            val channelReminders = NotificationChannel(CHANNEL_REMINDERS_ID, CHANNEL_REMINDERS_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = CHANNEL_REMINDERS_DESC
            }
            val channelSuggestions = NotificationChannel(CHANNEL_SUGGESTIONS_ID, CHANNEL_SUGGESTIONS_NAME, NotificationManager.IMPORTANCE_LOW).apply {
                description = CHANNEL_SUGGESTIONS_DESC
            }

            notificationManager.createNotificationChannel(channelAnalysis)
            notificationManager.createNotificationChannel(channelReminders)
            notificationManager.createNotificationChannel(channelSuggestions)
        }
    }

    fun showReelAnalysisCompletedNotification(context: Context, reelId: String, title: String, summary: String) {
        val userPrefs = UserPreferences.getInstance(context)
        if (!userPrefs.areNotificationsEnabled() || !userPrefs.isNotifReelsEnabled()) return

        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (reelId.isNotBlank()) putExtra("reel_id", reelId)
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            if (reelId.isNotBlank()) reelId.hashCode() else 1001,
            intent,
            pendingIntentFlags
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ANALYSIS_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(if (title.isNotBlank()) title else "Analyse terminée")
            .setContentText(if (summary.isNotBlank()) summary else "Ton reel est disponible avec son résumé !")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(if (summary.isNotBlank()) summary else "Ton reel RemX est analysé avec succès !")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(if (reelId.isNotBlank()) reelId.hashCode() else 1001, builder.build())
        } catch (e: SecurityException) {
            // Permission missing or denied by user
        }
    }

    fun showReminderNotification(context: Context, title: String, message: String) {
        val userPrefs = UserPreferences.getInstance(context)
        if (!userPrefs.areNotificationsEnabled() || !userPrefs.isNotifRemindersEnabled()) return

        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(context, 2002, intent, pendingIntentFlags)

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(2002, builder.build())
        } catch (e: SecurityException) {
            // Permission missing
        }
    }

    fun showSuggestionNotification(context: Context, title: String, message: String) {
        val userPrefs = UserPreferences.getInstance(context)
        if (!userPrefs.areNotificationsEnabled() || !userPrefs.isNotifSuggestionsEnabled()) return

        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(context, 3003, intent, pendingIntentFlags)

        val builder = NotificationCompat.Builder(context, CHANNEL_SUGGESTIONS_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(3003, builder.build())
        } catch (e: SecurityException) {
            // Permission missing
        }
    }

    fun showTestNotification(context: Context) {
        showReelAnalysisCompletedNotification(
            context = context,
            reelId = "test_notification",
            title = "RemX • Test de notification",
            summary = "Les notifications RemX sont opérationnelles ! Vous recevrez une alerte directe dès la fin de l'analyse d'un Reel."
        )
    }
}
