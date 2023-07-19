package com.jhr.xiaobaotvplugin.components

import com.jhr.xiaobaotvplugin.actions.CustomAction
import com.jhr.xiaobaotvplugin.components.Const.host
import com.jhr.xiaobaotvplugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.components.ICustomPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData
import com.su.mediabox.pluginapi.data.MediaInfo2Data
import com.su.mediabox.pluginapi.data.TagData
import com.su.mediabox.pluginapi.data.ViewPagerData
import org.jsoup.nodes.Element

/**
 * FileName: RecentUpdatesPageDataComponent
 * Founder: Jiang Houren
 * Create Date: 2023/7/19 11:53
 * Profile: 最近更新
 */
class RecentUpdatesPageDataComponent : ICustomPageDataComponent {

    override val pageName = "最近更新"
    override fun menus() = mutableListOf(CustomAction())

    override suspend fun getData(page: Int): List<BaseData>? {
        if (page != 1)
            return null
        val url = "$host/index.php/label/new.html"
        val doc = JsoupUtil.getDocument(url)
        val module = doc.select("div[class='myui-panel-box clearfix']")[0]
        val rank1 = module.select(".col-lg-4")[0].let {
            object : ViewPagerData.PageLoader {
                override fun pageName(page: Int): String {
                    return "电影更新"
                }

                override suspend fun loadData(page: Int): List<BaseData> {
                    return getTotalRankData(it)
                }
            }
        }
        val rank2 =module.select(".col-lg-4")[1].let {
            object : ViewPagerData.PageLoader {
                override fun pageName(page: Int): String {
                    return "电视剧更新"
                }

                override suspend fun loadData(page: Int): List<BaseData> {
                    return getTotalRankData(it)
                }
            }
        }
        val rank3 = module.select(".col-lg-4")[2].let {
            object : ViewPagerData.PageLoader {
                override fun pageName(page: Int): String {
                    return "动漫更新"
                }

                override suspend fun loadData(page: Int): List<BaseData> {
                    return getTotalRankData(it)
                }
            }
        }
        val rank4 = module.select(".col-lg-4")[3].let {
            object : ViewPagerData.PageLoader {
                override fun pageName(page: Int): String {
                    return "综艺更新"
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
        element.select("ul").select("li").forEach {
            val a = it.select("a")
            val title =  a.attr("title")
            val cover = a.attr("data-original")
            val url = a.attr("href")
            val tags = mutableListOf<TagData>()
            val tag = it.select(".detail").select("p")[0].text()
                .replace("类型：","")
                .replace("地区：","")
                .split("，")
            for (em in tag){
                if (em.isNotBlank()) tags.add(TagData(em))
            }
            val describe = it.select(".detail").select("p")[1].text()
            val item = MediaInfo2Data(
                title, cover, host + url, "", describe, tags
            ).apply {
                action = DetailAction.obtain(url)
            }
            data.add(item)
        }
        return data
    }
}