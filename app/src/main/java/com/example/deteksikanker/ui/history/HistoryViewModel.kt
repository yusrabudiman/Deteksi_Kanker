package com.example.deteksikanker.ui.history

import androidx.lifecycle.LiveData
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.deteksikanker.data.local.database.AppDatabaseRoomCancerDetection
import com.example.deteksikanker.data.local.database.HistoryCancerRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val historyDao = AppDatabaseRoomCancerDetection.getDatabase(application).historyCancerDao()
    val allRecords: LiveData<List<HistoryCancerRecord>> = historyDao.getAllRecords()

    fun insertRecord(record: HistoryCancerRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            historyDao.insertRecord(record)
        }
    }
}
