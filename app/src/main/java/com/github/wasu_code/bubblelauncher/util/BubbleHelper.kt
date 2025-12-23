package com.github.wasu_code.bubblelauncher.util

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.app.Person
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Icon
import androidx.annotation.RequiresPermission
import com.github.wasu_code.bubblelauncher.data.AppEntity
import java.util.UUID
import androidx.core.graphics.createBitmap
import com.github.wasu_code.bubblelauncher.BubbleChooserActivity
import com.github.wasu_code.bubblelauncher.R
import androidx.core.graphics.get

const val CHANNEL_ID = "bubble_channel"
const val MAX_DYNAMIC_SHORTCUTS = 5

object BubbleHelper {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun postBubble(context: Context, bubbleActivityIntent: Intent, app: AppEntity?) {
        // Ensure bubble intent has an action
        if (bubbleActivityIntent.action == null) {
            bubbleActivityIntent.action = "com.github.wasu_code.bubblelauncher.OPEN_BUBBLE_${UUID.randomUUID()}"
        }
        bubbleActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        // If no app specified launch bubble chooser
        val app = app ?: AppEntity(
            id = "",
            label = "Bubble Launcher",
            packageName = context.packageName,
            activityName = BubbleChooserActivity::class.java.name,
        )

        // Unique PendingIntent
        val pending = PendingIntent.getActivity(
            context,
            UUID.randomUUID().hashCode(),
            bubbleActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // target app icon
        val pm = context.packageManager
        val appIcon: Icon = try {
            val drawable = pm.getApplicationIcon(app.packageName)

            val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
            val canvas = Canvas(bitmap)

            // Quick transparency check using top-left pixel
            drawable.setBounds(0, 0, 2, 2)
            drawable.draw(canvas)
            val hasTransparency = Color.alpha(bitmap[0, 0]) < 255
            val padding = if (hasTransparency) 24 else 0

            canvas.drawColor(Color.WHITE)
            drawable.setBounds(padding, padding, canvas.width - padding, canvas.height - padding)
            drawable.draw(canvas)

            Icon.createWithAdaptiveBitmap(bitmap)
        } catch (_: Exception) {
            Icon.createWithResource(context, R.mipmap.ic_launcher)
        }

        // Shortcut management
        val sm = context.getSystemService(ShortcutManager::class.java)
        val shortcutId = "bubble_${UUID.randomUUID()}"
        val shortcut = ShortcutInfo.Builder(context, shortcutId)
            .setShortLabel(app.label)
            .setLongLived(true)
            .setIcon(appIcon)
            .setIntent(Intent(Intent.ACTION_DEFAULT))
            .build()

        sm?.let {
            // Remove dynamic shortcuts whose notifications no longer exist
            cleanupObsoleteShortcuts(context, it)

            val current = it.dynamicShortcuts.toMutableList()
            if (current.size >= MAX_DYNAMIC_SHORTCUTS) {
                val toRemove = current.take(current.size - MAX_DYNAMIC_SHORTCUTS + 1).map { s -> s.id }
                it.removeDynamicShortcuts(toRemove)
            }
            it.addDynamicShortcuts(listOf(shortcut))
        }

        // Bubble metadata
        val bubbleMetadata = Notification.BubbleMetadata.Builder(
            pending,
            Icon.createWithResource(context, R.mipmap.ic_launcher)
        )
            .setDesiredHeight(800)
            .setAutoExpandBubble(true)
            .setSuppressNotification(false)
            .build()

        // Dummy people
        val me = Person.Builder().setName("Me").setImportant(true).build()
        val partner = Person.Builder().setName("Partner").setImportant(true).build()
        val messagingStyle = Notification.MessagingStyle(me)
            .addMessage("Bubble active", System.currentTimeMillis(), partner)
            .setGroupConversation(false)

        // Unique notification ID
        val notifId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()

        // Build notification
        val notif = Notification.Builder(context, CHANNEL_ID)
            .setContentTitle(app.label)
            .setSmallIcon(appIcon)
            .setBubbleMetadata(bubbleMetadata)
            .setShortcutId(shortcutId)
            .setStyle(messagingStyle)
            .setOngoing(false)
            .setAutoCancel(true)
            .build()

        val nm = context.getSystemService(NotificationManager::class.java)
        nm.notify(notifId, notif)
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

    /**
     * Remove dynamic shortcuts whose notifications no longer exist.
     */
    private fun cleanupObsoleteShortcuts(context: Context, sm: ShortcutManager) {
        val nm = context.getSystemService(NotificationManager::class.java)
        val activeIds: Set<String> = nm.activeNotifications
            .mapNotNull { sbn -> sbn.notification.shortcutId }
            .toSet()

        val obsolete = sm.dynamicShortcuts.map { it.id }.filter { it !in activeIds }
        if (obsolete.isNotEmpty()) sm.removeDynamicShortcuts(obsolete)
    }

}