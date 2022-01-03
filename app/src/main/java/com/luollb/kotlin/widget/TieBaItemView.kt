package com.luollb.kotlin.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.luollb.kotlin.DemoProvider
import com.luollb.kotlin.R

import com.luollb.kotlin.bean.TieBaBean
import com.luollb.kotlin.databinding.TieBaItemViewBinding
import com.squareup.picasso.Picasso

class TieBaItemView : ConstraintLayout {

    private lateinit var binding: TieBaItemViewBinding

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        binding = TieBaItemViewBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun update(bean: TieBaBean) {
        binding.data = bean
        Picasso.get().load(bean.bpics!![0]).into(binding.iv)
    }

}