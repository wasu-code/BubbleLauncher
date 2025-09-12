package com.github.wasu_code.bubblelauncher

import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.wasu_code.bubblelauncher.data.AppDatabase
import com.github.wasu_code.bubblelauncher.data.AppEntity
import kotlinx.coroutines.launch
class BubbleChooserActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: AppAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bubble_chooser)
        recycler = findViewById(R.id.recycler)
        recycler.layoutManager = GridLayoutManager(this, 4)
        adapter = AppAdapter(this::onAppClicked, this::onAppLongPressed)
        recycler.adapter = adapter
        findViewById<Button>(R.id.btnRescan).setOnClickListener {
// trigger rescan
            val wr =
                androidx.work.OneTimeWorkRequestBuilder<AppScanWorker>().build()
            androidx.work.WorkManager.getInstance(this).enqueue(wr)
        }
        loadCached()
    }
    private fun loadCached() {
        lifecycleScope.launch {
            val list = AppDatabase.get(applicationContext).appDao().getAll()
            adapter.submitList(list)
        }
    }
    private fun onAppClicked(app: AppEntity) {
// Forward original intent if present
        val original = intent
        val target = Intent().apply {
            action = original.action
            setDataAndType(original.data, original.type)
            putExtras(original)
            setClassName(app.packageName, app.activityName)
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(target)
    }
    private fun onAppLongPressed(view: View, app: AppEntity) {
// simple context menu
        val popup = android.widget.PopupMenu(this, view)
        popup.menu.add("Open in split-screen")
        popup.menu.add("Open as new task (floating if supported)")
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.title) {
                "Open in split-screen" -> {
                    val target = Intent().apply {
                        setClassName(app.packageName, app.activityName);
                        addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or
                                Intent.FLAG_ACTIVITY_NEW_TASK) }
                    startActivity(target)
                    true
                }
                "Open as new task (floating if supported)" -> {
                    val target = Intent().apply {
                        setClassName(app.packageName, app.activityName);
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
// Many devices ignore floating requests; this is a besteffort.
                    startActivity(target)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}