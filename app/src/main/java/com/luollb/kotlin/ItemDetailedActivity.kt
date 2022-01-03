package com.luollb.kotlin

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.luollb.kotlin.adapter.BaseRecyclerViewAdapter
import com.luollb.kotlin.http.OkhttpManager
import com.luollb.kotlin.model.ItemDetailedModel
import com.squareup.picasso.Picasso
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.jsoup.Jsoup
import java.io.IOException

class ItemDetailedActivity : AppCompatActivity(), Callback,
    BaseRecyclerViewAdapter.RecyclerViewCreate {

    private lateinit var viewPage: ViewPager2
    private lateinit var adapter: BaseRecyclerViewAdapter<String>
    private val list = arrayListOf<String>()
    private lateinit var itemDetailedModel: ItemDetailedModel

    private val observer = Observer<String> {
        val parse = Jsoup.parse(it)
        val imageClass = parse.getElementsByClass("BDE_Image")
        list.clear()
        for (i in imageClass.indices) {
            val item = imageClass[i]
            val width = item.attr("width")
            if (width.isNotEmpty() && width.toInt() > 0) {
                list.add(item.attr("src"))
            }
        }

        if (list.size > 0) {
            adapter.notifyDataSetChangedAll(list)
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detailed)

        viewPage = findViewById(R.id.view_pager)
        adapter = BaseRecyclerViewAdapter(this, list, this)
        viewPage.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPage.adapter = adapter

        itemDetailedModel = ViewModelProvider(this).get(ItemDetailedModel::class.java)
        itemDetailedModel.getData().observe(this, observer)
        val url = intent.getStringExtra("url")

        println("url = $url")
        if (url != null && url.isNotEmpty()) {
            OkhttpManager.getSinger().accessNetworks(url, this)
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        itemDetailedModel.getData().removeObserver(observer)
        super.onDestroy()
    }

    override fun onFailure(call: Call, e: IOException) {
        println("onFailure")
    }

    override fun onResponse(call: Call, response: Response) {
        println("onResponse 获取数据成功")
        itemDetailedModel.getData().postValue(response.body?.string())
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): View {
        val iv = ImageView(this)
        iv.scaleType = ImageView.ScaleType.FIT_CENTER
        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )
        iv.layoutParams = params
        return iv
    }

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.BaseViewHolder, position: Int) {
        Picasso.get().load(list[position]).into(holder.itemView as ImageView)
    }
}