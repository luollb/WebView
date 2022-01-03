package com.luollb.kotlin.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luollb.kotlin.R
import com.luollb.kotlin.adapter.BaseRecyclerViewAdapter
import com.luollb.kotlin.bean.TieBaBean
import org.jsoup.Jsoup
import kotlin.math.abs

class TieBaView : ConstraintLayout, BaseRecyclerViewAdapter.RecyclerViewCreate {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: BaseRecyclerViewAdapter<TieBaBean>
    private var list = arrayListOf<TieBaBean>()
    private lateinit var loading: LoadingView

    private var onItemClickListener: OnItemClickListener? = null
    private var updateInterface: UpdateInterface? = null

    private var downY = 0f

    //更新数据防止点击
    private var isUpdating = false

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.fragment_tie_ba_view, this, true)
        rv = findViewById(R.id.rv)
        loading = findViewById(R.id.loading)

        adapter = BaseRecyclerViewAdapter(context, list, this)
        val manager = LinearLayoutManager(context)
        manager.orientation = LinearLayoutManager.VERTICAL
        rv.layoutManager = manager
        rv.adapter = adapter
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isUpdating) {//更新时拦截点击
                    return true
                }
                downY = ev.y
            }
            MotionEvent.ACTION_MOVE -> {

            }
            MotionEvent.ACTION_UP -> {
                val diffY = ev.y - downY

                if (diffY > 0 && diffY > 300f && !rv.canScrollVertically(-1)) {
                    dataUpdating()
                    updateInterface?.refreshData()
                }

                if (diffY < 0 && diffY < -300f && !rv.canScrollVertically(1)) {
                    dataUpdating()
                    updateInterface?.loadData()
                }
            }
            else -> {
                println("onInterceptTouchEvent ev=${ev.toString()}")
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    /**
     * 数据更新时的处理
     */
    private fun dataUpdating() {
        isUpdating = true
        if (loading.visibility == GONE) {
            loading.visibility = VISIBLE
        }
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    fun setUpdateInterface(updateInterface: UpdateInterface) {
        this.updateInterface = updateInterface
    }

    //取消监听
    fun onClear() {
        onItemClickListener = null
        updateInterface = null
        list.clear()
        adapter.onClear()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    //更新内容
    fun updateContent(list: ArrayList<TieBaBean>) {
        this.list = list
        println("updateContent = ${this.list.size}")
        if (this.list.size > 0) {
            isUpdating = false
            if (loading.visibility == VISIBLE) {
                loading.visibility = GONE
            }
            adapter.notifyDataSetChangedAll(this.list)
        }
    }

    //创建View
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): View {
        return TieBaItemView(context)
    }

    //绑定，显示View
    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.BaseViewHolder, position: Int) {
        (holder.itemView as TieBaItemView).update(list[position])
        holder.itemView.setOnClickListener {
            onItemClickListener?.itemClick(list[position].href)
        }
    }

    interface OnItemClickListener {
        fun itemClick(url: String)
    }
}