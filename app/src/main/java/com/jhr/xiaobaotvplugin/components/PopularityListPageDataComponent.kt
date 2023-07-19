package com.jhr.xiaobaotvplugin.components

import android.graphics.Color
import android.graphics.Typeface
import com.jhr.xiaobaotvplugin.actions.CustomAction
import com.jhr.xiaobaotvplugin.components.Const.host
import com.jhr.xiaobaotvplugin.components.Const.layoutSpanCount
import com.jhr.xiaobaotvplugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.components.ICustomPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData
import com.su.mediabox.pluginapi.data.MediaInfo2Data
import com.su.mediabox.pluginapi.data.SimpleTextData
import com.su.mediabox.pluginapi.data.TagData
import com.su.mediabox.pluginapi.data.ViewPagerData
import com.su.mediabox.pluginapi.util.UIUtil.dp
import org.jsoup.nodes.Element

/**
 * FileName: PopularityListPageDataComponent
 * Founder: Jiang Houren
 * Create Date: 2023/7/19 12:10
 * Profile: 人气榜单
 */
class PopularityListPageDataComponent  : ICustomPageDataComponent {

    override val pageName = "人气榜单"
    override fun menus() = mutableListOf(CustomAction())

    override suspend fun getData(page: Int): List<BaseData>? {
        if (page != 1)
            return null
        val url = Const.host
        val doc = JsoupUtil.getDocument(url)
        val module = doc.select("div[class='myui-panel myui-panel-bg hiddex-xs clearfix']")
        val rank1 = module.select(".col-lg-4")[0].let {
            object : ViewPagerData.PageLoader {
                override fun pageName(page: Int): String {
                    return "电影榜单"
                }

                override suspend fun loadData(page: Int): List<BaseData> {
                    return getTotalRankData(it)
                }
            }
        }
        val rank2 =module.select(".col-lg-4")[1].let {
            object : ViewPagerData.PageLoader {
                override fun pageName(page: Int): String {
                    return "电视剧榜单"
                }

                override suspend fun loadData(page: Int): List<BaseData> {
                    return getTotalRankData(it)
                }
            }
        }
        val rank3 = module.select(".col-lg-4")[2].let {
            object : ViewPagerData.PageLoader {
                override fun pageName(page: Int): String {
                    return "动漫榜单"
                }

                override suspend fun loadData(page: Int): List<BaseData> {
                    return getTotalRankData(it)
                }
            }
        }
        val rank4 = module.select(".col-lg-4")[3].let {
            object : ViewPagerData.PageLoader {
                override fun pageName(page: Int): String {
                    return "综艺榜单"
                }

                override suspend fun loadData(page: Int): List<BaseData> {
                    return getTotalRankData(it)
                }
            }
        }
        return listOf(ViewPagerData(mutableListOf(rank1, rank2,rank3,rank4)).apply {
            layoutConfig = BaseData.LayoutConfig(
                itemSpacing = 0,
                listLeftEdge = 0,
                listRightEdge = 0
            )
        })
    }

    private fun getTotalRankData(element: Element): List<BaseData> {
        val data = mutableListOf<BaseData>()
        val uls = element.select("ul")
        for ((ulIndex,ul) in uls.withIndex()){
            val lis = ul.select("li")
            if (ulIndex == 0){
                for (li in lis){
                    val a = li.select("a")
                    val title =  a.attr("title")
                    val cover = a.attr("data-original")
                    val url = a.attr("href")
                    val tags = mutableListOf<TagData>()
                    val tag = li.select(".detail").select("p")[0].text()
                        .replace("类型：","")
                        .replace("地区：","")
                        .split("，")
                    for (em in tag){
                        if (em.isNotBlank()) tags.add(TagData(em))
                    }
                    val describe = li.select(".detail").select("p")[1].text()
                    val item = MediaInfo2Data(
                        title, cover, Const.host + url, "", describe, tags
                    ).apply {
                        spanSize = layoutSpanCount
                        action = DetailAction.obtain(url)
                    }
                    data.add(item)
                }
            }else {
                for (li in lis){
                    val name = li.select("a").first()?.ownText()
                    val badge = li.select("a").select("span").text()
                    val href = li.select("a").attr("href")
                    // 序号
                    data.add(TagData(badge).apply {
                        spanSize = 1
                    })
                        data.add(SimpleTextData("$name").apply {
                            spanSize = 11
                            fontStyle = Typeface.BOLD
                            fontColor = Color.BLACK
                            paddingTop = 5.dp
                            paddingBottom = 5.dp
                            paddingLeft = 0.dp
                            paddingRight = 0.dp
                            action = DetailAction.obtain(href)
                        })
                }
            }
        }
        data[0].layoutConfig = BaseData.LayoutConfig(spanCount = layoutSpanCount)
        return data
    }
}