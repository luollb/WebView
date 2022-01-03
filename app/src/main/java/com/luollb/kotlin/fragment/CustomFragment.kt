package com.luollb.kotlin.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.luollb.kotlin.ItemDetailedActivity
import com.luollb.kotlin.MainActivity
import com.luollb.kotlin.MessageEvent
import com.luollb.kotlin.bean.TieBaBean
import com.luollb.kotlin.http.OkhttpManager
import com.luollb.kotlin.model.TieBaModel
import com.luollb.kotlin.widget.TieBaView
import com.luollb.kotlin.widget.UpdateInterface
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.greenrobot.eventbus.EventBus
import org.jsoup.Jsoup
import java.io.IOException

class CustomFragment : Fragment(), Callback, UpdateInterface {

    private val url = "https://tieba.baidu.com/f?kw=孙允珠&ie=utf-8&pn="
    private var pn = 0

    private lateinit var tieBaView: TieBaView
    private val model: TieBaModel  by viewModels()

    private val list = arrayListOf<TieBaBean>()

    //加载数据时是否清空原来数据
    private var isClearList = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        tieBaView = TieBaView(requireContext())

        tieBaView.setOnItemClickListener(object : TieBaView.OnItemClickListener {
            override fun itemClick(url: String) {
                val intent = Intent(requireContext(), ItemDetailedActivity::class.java)
                intent.putExtra("url", url)
                requireActivity().startActivity(intent)
            }
        })
        tieBaView.setUpdateInterface(this)

        //model = ViewModelProvider(this).get(TieBaModel::class.java)
        model.getData().observe(viewLifecycleOwner, {
            tieBaView.updateContent(it)
        })

        connect(pn)
        return tieBaView
    }

    override fun onDestroy() {
        tieBaView.onClear()
        super.onDestroy()
    }

    /**
     * 网络连接
     */
    private fun connect(index: Int) {
        OkhttpManager.getSinger().accessNetworks(url + index, this)
    }

    //网络访问失败
    override fun onFailure(call: Call, e: IOException) {
        println("onFailure")
    }

    //网络访问成功
    override fun onResponse(call: Call, response: Response) {
        var content = response.body?.string()
        content = content?.replace("<!--", "\n")
        content = content?.replace("-->", "\n")

        val parse = Jsoup.parse(content!!)
        val elements = parse.getElementsByClass("t_con")

        if (isClearList) {
            list.clear()
        }

        for (i in elements.indices) {
            var href: String? = null
            var title: String? = null
            var abs: String? = null
            val bpics = ArrayList<String>()
            val originals = ArrayList<String>()

            val element = elements[i]
            val elementTit = element.getElementsByClass("j_th_tit")
            val list_abs = element.getElementsByClass("threadlist_abs")
            val pic = element.getElementsByClass("j_m_pic")
            //println("element = ${element.toString()}")
            if (elementTit.size == 2) {
                href = elementTit[1].attr("href")
                title = elementTit[1].attr("title")
            }

            if (list_abs.size == 1) {
                abs = list_abs[0].text()
            }

            for (img in pic.indices) {
                bpics.add(pic[img].attr("bpic"))
                originals.add(pic[img].attr("data-original"))
            }

            //没有图片不加载该内容
            if (bpics.size == 0) {
                continue
            }

            list.add(TieBaBean(title!!, "https://tieba.baidu.com$href", abs, bpics, originals))
        }

        if (list.size > 0) {
            model.getData().postValue(list)
        }
    }

    //处理下拉刷新
    override fun refreshData() {
        isClearList = true
        pn = 0
        connect(pn)
    }

    //处理上拉加载
    override fun loadData() {
        isClearList = false
        pn += 50
        connect(pn)
    }
}