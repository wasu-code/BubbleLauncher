package com.github.wasu_code.bubblelauncher.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "embeddable_apps")
data class AppEntity(
    @PrimaryKey val id: String, // packageName + "/" + activityName
    val packageName: String,
    val activityName: String,
    val label: String
)