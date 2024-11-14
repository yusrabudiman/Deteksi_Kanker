package com.example.deteksikanker.data.local.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HistoryCancerDao {
    @Insert
    suspend fun insertRecord(record: HistoryCancerRecord)
    @Query("SELECT * FROM history_cancer_record ORDER BY id DESC")
    fun getAllRecords(): LiveData<List<HistoryCancerRecord>>
}