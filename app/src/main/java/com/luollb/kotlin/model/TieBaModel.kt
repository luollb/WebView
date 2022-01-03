package com.luollb.kotlin.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.luollb.kotlin.bean.TieBaBean

class TieBaModel : ViewModel() {

    private val data: MutableLiveData<ArrayList<TieBaBean>> by lazy {
        MutableLiveData<ArrayList<TieBaBean>>()
    }

    @JvmName("getData1")
    public fun getData(): MutableLiveData<ArrayList<TieBaBean>> {
        return data
    }
}