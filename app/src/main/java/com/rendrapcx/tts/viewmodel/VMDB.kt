package com.rendrapcx.tts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.model.Level
import kotlinx.coroutines.launch

class VMDB(
    private val dao: Level,
) : ViewModel() {

    val level = dao.getAllLevel()

    fun insertLevel(level: Data.Level)=viewModelScope.launch {
        dao.insertLevel(level)
    }

    fun updateLevel(level: Data.Level)=viewModelScope.launch {
        dao.updateLevel(level)
    }

    fun deleteLevel(level: Data.Level)=viewModelScope.launch {
        dao.deleteLevel(level)
    }

}