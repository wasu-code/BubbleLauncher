package com.github.wasu_code.bubblelauncher

import android.Manifest
import android.service.quicksettings.TileService
import android.content.Intent
import androidx.annotation.RequiresPermission
import com.github.wasu_code.bubblelauncher.util.BubbleHelper

class QuickTileService : TileService() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onClick() {
        super.onClick()

        // Create intent for BubbleChooserActivity and post bubble
        BubbleHelper.createBubbleChannel(this)
        val intent = Intent(this, BubbleChooserActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        BubbleHelper.postBubble(this, intent, null)
    }
}
