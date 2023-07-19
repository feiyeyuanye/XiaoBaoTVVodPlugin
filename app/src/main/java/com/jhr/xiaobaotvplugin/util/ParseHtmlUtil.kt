package com.jhr.xiaobaotvplugin.util

import android.util.Log
import com.jhr.xiaobaotvplugin.components.Const.host
import com.jhr.xiaobaotvplugin.components.Const.layoutSpanCount
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.data.*
import java.net.URL

object ParseHtmlUtil {

    fun getCoverUrl(cover: String, imageReferer: String): String {
        return when {
            cover.startsWith("//") -> {
                try {
                    "${URL(imageReferer).protocol}:$cover"
                } catch (e: Exception) {
                    e.printStackTrace()
                    cover
                }
            }
            cover.startsWith("/") -> {
                //url不全的情况
                host + cover
            }
            else -> cover
        }
    }

    /**
     * 解析搜索的元素
     * @param element ul的父元素
     */
    fun parseSearchEm(
        element: Element,
        imageReferer: String
    ): List<BaseData> {
        val videoInfoItemDataList = mutableListOf<BaseData>()

        val lpic = element.select("#searchList")
        val results: Elements = lpic.select("li")
        for (i in results.indices) {
            var cover = results[i].select("a").attr("data-original")
            if (imageReferer.isNotBlank())
                cover = getCoverUrl(cover, imageReferer)
            val title = results[i].select("a").attr("title")+"[小宝影院]"
            val url = results[i].select("a").attr("href")
            val episode = results[i].select(".text-right").text()
            val tags = mutableListOf<TagData>()
            val tag = results[i].select(".detail").select("p")[2].text()
                .replace("分类","")
                .replace("地区","")
                .replace("年份","")
                .split("：")
            for (em in tag){
                if (em.isNotBlank()) tags.add(TagData(em))
            }
            val describe = results[i].select(".detail").select("p[class='hidden-xs']").text()
            val item = MediaInfo2Data(
                title, cover, host + url, episode, describe, tags
            ).apply {
                    action = DetailAction.obtain(url)
                }
            videoInfoItemDataList.add(item)
        }
        return videoInfoItemDataList
    }
    /**
     * 解析分类下的元素
     * @param element ul的父元素
     */
    fun parseClassifyEm(
        element: Element,
        imageReferer: String
    ): List<BaseData> {
        val videoInfoItemDataList = mutableListOf<BaseData>()
        val results: Elements = element.select("ul[class='myui-vodlist clearfix']").select("li")
        for (i in results.indices) {
            val title = results[i].select("a").attr("title")
            var cover = results[i].select("a").attr("data-original")
            if (imageReferer.isNotBlank())
                cover = getCoverUrl(cover, imageReferer)
            val url = results[i].select("a").attr("href")
            val episode = results[i].select(".text-right").text()
            val item = MediaInfo1Data(title, cover, host + url, episode ?: "")
                .apply {
                    spanSize = layoutSpanCount / 3
                    action = DetailAction.obtain(url)
                }
            videoInfoItemDataList.add(item)
        }
        videoInfoItemDataList[0].layoutConfig = BaseData.LayoutConfig(layoutSpanCount)
        return videoInfoItemDataList
    }
    /**
     * 解析分类元素
     */
    fun parseClassifyEm(element: Element): List<ClassifyItemData> {
        val classifyItemDataList = mutableListOf<ClassifyItemData>()
        var classifyCategory =""
        val li = element.select("li")
        for ((index,em) in li.withIndex()){
            val a = em.select("a")
            if (index == 0) {
                classifyCategory = a.text()
            }else{
                classifyItemDataList.add(ClassifyItemData().apply {
                    action = ClassifyAction.obtain(
                        a.attr("href").apply {
                            Log.d("分类链接", this)
                        },
                        classifyCategory,
                        a.text()
                    )
                })
            }
            }
        return classifyItemDataList
    }
}