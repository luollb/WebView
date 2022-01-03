package com.luollb.kotlin.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class BaseRecyclerViewAdapter<T> private constructor() :
    RecyclerView.Adapter<BaseRecyclerViewAdapter.BaseViewHolder>() {

    private lateinit var list: List<T>
    private lateinit var context: Context
    private var recyclerViewCreate: RecyclerViewCreate? = null

    constructor(context: Context, list: List<T>, recyclerViewCreate: RecyclerViewCreate) : this() {
        this.list = list
        this.context = context
        this.recyclerViewCreate = recyclerViewCreate
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val onCreateViewHolder: View = recyclerViewCreate?.onCreateViewHolder(parent, viewType)!!
        return BaseViewHolder(onCreateViewHolder)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        recyclerViewCreate?.onBindViewHolder(holder, position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun onClear() {
        recyclerViewCreate = null
    }


    @SuppressLint("NotifyDataSetChanged")
    fun notifyDataSetChangedAll(list: List<T>) {
        this.list = list
        super.notifyDataSetChanged()
    }


    public class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    public interface RecyclerViewCreate {
        fun onCreateViewHolder(parent: ViewGroup, viewType: Int): View
        fun onBindViewHolder(holder: BaseViewHolder, position: Int)
    }
}