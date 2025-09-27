package com.github.wasu_code.bubblelauncher

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.wasu_code.bubblelauncher.data.AppDatabase
import com.github.wasu_code.bubblelauncher.data.AppEntity
import com.github.wasu_code.bubblelauncher.util.BubbleHelper
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class BubbleChooserActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: AppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()  // Hide default AppCompat ActionBar
        setContentView(R.layout.activity_bubble_chooser)

        recycler = findViewById(R.id.recycler)
        recycler.layoutManager = GridLayoutManager(this, 4)
        adapter = AppAdapter(this::onAppClicked, this::onAppLongPressed)
        recycler.adapter = adapter

        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        topAppBar.inflateMenu(R.menu.top_app_bar_menu)
        topAppBar.setOnClickListener {
            // trigger rescan
            val wr =
                androidx.work.OneTimeWorkRequestBuilder<AppScanWorker>().build()
            androidx.work.WorkManager.getInstance(this).enqueue(wr)
        }
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_add_bubble -> {
                    openNewBubble()
                    true
                }
                else -> false
            }
        }

        lifecycleScope.launch {
            AppDatabase.get(applicationContext)
                .appDao()
                .getAllFlow()
                .collect { list ->
                    adapter.submitList(list)
                }
        }
    }

    private fun openNewBubble() {
        val intent = Intent(this, BubbleChooserActivity::class.java)
        @SuppressLint("MissingPermission")
        BubbleHelper.postBubble(this, intent, null)
    }

    private fun onAppClicked(app: AppEntity) {
        // Forward original intent if present
        val original = intent
        val target = Intent().apply {
            action = original.action
            setDataAndType(original.data, original.type)
            putExtras(original)
            setClassName(app.packageName, app.activityName)
        }
//        startActivity(target)
        @SuppressLint("MissingPermission")
        BubbleHelper.postBubble(this, target, app)
        finish()
    }
    private fun onAppLongPressed(view: View, app: AppEntity) {
        //TODO see https://github.com/farmerbb/Taskbar
//        val popup = android.widget.PopupMenu(this, view)
//        popup.menu.add("Open in split-screen")
//        popup.menu.add("Open in freeform / floating")
//        popup.menu.add("Open as new task")
//
//        popup.setOnMenuItemClickListener { item: MenuItem ->
//            when (item.title) {
//
//                "Open in split-screen" -> {
//                    val intent = Intent().apply {
//                        setClassName(app.packageName, app.activityName)
//                        addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
//                    }
//                    startActivity(intent)
//                    true
//                }
//
//                "Open in freeform / floating" -> {
//                    val intent = Intent().apply {
//                        setClassName(app.packageName, app.activityName)
//                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    }
//
//                    // Use ActivityOptions to request freeform window (floating) if device supports
//                    val options =
//                        android.app.ActivityOptions.makeBasic().apply {
//                            launchBounds = android.graphics.Rect(100, 100, 800, 800)
//                        }
//
//                    if (options != null) startActivity(intent, options.toBundle())
//                    else startActivity(intent)
//
//                    true
//                }
//
//                "Open as new task" -> {
//                    val intent = Intent().apply {
//                        setClassName(app.packageName, app.activityName)
//                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    }
//                    startActivity(intent)
//                    true
//                }
//
//                else -> false
//            }
//        }
//
//        popup.show()
    }

}