package com.luollb.kotlin.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.luollb.kotlin.bean.UserItemBean
import com.luollb.kotlin.bean.UserVideoBean

class UserVideoModel : ViewModel() {

    private val data = MutableLiveData<ArrayList<UserItemBean>>()

    @JvmName("getData1")
    fun getData(): MutableLiveData<ArrayList<UserItemBean>> {
        return data
    }
}