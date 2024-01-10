package com.rendrapcx.tts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rendrapcx.tts.model.Level

class VMDBFactory(
    private val dao: Level
): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VMDB::class.java)){
            return VMDB(dao) as T
        }
        throw IllegalArgumentException("Unknown View Model Class")
    }
}