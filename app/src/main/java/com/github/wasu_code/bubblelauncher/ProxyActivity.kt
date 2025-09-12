package com.github.wasu_code.bubblelauncher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
class ProxyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
// Kick off a scan if DB is empty (simple heuristic)
        val db = com.github.wasu_code.bubblelauncher.data.AppDatabase.get(this)
// Launch a background scan before posting bubble (worker will update DB)
        val wr = OneTimeWorkRequestBuilder<AppScanWorker>().build()
        WorkManager.getInstance(this).enqueue(wr)
// Post a bubble that opens BubbleChooserActivity and pass along the original intent
        val bubbleIntent = Intent(this,
            BubbleChooserActivity::class.java).apply {
            putExtra("original_intent_action", intent?.action)
            putExtra("original_intent_uri", intent?.dataString)
            putExtras(intent ?: Intent())
        }
        com.github.wasu_code.bubblelauncher.util.BubbleHelper.postBubble(this,
            bubbleIntent, "Intent Proxy")
// Immediately finish: bubble will be used for UI
        finish()
    }
}