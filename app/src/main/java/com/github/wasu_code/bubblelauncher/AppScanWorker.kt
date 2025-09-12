package com.github.wasu_code.bubblelauncher

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.wasu_code.bubblelauncher.data.AppDatabase
import com.github.wasu_code.bubblelauncher.util.AppScanner

class AppScanWorker(appContext: Context, params: WorkerParameters):
    CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val list = AppScanner.scan(applicationContext)
        val db = AppDatabase.get(applicationContext)
        db.appDao().clearAll()
        db.appDao().insertAll(list)
        return Result.success()
    }
}
