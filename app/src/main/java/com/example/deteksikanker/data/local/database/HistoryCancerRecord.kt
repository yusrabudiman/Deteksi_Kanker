package com.example.deteksikanker.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_cancer_record")
data class HistoryCancerRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val confidenceScore: Float,
    val date: Long,
    val image: String
)