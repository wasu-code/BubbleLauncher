package com.github.wasu_code.bubblelauncher

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import com.github.wasu_code.bubblelauncher.util.BubbleHelper

class MainActivity : AppCompatActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                @SuppressLint("MissingPermission")
                launchBubble()
            } else {
                showPermissionDeniedMessage()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BubbleHelper.createBubbleChannel(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }

        // if permission not needed or already granted
        launchBubble()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun launchBubble() {
        val intent = Intent(this, BubbleChooserActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        BubbleHelper.postBubble(this, intent, "Bubble Chooser")
        finish()
    }

    private fun showPermissionDeniedMessage() {
        Toast.makeText(
            this,
            "Notification permission denied. Bubble cannot be displayed.",
            Toast.LENGTH_LONG
        ).show()
        finish()
    }
}