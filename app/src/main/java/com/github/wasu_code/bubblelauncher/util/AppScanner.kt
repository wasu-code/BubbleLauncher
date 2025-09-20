package com.github.wasu_code.bubblelauncher.util

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import com.github.wasu_code.bubblelauncher.data.AppEntity

object AppScanner {
    fun scan(context: Context): List<AppEntity> {
        val pm = context.packageManager
        val launcherIntent =
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val matches = pm.queryIntentActivities(
            launcherIntent,
            PackageManager.MATCH_ALL
        )
        val results = mutableListOf<AppEntity>()

        for (ri in matches) {
            val ai: ActivityInfo = ri.activityInfo

            // Only allow standard (multiple) or singleTop
            if (ai.launchMode == ActivityInfo.LAUNCH_MULTIPLE ||
                ai.launchMode == ActivityInfo.LAUNCH_SINGLE_TOP
            ) {
                val id = "${ai.packageName}/${ai.name}"
                val label = ri.loadLabel(pm).toString()
                results.add(
                    AppEntity(
                        id = id,
                        packageName = ai.packageName,
                        activityName = ai.name,
                        label = label
                    )
                )
            }
        }
        return results
    }
}