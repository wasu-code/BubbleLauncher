package com.github.wasu_code.bubblelauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
class PackageChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Trigger a background rescan
        val wr = OneTimeWorkRequestBuilder<AppScanWorker>().build()
        WorkManager.getInstance(context).enqueue(wr)
    }
}
