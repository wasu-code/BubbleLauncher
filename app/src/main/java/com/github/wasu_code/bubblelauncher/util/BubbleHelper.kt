package com.github.wasu_code.bubblelauncher.util

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.app.Person
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import androidx.annotation.RequiresPermission
import java.util.UUID

const val CHANNEL_ID = "bubble_channel"
const val NOTIF_ID_BUBBLE = 1001

object BubbleHelper {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun postBubble(context: Context, bubbleActivityIntent: Intent, title: String) {

        // Ensure the bubble intent has an action
        if (bubbleActivityIntent.action == null) {
            bubbleActivityIntent.action = "com.github.wasu_code.bubblelauncher.OPEN_BUBBLE"
        }
        bubbleActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        val pending = PendingIntent.getActivity(
            context,
            0,
            bubbleActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val shortcutId = "bubble_${UUID.randomUUID()}"
        val sm = context.getSystemService(ShortcutManager::class.java)
        val shortcut = android.content.pm.ShortcutInfo.Builder(context, shortcutId)
            .setShortLabel("Bubble")
            .setLongLived(true) // internal bubble association
            .setIntent(Intent(Intent.ACTION_DEFAULT)) // dummy intent
            .build()
        sm?.addDynamicShortcuts(listOf(shortcut))

        val bubbleMetadata = Notification.BubbleMetadata.Builder(
            pending,
            Icon.createWithResource(context, com.github.wasu_code.bubblelauncher.R.drawable.ic_launcher_foreground)
        )
            .setDesiredHeight(800)
            .setAutoExpandBubble(true) // Recommended for conversation-style
            .setSuppressNotification(true) // Prevent duplicate notification if desired
            .build()

        // Dummy "self" person
        val me = Person.Builder()
            .setName("Me")
            .setImportant(true)
            .build()

        // Dummy partner person
        val partner = Person.Builder()
            .setName("Partner")
            .setImportant(true)
            .build()

        // MessagingStyle with "self"
        val messagingStyle = Notification.MessagingStyle(me)
            .addMessage("Bubble active", System.currentTimeMillis(), partner)
            .setGroupConversation(false)


        val notif = Notification.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setSmallIcon(com.github.wasu_code.bubblelauncher.R.drawable.ic_launcher_foreground)
            .setBubbleMetadata(bubbleMetadata)
            .setShortcutId(shortcutId)
            .setStyle(messagingStyle)
            .setOngoing(true) // required for bubble
            .build()

        val nm = context.getSystemService(NotificationManager::class.java)
        nm.notify(NOTIF_ID_BUBBLE, notif)
    }

    fun createBubbleChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Bubble Notifications",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Channel for chat bubbles and embedded apps"
        }
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }
}
