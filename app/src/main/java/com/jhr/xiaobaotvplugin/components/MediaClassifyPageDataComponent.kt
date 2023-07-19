package com.jhr.xiaobaotvplugin.components

import android.util.Log
import com.jhr.xiaobaotvplugin.util.JsoupUtil
import com.jhr.xiaobaotvplugin.util.ParseHtmlUtil
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.components.IMediaClassifyPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData
import com.su.mediabox.pluginapi.data.ClassifyItemData
import com.su.mediabox.pluginapi.util.PluginPreferenceIns
import com.su.mediabox.pluginapi.util.TextUtil.urlDecode
import com.su.mediabox.pluginapi.util.WebUtil
import com.su.mediabox.pluginapi.util.WebUtilIns
import org.jsoup.Jsoup

class MediaClassifyPageDataComponent : IMediaClassifyPageDataComponent {
    var classify : String = Const.host +"/index.php/vod/type/id/5.html"

    override suspend fun getClassifyItemData(): List<ClassifyItemData> {
        val classifyItemDataList = mutableListOf<ClassifyItemData>()
        //示例：使用WebUtil解析动态生成的分类项
        val cookies = mapOf("cookie" to PluginPreferenceIns.get(JsoupUtil.cfClearanceKey, ""))
        Log.e("TAG","classify $classify")
        val document = Jsoup.parse(
            WebUtilIns.getRenderedHtmlCode(
                 classify, loadPolicy = object :
                    WebUtil.LoadPolicy by WebUtil.DefaultLoadPolicy {
                    override val headers = cookies
                    override val userAgentString = Const.ua
                    override val isClearEnv = false
                }
            )
        )
        document.select(".myui-panel_bd")[0].select("ul").forEach {
            classifyItemDataList.addAll(ParseHtmlUtil.parseClassifyEm(it))
        }
        return classifyItemDataList
    }

    override suspend fun getClassifyData(
        classifyAction: ClassifyAction,
        page: Int
    ): List<BaseData> {
        val classifyList = mutableListOf<BaseData>()
        // https://xiaobaotv.net/index.php/vod/type/id/5/page/2.html
        // https://xiaobaotv.net/index.php/vod/show/id/5/page/2/year/2023.html
        Log.e("TAG", "获取分类数据 ${classifyAction.url}")
        val str = classifyAction.url?.urlDecode() ?:""
        val charToInsert = "/page/${page}"
        var indexToInsert = str.length - 5
        if (str.contains("/year/")){
            indexToInsert -= 10
        }
        var url = StringBuilder(str).insert(indexToInsert, charToInsert).toString()
        if (!url.startsWith(Const.host)){
            url = Const.host + url
        }
        classify = url
        Log.e("TAG", "获取分类数据 $url")
        JsoupUtil.getDocument(url).also {
            classifyList.addAll(ParseHtmlUtil.parseClassifyEm(it, url))
        }
        return classifyList
    }
}