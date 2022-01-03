package com.luollb.kotlin.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ItemDetailedModel : ViewModel() {

    private val data: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    @JvmName("getData1")
    fun getData(): MutableLiveData<String> {
        return data
    }
}