package com.github.wasu_code.bubblelauncher.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface AppDao {
    @Query("SELECT * FROM embeddable_apps ORDER BY label")
    suspend fun getAll(): List<AppEntity>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<AppEntity>)


    @Query("DELETE FROM embeddable_apps")
    suspend fun clearAll()
}